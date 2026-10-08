package com.heracles.troco.domain.repository

import com.heracles.troco.domain.model.Product
import kotlinx.coroutines.flow.Flow

interface ProductRepository {
    fun observeProducts(): Flow<List<Product>>

    suspend fun getProductById(id: String): Product

    suspend fun addProduct(product: Product)

    suspend fun updateProduct(product: Product)

    suspend fun deleteProduct(id: String)
}