package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.SampleData
import com.example.ui.viewmodel.GroceryViewModel
import com.example.ui.viewmodel.SortOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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

    private val testDispatcher = StandardTestDispatcher()
    private lateinit var viewModel: GroceryViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        val app = ApplicationProvider.getApplicationContext<Application>()
        viewModel = GroceryViewModel(app)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialState() {
        assertEquals("all", viewModel.selectedCategoryId.value)
        assertEquals("", viewModel.searchQuery.value)
        assertFalse(viewModel.isOrganicOnly.value)
        assertFalse(viewModel.isLocalOnly.value)
        assertEquals(SortOption.POPULARITY, viewModel.sortOption.value)
        assertNull(viewModel.appliedCoupon.value)
    }

    @Test
    fun testSetCategoryAndSearchQuery() {
        viewModel.setCategory("fruits_vegetables")
        assertEquals("fruits_vegetables", viewModel.selectedCategoryId.value)

        viewModel.setSearchQuery("Apple")
        assertEquals("Apple", viewModel.searchQuery.value)
    }

    @Test
    fun testToggleFiltersAndSort() {
        viewModel.toggleOrganicOnly()
        assertTrue(viewModel.isOrganicOnly.value)

        viewModel.toggleLocalOnly()
        assertTrue(viewModel.isLocalOnly.value)

        viewModel.setSortOption(SortOption.PRICE_LOW_HIGH)
        assertEquals(SortOption.PRICE_LOW_HIGH, viewModel.sortOption.value)
    }

    @Test
    fun testCouponApplication() = runTest {
        viewModel.applyCoupon("FRESH20")
        assertEquals("FRESH20", viewModel.appliedCoupon.value)

        viewModel.removeCoupon()
        assertNull(viewModel.appliedCoupon.value)

        viewModel.applyCoupon("INVALID_CODE")
        assertNull(viewModel.appliedCoupon.value)
    }

    @Test
    fun testProductSelection() {
        val sampleProduct = SampleData.products.first()
        viewModel.selectProductForDetails(sampleProduct)
        assertEquals(sampleProduct, viewModel.selectedProductForDetails.value)

        viewModel.selectProductForDetails(null)
        assertNull(viewModel.selectedProductForDetails.value)
    }
}
