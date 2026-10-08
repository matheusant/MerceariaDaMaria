package com.heracles.troco.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heracles.troco.domain.model.Product
import com.heracles.troco.domain.usecase.product.GetProductsUseCase
import com.heracles.troco.ui.screen.ProductsListUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

@HiltViewModel
@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class ProductsListViewModel @Inject constructor(
    private val getProductsUseCase: GetProductsUseCase
): ViewModel() {
    private val _isLoading = MutableStateFlow(false)

    private val _searchQuery = MutableStateFlow("")

    val searchResults: StateFlow<List<Product>> = _searchQuery
        .debounce(500.milliseconds)
        .distinctUntilChanged()
        .flatMapLatest { query ->
            getProductsUseCase.getAllProducts().map { products ->
                if (query.isBlank()){
                    products
                } else {
                    products.filter { it.name.contains(query, ignoreCase = true) }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val uiState = combine(
        _isLoading,
        _searchQuery,
        getProductsUseCase.getAllProducts()
    ) { loading, query, products ->
        ProductsListUiState(
            products = products,
            isLoading = loading,
            query = query
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ProductsListUiState()
    )

    fun onSearchQueryChange(value: String) {
        _searchQuery.value = value
    }
}