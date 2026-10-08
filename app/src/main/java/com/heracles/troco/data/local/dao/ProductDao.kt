package com.heracles.troco.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.heracles.troco.data.local.entity.ProductEntity
import com.heracles.troco.domain.model.ProductStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {

    @Query("SELECT * FROM products ORDER by name")
    fun getAllProducts(): Flow<List<ProductEntity>>

    @Insert
    suspend fun insertProduct(product: ProductEntity)

    @Query("DELETE FROM products WHERE id = :id")
    suspend fun deleteProduct(id: String)

    @Query(
        """
            UPDATE products 
            SET quantity = :quantity AND price = :price AND name = :name AND status = :status
            WHERE id = :id
        """)
    suspend fun updateProduct(id: String, name: String, price: String, quantity: Int, status: ProductStatus)
}