package com.heracles.troco.domain.model

import java.util.UUID

enum class ProductStatus(
    val label: String
) {
    ACTIVE(label = "Ativo"),
    CRITIC(label = "Critíco"),
    INACTIVE(label = "Inativo");

    companion object {
        fun fromLabel(label: String): ProductStatus {
            return entries.first { it.label == label }
        }
    }
}

data class Product (
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val price: String,
    val quantity: Int,
    val status: ProductStatus
)