package com.heracles.troco.ui.screen

import com.heracles.troco.domain.model.Product

data class ProductsListUiState (
    val products: List<Product> = emptyList(),
    val isLoading: Boolean = false,
    val query: String = ""
)