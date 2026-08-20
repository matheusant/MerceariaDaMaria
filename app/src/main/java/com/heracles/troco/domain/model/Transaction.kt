package com.heracles.troco.domain.model

import java.util.UUID

enum class TransactionType {
    PURCHASE,
    PAYMENT
}

data class TransactionItem(
    val name: String,
    val subtotal: String
)

data class Transaction (
    val id: String = UUID.randomUUID().toString(),
    val title: String,                           // Ex: "2x Café Torrado 500g, 1x Açú..."
    val formattedDate: String,                   // Ex: "Hoje às 09:12" ou "10 Mai às 14:00"
    val amount: String,                          // Ex: "- R$ 42,90" ou "+ R$ 20,00"
    val type: TransactionType,                   // Defines as cores (rosa/verde) e ícone (seta)
    val details: List<TransactionItem> = emptyList(), // Lista expandida de produtos (se houver)
    val isExpanded: Boolean = false              // Estado local para abrir/fechar o detalhe
)