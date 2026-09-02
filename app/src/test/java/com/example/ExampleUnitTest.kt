package com.example

import com.example.data.model.Category
import com.example.data.model.OrderStatus
import com.example.data.model.PaymentMethod
import com.example.data.model.Product
import com.example.data.model.SampleData
import org.junit.Assert.*
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun testAllCategoriesPresent() {
        val categories = SampleData.categories
        assertEquals(21, categories.size)
        
        val categoryIds = categories.map { it.id }.toSet()
        val expectedCategories = listOf(
            "all", "fruits_vegetables", "dairy_eggs", "bakery_bread", "rice_flour_grains",
            "pulses_beans_lentils", "meat_seafood", "canned_packaged", "snacks_biscuits", "beverages",
            "tea_coffee", "spices_seasonings", "oils_sauces", "sweets_chocolates", "frozen_foods",
            "household_cleaning", "personal_care", "baby_products", "pet_supplies",
            "household_kitchen", "paper_disposables"
        )
        for (cat in expectedCategories) {
            assertTrue("Expected category $cat to exist", categoryIds.contains(cat))
        }
    }

    @Test
    fun testProductCatalogIntegrity() {
        val products = SampleData.products
        assertTrue("Product list should not be empty", products.isNotEmpty())
        
        for (p in products) {
            assertNotNull(p.id)
            assertNotNull(p.name)
            assertTrue("Product price must be positive for ${p.name}", p.price > 0)
            if (p.originalPrice != null) {
                assertTrue("Product originalPrice must be >= price for ${p.name}", p.originalPrice >= p.price)
            }
            assertNotNull(p.unit)
            assertNotNull(p.categoryId)
            assertNotNull(p.description)
            assertNotNull(p.nutritionalFacts)
        }
    }

    @Test
    fun testProductCreation() {
        val prodWithDiscount = Product(
            id = "test1",
            name = "Organic Apples",
            nepaliName = "स्याउ",
            price = 200.0,
            originalPrice = 250.0,
            unit = "1 kg",
            categoryId = "fruits_vegetables",
            description = "Crisp organic apples from Mustang",
            nutritionalFacts = "Rich in fiber and Vitamin C",
            iconEmoji = "🍎"
        )
        val discount = if (prodWithDiscount.originalPrice != null) {
            ((prodWithDiscount.originalPrice - prodWithDiscount.price) / prodWithDiscount.originalPrice * 100).toInt()
        } else 0
        assertEquals(20, discount)

        val prodNoDiscount = Product(
            id = "test2",
            name = "Fresh Milk",
            nepaliName = "दूध",
            price = 100.0,
            originalPrice = null,
            unit = "1 L",
            categoryId = "dairy_eggs",
            description = "Pasteurized cow milk",
            nutritionalFacts = "Calcium & protein rich",
            iconEmoji = "🥛"
        )
        assertNull(prodNoDiscount.originalPrice)
    }

    @Test
    fun testCouponsValidation() {
        val coupons = SampleData.coupons
        assertTrue(coupons.containsKey("FRESH20"))
        assertEquals(0.20, coupons["FRESH20"] ?: 0.0, 0.001)

        assertTrue(coupons.containsKey("DASHAIN25"))
        assertEquals(0.25, coupons["DASHAIN25"] ?: 0.0, 0.001)

        assertTrue(coupons.containsKey("LEKHALI50"))
        assertEquals(50.0, coupons["LEKHALI50"] ?: 0.0, 0.001)
    }

    @Test
    fun testOrderStatusTransitions() {
        val statuses = OrderStatus.values()
        assertEquals(6, statuses.size)
        assertEquals("Order Placed", OrderStatus.PLACED.displayName)
        assertEquals("Delivered", OrderStatus.DELIVERED.displayName)
        assertEquals("Cancelled", OrderStatus.CANCELLED.displayName)
    }

    @Test
    fun testPaymentMethods() {
        val methods = PaymentMethod.values()
        assertTrue(methods.contains(PaymentMethod.CASH_ON_DELIVERY))
        assertTrue(methods.contains(PaymentMethod.ESEWA))
        assertTrue(methods.contains(PaymentMethod.KHALTI))
        assertTrue(methods.contains(PaymentMethod.FONEPAY))
    }
}
