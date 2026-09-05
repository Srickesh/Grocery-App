package com.example

import androidx.test.core.app.ApplicationProvider
import com.example.data.model.SampleData
import com.example.ui.viewmodel.GroceryViewModel
import com.example.ui.viewmodel.SortOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
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
    fun testCategoriesInitialization() {
        val categories = viewModel.categories
        assertTrue("Categories should not be empty", categories.isNotEmpty())
    }

    @Test
    fun testSetCategoryFilter() {
        assertEquals("all", viewModel.selectedCategoryId.value)
        viewModel.setCategory("fruits_vegetables")
        assertEquals("fruits_vegetables", viewModel.selectedCategoryId.value)
    }

    @Test
    fun testSearchQueryFilter() {
        assertEquals("", viewModel.searchQuery.value)
        viewModel.setSearchQuery("Apple")
        assertEquals("Apple", viewModel.searchQuery.value)
    }

    @Test
    fun testToggleFilters() {
        assertFalse(viewModel.isOrganicOnly.value)
        viewModel.toggleOrganicOnly()
        assertTrue(viewModel.isOrganicOnly.value)

        assertFalse(viewModel.isLocalOnly.value)
        viewModel.toggleLocalOnly()
        assertTrue(viewModel.isLocalOnly.value)
    }

    @Test
    fun testSortOptionChange() {
        assertEquals(SortOption.POPULARITY, viewModel.sortOption.value)
        viewModel.setSortOption(SortOption.PRICE_LOW_HIGH)
        assertEquals(SortOption.PRICE_LOW_HIGH, viewModel.sortOption.value)
    }

    @Test
    fun testApplyValidCoupon() = runTest {
        var receivedMessage: String? = null
        val job = backgroundScope.launch(testDispatcher) {
            viewModel.userMessage.collect { receivedMessage = it }
        }

        assertNull(viewModel.appliedCoupon.value)
        viewModel.applyCoupon("FRESH20")
        assertEquals("FRESH20", viewModel.appliedCoupon.value)
        assertNotNull(receivedMessage)
        assertTrue(receivedMessage!!.contains("applied successfully"))

        viewModel.removeCoupon()
        assertNull(viewModel.appliedCoupon.value)
        job.cancel()
    }

    @Test
    fun testApplyInvalidCoupon() = runTest {
        var receivedMessage: String? = null
        val job = backgroundScope.launch(testDispatcher) {
            viewModel.userMessage.collect { receivedMessage = it }
        }

        viewModel.applyCoupon("INVALID_CODE_123")
        assertNull(viewModel.appliedCoupon.value)
        assertNotNull(receivedMessage)
        assertTrue(receivedMessage!!.contains("Invalid coupon code"))
        job.cancel()
    }

    @Test
    fun testAddToCartAndClearCart() = runTest {
        val product = SampleData.products.first()
        viewModel.addToCart(product, 2)
        viewModel.clearCart()
        assertEquals(0, viewModel.cartCount.value)
    }

    @Test
    fun testLocationSelection() {
        viewModel.setLocation("Pokhara, Kaski")
        assertEquals("Pokhara, Kaski", viewModel.currentLocation.value)
    }
}
