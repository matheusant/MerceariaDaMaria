package com.heracles.troco.data.repository

import com.heracles.troco.data.local.dao.ProductDao
import com.heracles.troco.data.local.entity.ProductEntity
import com.heracles.troco.domain.model.Product
import com.heracles.troco.domain.repository.ProductRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao
): ProductRepository {
    override fun observeProducts(): Flow<List<Product>> {
        TODO("Not yet implemented")
    }

    override suspend fun addProduct(product: Product) {
        delay(2000.milliseconds)
        productDao.insertProduct(
            ProductEntity(
                name = product.name,
                price = product.price,
                quantity = product.quantity,
                status = product.status
            )
        )
    }

    override suspend fun updateProduct(product: Product) {
        productDao.updateProduct(
            id = product.id,
            name = product.name,
            price = product.price,
            quantity = product.quantity,
            status = product.status
        )
    }

    override suspend fun deleteProduct(id: String) {
        TODO("Not yet implemented")
    }
}