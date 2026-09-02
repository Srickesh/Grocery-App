package com.example.data.local

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// Entities
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

// DAOs
@Dao
interface CartDao {
    @Query("SELECT * FROM cart_items")
    fun getAllCartItems(): Flow<List<CartItemEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(item: CartItemEntity)

    @Query("DELETE FROM cart_items WHERE productId = :productId")
    suspend fun deleteCartItem(productId: String)

    @Query("DELETE FROM cart_items")
    suspend fun clearCart()
}

@Dao
interface AddressDao {
    @Query("SELECT * FROM user_addresses WHERE userId = :userId OR userId = 'default_user' ORDER BY isDefault DESC, id DESC")
    fun getAddressesForUser(userId: String): Flow<List<AddressEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAddress(address: AddressEntity): Long

    @Update
    suspend fun updateAddress(address: AddressEntity)

    @Delete
    suspend fun deleteAddress(address: AddressEntity)

    @Query("UPDATE user_addresses SET isDefault = 0 WHERE userId = :userId OR userId = 'default_user'")
    suspend fun clearDefaultAddress(userId: String)

    @Query("UPDATE user_addresses SET isDefault = 1 WHERE id = :id")
    suspend fun setDefaultAddress(id: Long)
}

@Dao
interface OrderDao {
    @Query("SELECT * FROM orders ORDER BY createdAt DESC")
    fun getAllOrders(): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE userId = :userId OR userId = 'default_user' ORDER BY createdAt DESC")
    fun getOrdersForUser(userId: String): Flow<List<OrderEntity>>

    @Query("SELECT * FROM orders WHERE orderId = :orderId")
    suspend fun getOrderById(orderId: String): OrderEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrder(order: OrderEntity)

    @Query("UPDATE orders SET status = :status WHERE orderId = :orderId")
    suspend fun updateOrderStatus(orderId: String, status: String)

    @Query("UPDATE orders SET status = 'CANCELLED' WHERE orderId = :orderId")
    suspend fun cancelOrder(orderId: String)

    @Query("DELETE FROM orders WHERE orderId = :orderId")
    suspend fun deleteOrder(orderId: String)
}

@Dao
interface FavoriteDao {
    @Query("SELECT productId FROM favorite_products WHERE userId = :userId OR userId = 'default_user'")
    fun getFavoritesForUser(userId: String): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addFavorite(favorite: FavoriteEntity)

    @Query("DELETE FROM favorite_products WHERE productId = :productId AND (userId = :userId OR userId = 'default_user')")
    suspend fun removeFavorite(productId: String, userId: String)
}

// Room Database
@Database(
    entities = [
        CartItemEntity::class,
        AddressEntity::class,
        OrderEntity::class,
        FavoriteEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun cartDao(): CartDao
    abstract fun addressDao(): AddressDao
    abstract fun orderDao(): OrderDao
    abstract fun favoriteDao(): FavoriteDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: android.content.Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = androidx.room.Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lekhali_fresh_db"
                ).fallbackToDestructiveMigration(dropAllTables = true).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
