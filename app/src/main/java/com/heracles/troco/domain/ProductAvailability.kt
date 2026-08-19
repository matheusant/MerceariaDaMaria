package com.heracles.troco.domain

enum class ProductAvailability (
    val label: String,
    val isBuyable: Boolean
) {
    AVAILABLE("Disponível", true),
    OUT_OF_STOCK("Sem estoque", false),
    LAST_UNITS("Poucas unidades", true)
}