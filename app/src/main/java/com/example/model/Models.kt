package com.example.model

data class Product(
    val id: String,
    val name: String,
    val brand: String,
    val categoryId: String,
    val priceNrs: Int,
    val originalPriceNrs: Int? = null,
    val discountPercent: Int? = null,
    val unit: String,
    val rating: Double = 4.8,
    val reviewCount: Int = 120,
    val isExpress: Boolean = true,
    val isOrganic: Boolean = false,
    val isLocal: Boolean = true,
    val inStock: Boolean = true,
    val description: String,
    val deliveryInfo: String = "Express Delivery available in Kathmandu Valley. Order before 2 PM for same-day delivery.",
    val returnPolicy: String = "Easy 7-day return policy for sealed products. Quality assured.",
    val tags: List<String> = emptyList()
)

data class Category(
    val id: String,
    val name: String,
    val iconName: String,
    val itemCount: Int,
    val subtitle: String = "Sourced locally, delivered fresh."
)

data class Address(
    val id: Long = 0,
    val label: String, // "HOME", "OFFICE"
    val recipientName: String,
    val street: String,
    val area: String,
    val province: String,
    val postalCode: String,
    val phone: String,
    val isDefault: Boolean = false
)

data class DeliverySlot(
    val id: String,
    val dayTitle: String, // "Today", "Tomorrow", "Wednesday"
    val dateFormatted: String, // "12 Oct", "13 Oct", "14 Oct"
    val timeRange: String, // "8:00 AM - 10:00 AM"
    val isAvailable: Boolean = true
)

enum class OrderStatus(val title: String) {
    CONFIRMED("Confirmed"),
    PREPARING("Preparing Order"),
    PACKED("Packed"),
    OUT_FOR_DELIVERY("Out for Delivery"),
    DELIVERED("Delivered"),
    CANCELLED("Cancelled")
}

data class OrderItem(
    val productId: String,
    val productName: String,
    val priceNrs: Int,
    val quantity: Int,
    val unit: String,
    val categoryId: String = "veg"
)

data class Order(
    val orderNumber: String,
    val timestamp: Long,
    val dateFormatted: String,
    val status: OrderStatus,
    val items: List<OrderItem>,
    val subtotalNrs: Int,
    val deliveryFeeNrs: Int,
    val vatNrs: Int,
    val grandTotalNrs: Int,
    val paymentMethod: String,
    val deliveryAddress: String,
    val deliverySlot: String,
    val shopperName: String = "Suman",
    val shopperPhone: String = "+977 9841998877",
    val isExpress: Boolean = false
)

data class UserProfile(
    val name: String = "Bibek",
    val email: String = "bibek@email.com",
    val phone: String = "+977 9841234567",
    val loyaltyPoints: Int = 1250,
    val locationName: String = "Koteshwor, Kathmandu"
)
