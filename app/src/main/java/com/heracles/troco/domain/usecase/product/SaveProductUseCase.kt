package com.heracles.troco.domain.usecase.product

import com.heracles.troco.domain.model.Product
import com.heracles.troco.domain.repository.ProductRepository
import javax.inject.Inject

class SaveProductUseCase @Inject constructor(
    private val productRepository: ProductRepository
) {
    suspend operator fun invoke(product: Product) = productRepository.addProduct(product)
}