package com.heracles.troco.data.repository

import com.heracles.troco.data.local.dao.ProductDao
import com.heracles.troco.data.local.entity.ProductEntity
import com.heracles.troco.domain.model.Product
import com.heracles.troco.domain.repository.ProductRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class ProductRepositoryImpl @Inject constructor(
    private val productDao: ProductDao
): ProductRepository {
    override fun observeProducts(): Flow<List<Product>> {
        return productDao.getAllProducts()
            .map { products ->
                products.map {
                    Product(
                        id = it.id,
                        name = it.name,
                        price = it.price,
                        quantity = it.quantity,
                        status = it.status
                    )
                }
            }
    }

    override suspend fun getProductById(id: String): Product {
        return productDao.getProductById(id)?.let { productEntity ->
            Product(
                id = productEntity.id,
                name = productEntity.name,
                price = productEntity.price,
                quantity = productEntity.quantity,
                status = productEntity.status
            )
        } ?: run {
            throw Exception("Produto não encontrado")
        }
    }

    override suspend fun addProduct(product: Product) {
        delay(2000.milliseconds)
        productDao.insertProduct(
            ProductEntity(
                name = product.name.trim(),
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