package com.heracles.troco.domain.model

import androidx.compose.ui.graphics.Color
import com.heracles.troco.domain.ProductAvailability
import java.util.UUID

data class CatalogProduct(
    val id: String = UUID.randomUUID().toString(),
    val bg: Color,
    val name: String,
    val price: String,
    val status: ProductAvailability
)
