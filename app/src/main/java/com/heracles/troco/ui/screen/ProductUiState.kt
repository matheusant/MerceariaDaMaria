package com.heracles.troco.ui.screen

data class ProductErrors(
    var name: String = "",
    var price: String = "",
    var quantity: String = ""
)

data class ProductUiState(
    val name: String = "",
    val price: String = "",
    val quantity: String = "",
    val isAvailable: Boolean = false,
    val isLoading: Boolean = false,
    val errors: ProductErrors = ProductErrors()
)