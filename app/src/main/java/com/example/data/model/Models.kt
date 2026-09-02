package com.example.data.model

data class Product(
    val id: String,
    val name: String,
    val nepaliName: String,
    val price: Double,
    val originalPrice: Double? = null,
    val unit: String,
    val categoryId: String,
    val isOrganic: Boolean = true,
    val isLocalHarvest: Boolean = true,
    val originValley: String = "Kathmandu Valley",
    val rating: Double = 4.8,
    val reviewCount: Int = 34,
    val description: String,
    val nutritionalFacts: String,
    val inStock: Boolean = true,
    val isFlashDeal: Boolean = false,
    val iconEmoji: String = "🥬"
)

data class Category(
    val id: String,
    val name: String,
    val nepaliName: String,
    val iconName: String,
    val itemCount: Int,
    val emoji: String = "🌱"
)

enum class OrderStatus(val displayName: String, val step: Int) {
    PLACED("Order Placed", 1),
    CONFIRMED("Confirmed by Farm Hub", 2),
    PREPARING("Harvesting & Packing", 3),
    OUT_FOR_DELIVERY("Out for Delivery", 4),
    DELIVERED("Delivered", 5),
    CANCELLED("Cancelled", 0)
}

enum class PaymentMethod(val id: String, val title: String, val subtitle: String, val iconEmoji: String) {
    ESEWA("esewa", "eSewa Mobile Wallet", "Instant Nepal digital wallet", "🟢"),
    KHALTI("khalti", "Khalti Digital Wallet", "Pay via Khalti app/web", "🟣"),
    FONEPAY("fonepay", "Fonepay QR / Mobile Banking", "Scan QR from any Nepal bank", "🔴"),
    CASH_ON_DELIVERY("cod", "Cash on Delivery", "Pay cash or QR at doorstep", "💵"),
    CARD("card", "Debit / Credit Card", "Visa, Mastercard, SCT", "💳")
}

data class OrderItem(
    val productId: String,
    val productName: String,
    val unitPrice: Double,
    val quantity: Int,
    val unit: String,
    val emoji: String
)

data class Order(
    val orderId: String,
    val userId: String,
    val items: List<OrderItem>,
    val subtotal: Double,
    val deliveryFee: Double,
    val vatAmount: Double,
    val discountAmount: Double,
    val totalAmount: Double,
    val status: OrderStatus,
    val createdAt: Long,
    val deliveryDate: String,
    val deliverySlot: String,
    val isExpress: Boolean,
    val paymentMethod: PaymentMethod,
    val deliveryAddress: String,
    val customerPhone: String,
    val customerName: String,
    val riderName: String? = "Bikash Tamang",
    val riderPhone: String? = "+977 9801234567"
)

data class UserAddress(
    val id: Long = 0,
    val userId: String,
    val label: String, // Home, Work, Parents
    val recipientName: String,
    val phone: String,
    val area: String, // e.g., Jhamsikhel, Baluwatar, Baneshwor
    val streetAddress: String,
    val landmark: String,
    val isDefault: Boolean = false
)

data class UserProfile(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?,
    val isAnonymous: Boolean,
    val isEmailVerified: Boolean,
    val phoneNumber: String? = null,
    val loyaltyPoints: Int = 180,
    val memberTier: String = "Lekhali Gold Member"
)
