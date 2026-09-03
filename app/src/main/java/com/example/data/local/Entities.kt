package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartItemEntity(
    @PrimaryKey val productId: String,
    val quantity: Int,
    val addedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "user_addresses")
data class AddressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val userId: String,
    val label: String,
    val recipientName: String,
    val phone: String,
    val area: String,
    val streetAddress: String,
    val landmark: String,
    val isDefault: Boolean = false
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderId: String,
    val userId: String,
    val itemsJson: String, // serialized OrderItem list
    val subtotal: Double,
    val deliveryFee: Double,
    val vatAmount: Double,
    val discountAmount: Double,
    val totalAmount: Double,
    val status: String, // PLACED, CONFIRMED, PREPARING, OUT_FOR_DELIVERY, DELIVERED, CANCELLED
    val createdAt: Long,
    val deliveryDate: String,
    val deliverySlot: String,
    val isExpress: Boolean,
    val paymentMethod: String,
    val deliveryAddress: String,
    val customerPhone: String,
    val customerName: String,
    val riderName: String,
    val riderPhone: String
)

@Entity(tableName = "favorite_products")
data class FavoriteEntity(
    @PrimaryKey val productId: String,
    val userId: String,
    val addedAt: Long = System.currentTimeMillis()
)
