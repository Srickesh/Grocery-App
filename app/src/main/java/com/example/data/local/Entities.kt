package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "cart_items")
data class CartEntity(
    @PrimaryKey val productId: String,
    val quantity: Int
)

@Entity(tableName = "saved_addresses")
data class AddressEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val label: String,
    val recipientName: String,
    val street: String,
    val area: String,
    val province: String,
    val postalCode: String,
    val phone: String,
    val isDefault: Boolean = false
)

@Entity(tableName = "orders")
data class OrderEntity(
    @PrimaryKey val orderNumber: String,
    val timestamp: Long,
    val dateFormatted: String,
    val status: String,
    val itemsJson: String,
    val subtotalNrs: Int,
    val deliveryFeeNrs: Int,
    val vatNrs: Int,
    val grandTotalNrs: Int,
    val paymentMethod: String,
    val deliveryAddress: String,
    val deliverySlot: String,
    val shopperName: String,
    val shopperPhone: String,
    val isExpress: Boolean
)
