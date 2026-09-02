package com.example.data.repository

import android.content.Context
import com.example.data.local.AddressEntity
import com.example.data.local.AppDatabase
import com.example.data.local.CartItemEntity
import com.example.data.local.FavoriteEntity
import com.example.data.local.OrderEntity
import com.example.data.model.Order
import com.example.data.model.OrderItem
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.data.model.SampleData
import com.example.data.model.UserAddress
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

class GroceryRepository(context: Context) {
    private val database = AppDatabase.getDatabase(context)
    private val cartDao = database.cartDao()
    private val addressDao = database.addressDao()
    private val orderDao = database.orderDao()
    private val favoriteDao = database.favoriteDao()

    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val orderItemListType = Types.newParameterizedType(List::class.java, OrderItem::class.java)
    private val orderItemAdapter = moshi.adapter<List<OrderItem>>(orderItemListType)

    // Products catalog with dynamic store management
    private val _customProducts = kotlinx.coroutines.flow.MutableStateFlow<List<Product>>(SampleData.products)
    val productsFlow: Flow<List<Product>> = _customProducts

    val allProducts: List<Product>
        get() = _customProducts.value

    val allCategories = SampleData.categories

    private val _couponsMap = kotlinx.coroutines.flow.MutableStateFlow<Map<String, Double>>(SampleData.coupons)
    val couponsFlow: Flow<Map<String, Double>> = _couponsMap

    fun getCoupons(): Map<String, Double> = _couponsMap.value

    fun addCoupon(code: String, discount: Double) {
        val updated = _couponsMap.value.toMutableMap()
        updated[code.trim().uppercase()] = discount
        _couponsMap.value = updated
    }

    fun removeCoupon(code: String) {
        val updated = _couponsMap.value.toMutableMap()
        updated.remove(code.trim().uppercase())
        _couponsMap.value = updated
    }

    fun addProduct(product: Product) {
        val updated = _customProducts.value.toMutableList()
        updated.add(0, product)
        _customProducts.value = updated
    }

    fun updateProduct(product: Product) {
        val updated = _customProducts.value.map {
            if (it.id == product.id) product else it
        }
        _customProducts.value = updated
    }

    fun deleteProduct(productId: String) {
        val updated = _customProducts.value.filterNot { it.id == productId }
        _customProducts.value = updated
    }

    fun getProductById(id: String): Product? {
        return _customProducts.value.find { it.id == id }
    }

    // Cart Streams & Operations
    fun getCartItems(): Flow<Map<Product, Int>> {
        return cartDao.getAllCartItems().map { entities ->
            entities.mapNotNull { entity ->
                val prod = getProductById(entity.productId)
                if (prod != null) prod to entity.quantity else null
            }.toMap()
        }
    }

    suspend fun addToCart(productId: String, quantityToAdd: Int = 1) {
        val currentEntities = database.cartDao()
        cartDao.insertOrUpdate(
            CartItemEntity(productId = productId, quantity = quantityToAdd)
        )
    }

    suspend fun updateCartQuantity(productId: String, newQuantity: Int) {
        if (newQuantity <= 0) {
            cartDao.deleteCartItem(productId)
        } else {
            cartDao.insertOrUpdate(CartItemEntity(productId = productId, quantity = newQuantity))
        }
    }

    suspend fun removeFromCart(productId: String) {
        cartDao.deleteCartItem(productId)
    }

    suspend fun clearCart() {
        cartDao.clearCart()
    }

    // Orders Streams & Operations
    fun getAllOrders(): Flow<List<Order>> {
        return orderDao.getAllOrders().map { entities ->
            entities.map { it.toDomainOrder() }
        }
    }

    fun getOrdersForUser(userId: String): Flow<List<Order>> {
        return orderDao.getOrdersForUser(userId).map { entities ->
            entities.map { it.toDomainOrder() }
        }
    }

    suspend fun placeOrder(
        userId: String,
        cartItems: Map<Product, Int>,
        address: UserAddress,
        deliveryDate: String,
        deliverySlot: String,
        isExpress: Boolean,
        paymentMethod: PaymentMethod,
        discountAmount: Double,
        customerName: String,
        customerPhone: String
    ): Order {
        val orderItems = cartItems.map { (prod, qty) ->
            OrderItem(
                productId = prod.id,
                productName = prod.name,
                unitPrice = prod.price,
                quantity = qty,
                unit = prod.unit,
                emoji = prod.iconEmoji
            )
        }

        val subtotal = cartItems.entries.sumOf { it.key.price * it.value }
        val deliveryFee = if (subtotal >= 1000.0) 0.0 else (if (isExpress) 120.0 else 60.0)
        val vatAmount = (subtotal - discountAmount).coerceAtLeast(0.0) * 0.13
        val totalAmount = (subtotal - discountAmount).coerceAtLeast(0.0) + deliveryFee + vatAmount

        val orderId = "LF-" + (100000..999999).random()
        val order = Order(
            orderId = orderId,
            userId = userId,
            items = orderItems,
            subtotal = subtotal,
            deliveryFee = deliveryFee,
            vatAmount = vatAmount,
            discountAmount = discountAmount,
            totalAmount = totalAmount,
            status = OrderStatus.PLACED,
            createdAt = System.currentTimeMillis(),
            deliveryDate = deliveryDate,
            deliverySlot = deliverySlot,
            isExpress = isExpress,
            paymentMethod = paymentMethod,
            deliveryAddress = "${address.streetAddress}, ${address.area} (Landmark: ${address.landmark})",
            customerPhone = customerPhone.ifBlank { address.phone },
            customerName = customerName.ifBlank { address.recipientName }
        )

        val entity = OrderEntity(
            orderId = order.orderId,
            userId = order.userId,
            itemsJson = orderItemAdapter.toJson(order.items) ?: "[]",
            subtotal = order.subtotal,
            deliveryFee = order.deliveryFee,
            vatAmount = order.vatAmount,
            discountAmount = order.discountAmount,
            totalAmount = order.totalAmount,
            status = order.status.name,
            createdAt = order.createdAt,
            deliveryDate = order.deliveryDate,
            deliverySlot = order.deliverySlot,
            isExpress = order.isExpress,
            paymentMethod = order.paymentMethod.id,
            deliveryAddress = order.deliveryAddress,
            customerPhone = order.customerPhone,
            customerName = order.customerName,
            riderName = order.riderName ?: "Bikash Tamang",
            riderPhone = order.riderPhone ?: "+977 9801234567"
        )

        orderDao.insertOrder(entity)
        clearCart()
        return order
    }

    suspend fun cancelOrder(orderId: String) {
        orderDao.cancelOrder(orderId)
    }

    suspend fun deleteOrder(orderId: String) {
        orderDao.deleteOrder(orderId)
    }

    suspend fun updateOrderStatus(orderId: String, status: OrderStatus) {
        orderDao.updateOrderStatus(orderId, status.name)
    }

    // Addresses Streams & Operations
    fun getAddressesForUser(userId: String): Flow<List<UserAddress>> {
        return addressDao.getAddressesForUser(userId).map { entities ->
            if (entities.isEmpty()) {
                // Populate sample address if empty
                SampleData.sampleAddresses
            } else {
                entities.map { it.toDomainAddress() }
            }
        }
    }

    suspend fun addAddress(address: UserAddress): Long {
        if (address.isDefault) {
            addressDao.clearDefaultAddress(address.userId)
        }
        return addressDao.insertAddress(
            AddressEntity(
                userId = address.userId,
                label = address.label,
                recipientName = address.recipientName,
                phone = address.phone,
                area = address.area,
                streetAddress = address.streetAddress,
                landmark = address.landmark,
                isDefault = address.isDefault
            )
        )
    }

    suspend fun deleteAddress(addressId: Long, userId: String) {
        addressDao.deleteAddress(
            AddressEntity(
                id = addressId,
                userId = userId,
                label = "",
                recipientName = "",
                phone = "",
                area = "",
                streetAddress = "",
                landmark = ""
            )
        )
    }

    suspend fun setDefaultAddress(addressId: Long, userId: String) {
        addressDao.clearDefaultAddress(userId)
        addressDao.setDefaultAddress(addressId)
    }

    // Favorites
    fun getFavorites(userId: String): Flow<List<String>> {
        return favoriteDao.getFavoritesForUser(userId)
    }

    suspend fun toggleFavorite(productId: String, userId: String, isFav: Boolean) {
        if (isFav) {
            favoriteDao.removeFavorite(productId, userId)
        } else {
            favoriteDao.addFavorite(FavoriteEntity(productId = productId, userId = userId))
        }
    }

    private fun OrderEntity.toDomainOrder(): Order {
        val parsedItems = try {
            orderItemAdapter.fromJson(this.itemsJson) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }

        val parsedStatus = try {
            OrderStatus.valueOf(this.status)
        } catch (e: Exception) {
            OrderStatus.PLACED
        }

        val parsedPayment = PaymentMethod.values().find { it.id == this.paymentMethod } ?: PaymentMethod.CASH_ON_DELIVERY

        return Order(
            orderId = this.orderId,
            userId = this.userId,
            items = parsedItems,
            subtotal = this.subtotal,
            deliveryFee = this.deliveryFee,
            vatAmount = this.vatAmount,
            discountAmount = this.discountAmount,
            totalAmount = this.totalAmount,
            status = parsedStatus,
            createdAt = this.createdAt,
            deliveryDate = this.deliveryDate,
            deliverySlot = this.deliverySlot,
            isExpress = this.isExpress,
            paymentMethod = parsedPayment,
            deliveryAddress = this.deliveryAddress,
            customerPhone = this.customerPhone,
            customerName = this.customerName,
            riderName = this.riderName,
            riderPhone = this.riderPhone
        )
    }

    private fun AddressEntity.toDomainAddress(): UserAddress {
        return UserAddress(
            id = this.id,
            userId = this.userId,
            label = this.label,
            recipientName = this.recipientName,
            phone = this.phone,
            area = this.area,
            streetAddress = this.streetAddress,
            landmark = this.landmark,
            isDefault = this.isDefault
        )
    }
}
