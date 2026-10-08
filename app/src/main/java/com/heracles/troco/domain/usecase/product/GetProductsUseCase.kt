package com.heracles.troco.domain.usecase.product

import com.heracles.troco.domain.repository.ProductRepository
import javax.inject.Inject

class GetProductsUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    fun getAllProducts() = productRepository.observeProducts()

    suspend fun getProductById(id: String) = productRepository.getProductById(id)
}