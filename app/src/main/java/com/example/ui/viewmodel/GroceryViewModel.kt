package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Category
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.data.model.SampleData
import com.example.data.model.UserAddress
import com.example.data.repository.GroceryRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class SortOption(val title: String) {
    POPULARITY("Featured / Popular"),
    PRICE_LOW_HIGH("Price: Low to High"),
    PRICE_HIGH_LOW("Price: High to Low"),
    RATING("Highest Rated")
}

class GroceryViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = GroceryRepository(application.applicationContext)

    val categories: List<Category> = repository.allCategories
    val allProductsFlow: StateFlow<List<Product>> = repository.productsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.allProducts)

    val allProducts: List<Product>
        get() = repository.allProducts

    val activeCoupons: StateFlow<Map<String, Double>> = repository.couponsFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.getCoupons())

    private val _selectedCategoryId = MutableStateFlow("all")
    val selectedCategoryId: StateFlow<String> = _selectedCategoryId.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isOrganicOnly = MutableStateFlow(false)
    val isOrganicOnly: StateFlow<Boolean> = _isOrganicOnly.asStateFlow()

    private val _isLocalOnly = MutableStateFlow(false)
    val isLocalOnly: StateFlow<Boolean> = _isLocalOnly.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.POPULARITY)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val _currentLocation = MutableStateFlow("Jhamsikhel, Lalitpur")
    val currentLocation: StateFlow<String> = _currentLocation.asStateFlow()

    private val _appliedCoupon = MutableStateFlow<String?>(null)
    val appliedCoupon: StateFlow<String?> = _appliedCoupon.asStateFlow()

    val discountAmount: StateFlow<Double> = combine(_appliedCoupon, activeCoupons, cartSubtotal) { couponCode, coupons, subtotal ->
        if (couponCode == null || subtotal == 0.0) 0.0
        else {
            val discountVal = coupons[couponCode] ?: 0.0
            if (discountVal <= 1.0) {
                subtotal * discountVal
            } else {
                discountVal.coerceAtMost(subtotal)
            }
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    private val _selectedProductForDetails = MutableStateFlow<Product?>(null)
    val selectedProductForDetails: StateFlow<Product?> = _selectedProductForDetails.asStateFlow()

    private val _selectedOrderForTracking = MutableStateFlow<Order?>(null)
    val selectedOrderForTracking: StateFlow<Order?> = _selectedOrderForTracking.asStateFlow()

    private val _activeUserId = MutableStateFlow("default_user")

    private val _userMessage = MutableSharedFlow<String>()
    val userMessage: SharedFlow<String> = _userMessage.asSharedFlow()

    // Filtered products list (reactive to repository.productsFlow)
    val filteredProducts: StateFlow<List<Product>> = combine(
        repository.productsFlow,
        _selectedCategoryId,
        _searchQuery,
        _isOrganicOnly,
        _isLocalOnly
    ) { products, catId, query, organicOnly, localOnly ->
        var list = products

        if (catId != "all") {
            list = list.filter { it.categoryId == catId }
        }

        if (query.isNotBlank()) {
            list = list.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.nepaliName.contains(query, ignoreCase = true) ||
                it.description.contains(query, ignoreCase = true) ||
                it.originValley.contains(query, ignoreCase = true)
            }
        }

        if (organicOnly) {
            list = list.filter { it.isOrganic }
        }

        if (localOnly) {
            list = list.filter { it.isLocalHarvest }
        }

        list
    }.combine(_sortOption) { list, sort ->
        when (sort) {
            SortOption.PRICE_LOW_HIGH -> list.sortedBy { it.price }
            SortOption.PRICE_HIGH_LOW -> list.sortedByDescending { it.price }
            SortOption.RATING -> list.sortedByDescending { it.rating }
            SortOption.POPULARITY -> list
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), repository.allProducts)

    // Cart state from Room
    val cartItems: StateFlow<Map<Product, Int>> = repository.getCartItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyMap())

    val cartCount: StateFlow<Int> = cartItems.combineTransform { items ->
        items.values.sum()
    }

    val cartSubtotal: StateFlow<Double> = cartItems.combineTransform { items ->
        items.entries.sumOf { it.key.price * it.value }
    }

    val cartVat: StateFlow<Double> = combine(cartSubtotal, _discountAmount) { subtotal, discount ->
        (subtotal - discount).coerceAtLeast(0.0) * 0.13
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    val cartDeliveryFee: StateFlow<Double> = cartSubtotal.combineTransform { subtotal ->
        if (subtotal == 0.0 || subtotal >= 1000.0) 0.0 else 60.0
    }

    val cartTotal: StateFlow<Double> = combine(cartSubtotal, _discountAmount, cartDeliveryFee, cartVat) { sub, disc, delivery, vat ->
        if (sub == 0.0) 0.0 else (sub - disc).coerceAtLeast(0.0) + delivery + vat
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0.0)

    // User Orders from Room
    val userOrders: StateFlow<List<Order>> = repository.getOrdersForUser(_activeUserId.value)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // All Store Orders for Admin Panel
    val allStoreOrders: StateFlow<List<Order>> = repository.getAllOrders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Saved delivery addresses from Room
    val userAddresses: StateFlow<List<UserAddress>> = repository.getAddressesForUser(_activeUserId.value)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SampleData.sampleAddresses)

    // Favorites
    val favoriteProductIds: StateFlow<List<String>> = repository.getFavorites(_activeUserId.value)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun setUserId(uid: String) {
        _activeUserId.value = uid
    }

    fun setCategory(categoryId: String) {
        _selectedCategoryId.value = categoryId
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun toggleOrganicOnly() {
        _isOrganicOnly.value = !_isOrganicOnly.value
    }

    fun toggleLocalOnly() {
        _isLocalOnly.value = !_isLocalOnly.value
    }

    fun setSortOption(option: SortOption) {
        _sortOption.value = option
    }

    fun setLocation(location: String) {
        _currentLocation.value = location
    }

    fun selectProductForDetails(product: Product?) {
        _selectedProductForDetails.value = product
    }

    fun selectOrderForTracking(order: Order?) {
        _selectedOrderForTracking.value = order
    }

    // Cart actions
    fun addToCart(product: Product, quantityToAdd: Int = 1) {
        viewModelScope.launch {
            val currentQty = cartItems.value[product] ?: 0
            repository.addToCart(product.id, currentQty + quantityToAdd)
            _userMessage.emit("Added ${product.name} to cart 🥬")
        }
    }

    fun updateCartQuantity(product: Product, newQuantity: Int) {
        viewModelScope.launch {
            repository.updateCartQuantity(product.id, newQuantity)
        }
    }

    fun removeFromCart(product: Product) {
        viewModelScope.launch {
            repository.removeFromCart(product.id)
            _userMessage.emit("Removed ${product.name} from basket")
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            repository.clearCart()
        }
    }

    // Coupon engine
    fun applyCoupon(code: String) {
        val uppercase = code.trim().uppercase()
        if (uppercase.isBlank()) return

        val couponsMap = activeCoupons.value
        if (couponsMap.containsKey(uppercase)) {
            _appliedCoupon.value = uppercase
            viewModelScope.launch {
                _userMessage.emit("Coupon '$uppercase' applied successfully! 🎉")
            }
        } else {
            viewModelScope.launch {
                _userMessage.emit("Invalid coupon code. Try 'FRESH20' or 'DASHAIN25'")
            }
        }
    }

    fun removeCoupon() {
        _appliedCoupon.value = null
    }

    // Order actions
    fun placeOrder(
        userId: String,
        address: UserAddress,
        deliveryDate: String,
        deliverySlot: String,
        isExpress: Boolean,
        paymentMethod: PaymentMethod,
        customerName: String,
        customerPhone: String,
        onSuccess: (Order) -> Unit
    ) {
        if (cartItems.value.isEmpty()) return

        viewModelScope.launch {
            val order = repository.placeOrder(
                userId = userId,
                cartItems = cartItems.value,
                address = address,
                deliveryDate = deliveryDate,
                deliverySlot = deliverySlot,
                isExpress = isExpress,
                paymentMethod = paymentMethod,
                discountAmount = discountAmount.value,
                customerName = customerName,
                customerPhone = customerPhone
            )
            removeCoupon()
            _selectedOrderForTracking.value = order
            _userMessage.emit("Order placed successfully! Order ID: ${order.orderId} 🛵")
            onSuccess(order)
        }
    }

    fun reorderAll(order: Order) {
        viewModelScope.launch {
            order.items.forEach { item ->
                repository.addToCart(item.productId, item.quantity)
            }
            _userMessage.emit("All items from ${order.orderId} added to your basket! 🛒")
        }
    }

    fun cancelOrder(orderId: String) {
        viewModelScope.launch {
            repository.cancelOrder(orderId)
            _userMessage.emit("Order $orderId has been cancelled.")
            if (_selectedOrderForTracking.value?.orderId == orderId) {
                _selectedOrderForTracking.value = _selectedOrderForTracking.value?.copy(status = OrderStatus.CANCELLED)
            }
        }
    }

    fun advanceOrderSimulation(orderId: String) {
        viewModelScope.launch {
            val current = _selectedOrderForTracking.value
            if (current != null && current.orderId == orderId) {
                val nextStatus = when (current.status) {
                    OrderStatus.PLACED -> OrderStatus.CONFIRMED
                    OrderStatus.CONFIRMED -> OrderStatus.PREPARING
                    OrderStatus.PREPARING -> OrderStatus.OUT_FOR_DELIVERY
                    OrderStatus.OUT_FOR_DELIVERY -> OrderStatus.DELIVERED
                    OrderStatus.DELIVERED -> OrderStatus.DELIVERED
                    OrderStatus.CANCELLED -> OrderStatus.CANCELLED
                }
                repository.updateOrderStatus(orderId, nextStatus)
                _selectedOrderForTracking.value = current.copy(status = nextStatus)
                _userMessage.emit("Order status updated: ${nextStatus.displayName}")
            }
        }
    }

    // Address actions
    fun addAddress(address: UserAddress) {
        viewModelScope.launch {
            repository.addAddress(address)
            _userMessage.emit("New delivery address saved!")
        }
    }

    fun deleteAddress(addressId: Long) {
        viewModelScope.launch {
            repository.deleteAddress(addressId, _activeUserId.value)
            _userMessage.emit("Delivery address removed.")
        }
    }

    fun setDefaultAddress(addressId: Long) {
        viewModelScope.launch {
            repository.setDefaultAddress(addressId, _activeUserId.value)
            _userMessage.emit("Default delivery address updated.")
        }
    }

    // Favorites
    fun toggleFavorite(productId: String) {
        val isFav = favoriteProductIds.value.contains(productId)
        viewModelScope.launch {
            repository.toggleFavorite(productId, _activeUserId.value, isFav)
            _userMessage.emit(if (isFav) "Removed from favorites" else "Saved to favorites ❤️")
        }
    }

    // ================= ADMIN ACTIONS ================= //

    fun adminUpdateOrderStatus(orderId: String, newStatus: OrderStatus) {
        viewModelScope.launch {
            repository.updateOrderStatus(orderId, newStatus)
            if (_selectedOrderForTracking.value?.orderId == orderId) {
                _selectedOrderForTracking.value = _selectedOrderForTracking.value?.copy(status = newStatus)
            }
            _userMessage.emit("Order $orderId status changed to ${newStatus.displayName}")
        }
    }

    fun adminDeleteOrder(orderId: String) {
        viewModelScope.launch {
            repository.deleteOrder(orderId)
            _userMessage.emit("Order $orderId deleted by Admin")
        }
    }

    fun adminAddProduct(product: Product) {
        viewModelScope.launch {
            repository.addProduct(product)
            _userMessage.emit("Product '${product.name}' added to store catalog! 🌿")
        }
    }

    fun adminUpdateProduct(product: Product) {
        viewModelScope.launch {
            repository.updateProduct(product)
            _userMessage.emit("Product '${product.name}' updated successfully!")
        }
    }

    fun adminDeleteProduct(productId: String) {
        viewModelScope.launch {
            val prod = repository.getProductById(productId)
            repository.deleteProduct(productId)
            _userMessage.emit("Product '${prod?.name ?: productId}' removed from store.")
        }
    }

    fun adminAddCoupon(code: String, discountVal: Double) {
        viewModelScope.launch {
            repository.addCoupon(code, discountVal)
            _userMessage.emit("Promo Code '$code' is now ACTIVE!")
        }
    }

    fun adminDeleteCoupon(code: String) {
        viewModelScope.launch {
            repository.removeCoupon(code)
            _userMessage.emit("Coupon '$code' removed.")
        }
    }

    private fun <T, R> StateFlow<T>.combineTransform(transform: (T) -> R): StateFlow<R> {
        return this.map { transform(it) }.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            transform(this.value)
        )
    }
}
