package com.heracles.troco.domain.model

import java.util.UUID

enum class ProductStatus(
    val label: String
) {
    ACTIVE(label = "Ativo"),
    CRITIC(label = "Critíco"),
    INACTIVE(label = "Inativo")
}

data class Product (
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val price: String,
    val quantity: Int,
    val status: ProductStatus
)


data class Products (
    val products: List<Product> = emptyList()
)