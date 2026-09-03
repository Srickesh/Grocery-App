package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.ui.viewmodel.GroceryViewModel
import com.example.ui.viewmodel.SortOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
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
        val context = ApplicationProvider.getApplicationContext<Application>()
        viewModel = GroceryViewModel(context)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun testInitialStateAndCategories() = runTest {
        assertEquals("all", viewModel.selectedCategoryId.value)
        assertEquals("", viewModel.searchQuery.value)
        assertEquals(21, viewModel.categories.size)
    }

    @Test
    fun testCategoryFilter() = runTest {
        backgroundScope.launch { viewModel.filteredProducts.collect {} }
        advanceUntilIdle()

        viewModel.setCategory("fruits_vegetables")
        advanceUntilIdle()

        val filtered = viewModel.filteredProducts.value
        assertTrue(filtered.isNotEmpty())
        assertTrue(filtered.all { it.categoryId == "fruits_vegetables" })
    }

    @Test
    fun testSearchQueryFilter() = runTest {
        backgroundScope.launch { viewModel.filteredProducts.collect {} }
        advanceUntilIdle()

        viewModel.setSearchQuery("Apple")
        advanceUntilIdle()

        val filtered = viewModel.filteredProducts.value
        assertTrue(filtered.isNotEmpty())
        assertTrue(filtered.all {
            it.name.contains("Apple", ignoreCase = true) ||
            it.nepaliName.contains("Apple", ignoreCase = true) ||
            it.description.contains("Apple", ignoreCase = true) ||
            it.originValley.contains("Apple", ignoreCase = true)
        })
    }

    @Test
    fun testOrganicFilterToggle() = runTest {
        backgroundScope.launch { viewModel.filteredProducts.collect {} }
        advanceUntilIdle()

        viewModel.toggleOrganicOnly()
        advanceUntilIdle()

        assertTrue(viewModel.isOrganicOnly.value)
        val filtered = viewModel.filteredProducts.value
        assertTrue(filtered.all { it.isOrganic })
    }

    @Test
    fun testSortingLowToHigh() = runTest {
        backgroundScope.launch { viewModel.filteredProducts.collect {} }
        advanceUntilIdle()

        viewModel.setSortOption(SortOption.PRICE_LOW_HIGH)
        advanceUntilIdle()

        val filtered = viewModel.filteredProducts.value
        for (i in 0 until filtered.size - 1) {
            assertTrue(filtered[i].price <= filtered[i + 1].price)
        }
    }

    @Test
    fun testCouponApplication() = runTest {
        viewModel.applyCoupon("FRESH20")
        advanceUntilIdle()

        assertEquals("FRESH20", viewModel.appliedCoupon.value)

        viewModel.removeCoupon()
        advanceUntilIdle()

        assertNull(viewModel.appliedCoupon.value)
    }
}
