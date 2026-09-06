package com.example

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.SampleData
import com.example.ui.viewmodel.GroceryViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GroceryViewModelTest {

    private lateinit var viewModel: GroceryViewModel

    @Before
    fun setUp() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        viewModel = GroceryViewModel(app)
    }

    @Test
    fun testInitialState() {
        assertNotNull(viewModel.categories)
        assertTrue(viewModel.categories.isNotEmpty())
        assertEquals("all", viewModel.selectedCategoryId.value)
        assertEquals("", viewModel.searchQuery.value)
        assertEquals("Jhamsikhel, Lalitpur", viewModel.currentLocation.value)
        assertNull(viewModel.appliedCoupon.value)
    }

    @Test
    fun testSetCategory() {
        viewModel.setCategory("fruits_vegetables")
        assertEquals("fruits_vegetables", viewModel.selectedCategoryId.value)
    }

    @Test
    fun testSetSearchQuery() {
        viewModel.setSearchQuery("Apple")
        assertEquals("Apple", viewModel.searchQuery.value)
    }

    @Test
    fun testToggleOrganicAndLocal() {
        assertEquals(false, viewModel.isOrganicOnly.value)
        viewModel.toggleOrganicOnly()
        assertEquals(true, viewModel.isOrganicOnly.value)

        assertEquals(false, viewModel.isLocalOnly.value)
        viewModel.toggleLocalOnly()
        assertEquals(true, viewModel.isLocalOnly.value)
    }

    @Test
    fun testApplyAndRemoveCoupon() {
        viewModel.applyCoupon("FRESH20")
        assertEquals("FRESH20", viewModel.appliedCoupon.value)

        viewModel.removeCoupon()
        assertNull(viewModel.appliedCoupon.value)
    }

    @Test
    fun testSetLocation() {
        viewModel.setLocation("Thamel, Kathmandu")
        assertEquals("Thamel, Kathmandu", viewModel.currentLocation.value)
    }

    @Test
    fun testSelectProductForDetails() {
        val sampleProduct = SampleData.products.first()
        viewModel.selectProductForDetails(sampleProduct)
        assertEquals(sampleProduct, viewModel.selectedProductForDetails.value)

        viewModel.selectProductForDetails(null)
        assertNull(viewModel.selectedProductForDetails.value)
    }
}
