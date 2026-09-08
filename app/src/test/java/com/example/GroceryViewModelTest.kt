package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.data.model.UserAddress
import com.example.ui.viewmodel.GroceryViewModel
import com.example.ui.viewmodel.SortOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GroceryViewModelTest {

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: GroceryViewModel

    private suspend fun waitUntil(timeoutMs: Long = 3000, condition: () -> Boolean) {
        val start = System.currentTimeMillis()
        while (!condition()) {
            if (System.currentTimeMillis() - start > timeoutMs) {
                throw AssertionError("Condition not met within $timeoutMs ms")
            }
            delay(50)
        }
    }

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val application = ApplicationProvider.getApplicationContext<Application>()
        viewModel = GroceryViewModel(application)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialCategoriesAndProducts() {
        assertFalse(viewModel.categories.isEmpty())
        assertTrue(viewModel.allProducts.isNotEmpty())
    }

    @Test
    fun testCategoryFiltering() = runTest {
        backgroundScope.launch { viewModel.filteredProducts.collect {} }

        viewModel.setCategory("fruits_vegetables")
        advanceUntilIdle()

        val filtered = viewModel.filteredProducts.value
        assertTrue(filtered.isNotEmpty())
        assertTrue(filtered.all { it.categoryId == "fruits_vegetables" })
    }

    @Test
    fun testSearchQueryFiltering() = runTest {
        backgroundScope.launch { viewModel.filteredProducts.collect {} }

        viewModel.setSearchQuery("Apples")
        advanceUntilIdle()

        val filtered = viewModel.filteredProducts.value
        assertTrue(filtered.isNotEmpty())
        assertTrue(filtered.all { it.name.contains("Apples", ignoreCase = true) })
    }

    @Test
    fun testOrganicFilter() = runTest {
        backgroundScope.launch { viewModel.filteredProducts.collect {} }

        viewModel.toggleOrganicOnly()
        advanceUntilIdle()

        val filtered = viewModel.filteredProducts.value
        assertTrue(filtered.isNotEmpty())
        assertTrue(filtered.all { it.isOrganic })
    }

    @Test
    fun testSorting() = runTest {
        backgroundScope.launch { viewModel.filteredProducts.collect {} }

        viewModel.setSortOption(SortOption.PRICE_LOW_HIGH)
        advanceUntilIdle()

        val sortedLowToHigh = viewModel.filteredProducts.value
        for (i in 0 until sortedLowToHigh.size - 1) {
            assertTrue(sortedLowToHigh[i].price <= sortedLowToHigh[i + 1].price)
        }

        viewModel.setSortOption(SortOption.PRICE_HIGH_LOW)
        advanceUntilIdle()

        val sortedHighToLow = viewModel.filteredProducts.value
        for (i in 0 until sortedHighToLow.size - 1) {
            assertTrue(sortedHighToLow[i].price >= sortedHighToLow[i + 1].price)
        }
    }

    @Test
    fun testCouponApplicationAndRemoval() = runTest {
        backgroundScope.launch { viewModel.activeCoupons.collect {} }
        backgroundScope.launch { viewModel.appliedCoupon.collect {} }

        viewModel.applyCoupon("FRESH20")
        advanceUntilIdle()
        assertEquals("FRESH20", viewModel.appliedCoupon.value)

        viewModel.removeCoupon()
        advanceUntilIdle()
        assertNull(viewModel.appliedCoupon.value)
    }

    @Test
    fun testInvalidCouponApplication() = runTest {
        backgroundScope.launch { viewModel.activeCoupons.collect {} }
        backgroundScope.launch { viewModel.appliedCoupon.collect {} }

        viewModel.applyCoupon("INVALID_CODE")
        advanceUntilIdle()
        assertNull(viewModel.appliedCoupon.value)
    }

    @Test
    fun testCartOperations() = runTest {
        backgroundScope.launch { viewModel.cartItems.collect {} }

        val product = viewModel.allProducts.first()

        viewModel.addToCart(product, 2)
        waitUntil { viewModel.cartItems.value[product] == 2 }
        assertEquals(2, viewModel.cartItems.value[product])

        viewModel.updateCartQuantity(product, 5)
        waitUntil { viewModel.cartItems.value[product] == 5 }
        assertEquals(5, viewModel.cartItems.value[product])

        viewModel.removeFromCart(product)
        waitUntil { viewModel.cartItems.value[product] == null }
        assertNull(viewModel.cartItems.value[product])

        viewModel.addToCart(product, 1)
        waitUntil { viewModel.cartItems.value[product] == 1 }
        viewModel.clearCart()
        waitUntil { viewModel.cartItems.value.isEmpty() }
        assertTrue(viewModel.cartItems.value.isEmpty())
    }

    @Test
    fun testCartCalculationsWithPercentageCoupon() = runTest {
        backgroundScope.launch { viewModel.cartItems.collect {} }
        backgroundScope.launch { viewModel.cartSubtotal.collect {} }
        backgroundScope.launch { viewModel.discountAmount.collect {} }
        backgroundScope.launch { viewModel.cartVat.collect {} }
        backgroundScope.launch { viewModel.cartDeliveryFee.collect {} }
        backgroundScope.launch { viewModel.cartTotal.collect {} }
        backgroundScope.launch { viewModel.activeCoupons.collect {} }

        val product = viewModel.allProducts.find { it.id == "prod_spinach" }!! // Price 95.0
        viewModel.addToCart(product, 2) // Subtotal = 190.0
        waitUntil { viewModel.cartItems.value[product] == 2 }

        viewModel.applyCoupon("FRESH20") // 20% discount = 38.0
        advanceUntilIdle()

        assertEquals(190.0, viewModel.cartSubtotal.value, 0.01)
        assertEquals(38.0, viewModel.discountAmount.value, 0.01)

        val expectedVat = (190.0 - 38.0) * 0.13 // 19.76
        assertEquals(expectedVat, viewModel.cartVat.value, 0.01)

        val expectedDeliveryFee = 60.0 // Subtotal < 1000
        assertEquals(expectedDeliveryFee, viewModel.cartDeliveryFee.value, 0.01)

        val expectedTotal = (190.0 - 38.0) + expectedDeliveryFee + expectedVat
        assertEquals(expectedTotal, viewModel.cartTotal.value, 0.01)
    }

    @Test
    fun testCartCalculationsWithFlatCoupon() = runTest {
        backgroundScope.launch { viewModel.cartItems.collect {} }
        backgroundScope.launch { viewModel.cartSubtotal.collect {} }
        backgroundScope.launch { viewModel.discountAmount.collect {} }
        backgroundScope.launch { viewModel.activeCoupons.collect {} }

        val product = viewModel.allProducts.find { it.id == "prod_spinach" }!! // Price 95.0
        viewModel.addToCart(product, 2) // Subtotal = 190.0
        waitUntil { viewModel.cartItems.value[product] == 2 }

        viewModel.applyCoupon("LEKHALI50") // Flat 50 discount
        advanceUntilIdle()

        assertEquals(50.0, viewModel.discountAmount.value, 0.01)
    }

    @Test
    fun testOrderPlacementAndTracking() = runTest {
        backgroundScope.launch { viewModel.cartItems.collect {} }
        backgroundScope.launch { viewModel.selectedOrderForTracking.collect {} }

        val product = viewModel.allProducts.first()
        viewModel.addToCart(product, 1)
        waitUntil { viewModel.cartItems.value[product] == 1 }

        val address = UserAddress(
            id = 1,
            userId = "default_user",
            label = "Home",
            recipientName = "John Doe",
            phone = "9800000000",
            area = "Kathmandu",
            streetAddress = "Main St",
            landmark = "Park",
            isDefault = true
        )

        var placedOrder: Order? = null
        viewModel.placeOrder(
            userId = "default_user",
            address = address,
            deliveryDate = "Today",
            deliverySlot = "10 AM - 12 PM",
            isExpress = false,
            paymentMethod = PaymentMethod.CASH_ON_DELIVERY,
            customerName = "John Doe",
            customerPhone = "9800000000",
            onSuccess = { order -> placedOrder = order }
        )
        waitUntil { placedOrder != null }

        assertNotNull(placedOrder)
        assertEquals(OrderStatus.PLACED, placedOrder?.status)
        assertEquals(placedOrder, viewModel.selectedOrderForTracking.value)
        waitUntil { viewModel.cartItems.value.isEmpty() }
        assertTrue(viewModel.cartItems.value.isEmpty())

        // Test advance order status simulation
        viewModel.advanceOrderSimulation(placedOrder!!.orderId)
        waitUntil { viewModel.selectedOrderForTracking.value?.status == OrderStatus.CONFIRMED }
        assertEquals(OrderStatus.CONFIRMED, viewModel.selectedOrderForTracking.value?.status)

        // Test cancel order
        viewModel.cancelOrder(placedOrder!!.orderId)
        waitUntil { viewModel.selectedOrderForTracking.value?.status == OrderStatus.CANCELLED }
        assertEquals(OrderStatus.CANCELLED, viewModel.selectedOrderForTracking.value?.status)
    }

    @Test
    fun testAdminProductManagement() = runTest {
        backgroundScope.launch { viewModel.allProductsFlow.collect {} }

        val newProduct = Product(
            id = "test_admin_prod",
            name = "Test Admin Apple",
            nepaliName = "टेस्ट स्याउ",
            price = 100.0,
            unit = "1 kg",
            categoryId = "fruits_vegetables",
            description = "Test product",
            nutritionalFacts = "Test facts"
        )

        viewModel.adminAddProduct(newProduct)
        advanceUntilIdle()
        assertNotNull(viewModel.allProducts.find { it.id == "test_admin_prod" })

        val updatedProduct = newProduct.copy(price = 120.0)
        viewModel.adminUpdateProduct(updatedProduct)
        advanceUntilIdle()
        assertEquals(120.0, viewModel.allProducts.find { it.id == "test_admin_prod" }?.price)

        viewModel.adminDeleteProduct("test_admin_prod")
        advanceUntilIdle()
        assertNull(viewModel.allProducts.find { it.id == "test_admin_prod" })
    }

    @Test
    fun testAdminCouponManagement() = runTest {
        backgroundScope.launch { viewModel.activeCoupons.collect {} }

        viewModel.adminAddCoupon("SUPER100", 100.0)
        advanceUntilIdle()
        assertTrue(viewModel.activeCoupons.value.containsKey("SUPER100"))
        assertEquals(100.0, viewModel.activeCoupons.value["SUPER100"] ?: 0.0, 0.001)

        viewModel.adminDeleteCoupon("SUPER100")
        advanceUntilIdle()
        assertFalse(viewModel.activeCoupons.value.containsKey("SUPER100"))
    }
}
