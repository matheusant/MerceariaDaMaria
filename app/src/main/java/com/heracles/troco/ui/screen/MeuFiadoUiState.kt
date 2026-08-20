package com.heracles.troco.ui.screen

import com.heracles.troco.domain.model.Transaction

data class MeuFiadoUiState(
    val balance: String = "R$ 0,00",
    val dueDate: String? = null,
    val transactions: List<Transaction> = emptyList(),
    val isLoading: Boolean = false
) {
    val isClear: Boolean get() = transactions.isEmpty()
}
