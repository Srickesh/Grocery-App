package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.data.model.SampleData
import com.example.ui.viewmodel.GroceryViewModel
import com.example.ui.viewmodel.SortOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
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

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var viewModel: GroceryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        viewModel = GroceryViewModel(ApplicationProvider.getApplicationContext())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `test initial products and categories loading`() {
        assertEquals("all", viewModel.selectedCategoryId.value)
        assertTrue(viewModel.allProducts.isNotEmpty())
        assertEquals(SampleData.categories.size, viewModel.categories.size)
    }

    @Test
    fun `test search and filtering products`() = runTest(testDispatcher) {
        val job = backgroundScope.launch { viewModel.filteredProducts.collect {} }

        viewModel.setSearchQuery("Apple")
        val filtered = viewModel.filteredProducts.value
        assertTrue(filtered.all { it.name.contains("Apple", ignoreCase = true) || it.description.contains("Apple", ignoreCase = true) })

        viewModel.setSearchQuery("")
        viewModel.setCategory("fruits_vegetables")
        val categoryFiltered = viewModel.filteredProducts.value
        assertTrue(categoryFiltered.all { it.categoryId == "fruits_vegetables" })
        job.cancel()
    }

    @Test
    fun `test sort options`() = runTest(testDispatcher) {
        val job = backgroundScope.launch { viewModel.filteredProducts.collect {} }

        viewModel.setSortOption(SortOption.PRICE_LOW_HIGH)
        val sortedAsc = viewModel.filteredProducts.value
        val isAscending = sortedAsc.zipWithNext().all { (a, b) -> a.price <= b.price }
        assertTrue(isAscending)

        viewModel.setSortOption(SortOption.PRICE_HIGH_LOW)
        val sortedDesc = viewModel.filteredProducts.value
        val isDescending = sortedDesc.zipWithNext().all { (a, b) -> a.price >= b.price }
        assertTrue(isDescending)
        job.cancel()
    }

    @Test
    fun `test coupon application and cart calculations`() = runTest(testDispatcher) {
        val job1 = backgroundScope.launch { viewModel.cartSubtotal.collect {} }
        val job2 = backgroundScope.launch { viewModel.discountAmount.collect {} }
        val job3 = backgroundScope.launch { viewModel.cartVat.collect {} }
        val job4 = backgroundScope.launch { viewModel.cartDeliveryFee.collect {} }
        val job5 = backgroundScope.launch { viewModel.cartTotal.collect {} }
        val job6 = backgroundScope.launch { viewModel.cartItems.collect {} }

        val sampleProduct = viewModel.allProducts.first()

        viewModel.addToCart(sampleProduct, 2)
        val expectedSubtotal = sampleProduct.price * 2

        val cartSubtotalVal = viewModel.cartSubtotal.first { it == expectedSubtotal }
        assertEquals(expectedSubtotal, cartSubtotalVal, 0.01)

        viewModel.applyCoupon("FRESH20")
        assertEquals("FRESH20", viewModel.appliedCoupon.value)
        val expectedDiscount = expectedSubtotal * 0.20
        assertEquals(expectedDiscount, viewModel.discountAmount.first { it == expectedDiscount }, 0.01)

        val expectedVat = (expectedSubtotal - expectedDiscount) * 0.13
        assertEquals(expectedVat, viewModel.cartVat.first { it == expectedVat }, 0.01)

        val expectedDeliveryFee = if (expectedSubtotal >= 1000.0) 0.0 else 60.0
        assertEquals(expectedDeliveryFee, viewModel.cartDeliveryFee.first { it == expectedDeliveryFee }, 0.01)

        val expectedTotal = (expectedSubtotal - expectedDiscount) + expectedVat + expectedDeliveryFee
        assertEquals(expectedTotal, viewModel.cartTotal.first { it == expectedTotal }, 0.01)

        viewModel.removeCoupon()
        assertNull(viewModel.appliedCoupon.value)
        assertEquals(0.0, viewModel.discountAmount.first { it == 0.0 }, 0.01)

        job1.cancel()
        job2.cancel()
        job3.cancel()
        job4.cancel()
        job5.cancel()
        job6.cancel()
    }

    @Test
    fun `test toggle organic and local filter flags`() {
        assertFalse(viewModel.isOrganicOnly.value)
        viewModel.toggleOrganicOnly()
        assertTrue(viewModel.isOrganicOnly.value)

        assertFalse(viewModel.isLocalOnly.value)
        viewModel.toggleLocalOnly()
        assertTrue(viewModel.isLocalOnly.value)
    }
}
