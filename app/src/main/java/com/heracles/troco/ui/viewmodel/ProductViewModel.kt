package com.heracles.troco.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.heracles.troco.domain.model.Product
import com.heracles.troco.domain.model.ProductStatus
import com.heracles.troco.domain.usecase.product.SaveProductUseCase
import com.heracles.troco.ui.screen.ProductUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject
import kotlin.text.ifEmpty

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val saveProductUseCase: SaveProductUseCase
): ViewModel() {
    private val _productName = MutableStateFlow("")
    private val _productPrice = MutableStateFlow("")
    private val _productQuantity = MutableStateFlow("")
    private val _isLoading = MutableStateFlow(false)
    private val _isAvailable = MutableStateFlow(false)

    val uiState = combine(
        _productName,
        _productPrice,
        _productQuantity,
        _isLoading,
        _isAvailable
    ) { name, price, quantity, isLoading, isAvailable ->
        ProductUiState(
            name = name,
            price = price,
            quantity = quantity,
            isLoading = isLoading,
            isAvailable = isAvailable
        )
    }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProductUiState()
        )

    fun onProdNameChange(value: String) {
        _productName.value = value
    }

    fun onProdPriceChange(value: String) {
        _productPrice.value = value.filter(Char::isDigit)
    }

    fun onProdQuantityChange(value: String) {
        _productQuantity.value = value
    }

    fun onAvailabilityChange(value: Boolean) {
        _isAvailable.value = value
    }

    fun addProduct() {
        viewModelScope.launch {
            val status = when(_isAvailable.value) {
                true -> {
                    if (_productQuantity.value.toInt() > 5) ProductStatus.ACTIVE else ProductStatus.CRITIC
                }
                false -> {
                    ProductStatus.INACTIVE
                }
            }
            val amount = BigDecimal(_productPrice.value.ifEmpty { "0" }).movePointLeft(2)
            saveProductUseCase(
                Product(
                    name = _productName.value,
                    price = amount.toString(),
                    quantity = _productQuantity.value.toInt(),
                    status = status
                )
            )
            _productName.value = ""
            _productPrice.value = ""
            _productQuantity.value = ""
            _isAvailable.value = false
        }
    }

}