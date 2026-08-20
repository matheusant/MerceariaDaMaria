package com.heracles.troco.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heracles.troco.R
import com.heracles.troco.domain.model.Transaction
import com.heracles.troco.domain.model.TransactionItem
import com.heracles.troco.domain.model.TransactionType
import com.heracles.troco.ui.theme.DMSansFontFamily
import com.heracles.troco.ui.theme.MerceariaDaMariaTheme
import com.heracles.troco.ui.theme.RubikFontFamily

@Composable
fun MeuFiadoScreen(
    state: MeuFiadoUiState
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        MeuFiadoCard(
            balance = state.balance,
            isClear = state.isClear,
            dueDate = state.dueDate
        )

        AnimatedContent(
            targetState = state.isClear,
            label = "FiadoContentTransition"
        ) { isClear ->
            if (isClear) {
                MeuFiadoPaid()
            } else {
                MeuFiadoTransactionList(state.transactions)
            }
        }
    }
}

@Composable
fun MeuFiadoPaid() {
    Column(
        modifier = Modifier
            .padding(top = 20.dp)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(100.dp)
                .background(
                    color = MaterialTheme.colorScheme.primaryContainer,
                    shape = CircleShape
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Check",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(40.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = "Você está em dia!",
            style = TextStyle(
                fontFamily = RubikFontFamily,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = MaterialTheme.colorScheme.primary
            )
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "Agradecemos pela preferência e confiança. Sua caderneta não tem nenhuma pendência com Dona Maria.",
            style = TextStyle(
                fontFamily = DMSansFontFamily,
                fontWeight = FontWeight.Normal,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
    }
}

@Composable
fun MeuFiadoCard(
    balance: String,
    isClear: Boolean,
    dueDate: String?,
) {
    val (backgroundColor, textColor) = when (isClear) {
        true -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
        false -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.secondary
    }
    Card(
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        ),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, textColor),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 20.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp)
        ) {
            Text(
                text = "SALDO DA CADERNETA",
                style = TextStyle(
                    fontFamily = DMSansFontFamily,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    letterSpacing = 0.5.sp,
                    color = textColor
                )
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = balance,
                style = TextStyle(
                    fontFamily = RubikFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 32.sp,
                    color = textColor
                )
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = if (isClear) "Tudo pago por aqui!" else dueDate!!,
                style = TextStyle(
                    fontFamily = DMSansFontFamily,
                    fontWeight = FontWeight.Normal,
                    fontSize = 13.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
    }
}

@Composable
fun MeuFiadoTransactionList(
    transactions: List<Transaction>
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "Extrato de Compras",
            style = TextStyle(
                fontFamily = RubikFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        ) {
            items(transactions) { transaction ->
                MeuFiadoTransactionItem(transaction)
            }
        }
    }
}

@Composable
fun MeuFiadoTransactionItem(
    transaction: Transaction,
) {
    var isExpanded: Boolean by rememberSaveable { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
            .clickable(onClick = {
                isExpanded = transaction.details.isNotEmpty() && !isExpanded
            })
    ) {
        val (bgColor, otherColors) = when (transaction.type) {
            TransactionType.PURCHASE -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.secondary
            TransactionType.PAYMENT -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
        }
        val paymentIcon = when (transaction.type) {
            TransactionType.PURCHASE -> R.drawable.ic_arrow_up_right
            TransactionType.PAYMENT -> R.drawable.ic_arrow_down_left
        }
        val arrowRotationDegree by animateFloatAsState(

            targetValue = if (isExpanded) 180f else 0f,
            label = "ArrowRotation"
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = bgColor,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(paymentIcon),
                        contentDescription = "Arrow",
                        tint = otherColors
                    )
                }
                Column(modifier = Modifier.padding(start = 12.dp)) {
                    Text(
                        text = transaction.title,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        style = TextStyle(
                            fontFamily = DMSansFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        text = transaction.formattedDate,
                        style = TextStyle(
                            fontFamily = DMSansFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        ),
                        modifier = Modifier.padding(top = 2.dp)
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxHeight(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = transaction.amount,
                    style = TextStyle(
                        fontFamily = DMSansFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = otherColors
                    ),
                )
                Icon(
                    imageVector = Icons.Default.KeyboardArrowDown,
                    contentDescription = "Expand",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.rotate(arrowRotationDegree)
                )
            }
        }
        AnimatedVisibility(
            visible = isExpanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            MeuFiadoTransactionItemDetails(transaction.details)
        }
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 12.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp
        )
    }
}

@Composable
fun MeuFiadoTransactionItemDetails(details: List<TransactionItem>) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 12.dp, start = 52.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Item",
                style = TextStyle(
                    fontFamily = DMSansFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Text(
                text = "Subtotal",
                style = TextStyle(
                    fontFamily = DMSansFontFamily,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
        }
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant,
            thickness = 1.dp
        )
        Row {
            Column {
                details.forEach { item ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = item.name,
                            style = TextStyle(
                                fontFamily = DMSansFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                        Text(
                            text = "R$ ${item.subtotal}",
                            style = TextStyle(
                                fontFamily = DMSansFontFamily,
                                fontWeight = FontWeight.Normal,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MeuFiadoScreenPreview() {
    MerceariaDaMariaTheme {
        Column(modifier = Modifier.padding(20.dp)) {
            MeuFiadoScreen(
                state = MeuFiadoUiState(
                    balance = "R$ 54,90",
                    dueDate = "11 de maio de 2026",
                    transactions = listOf(
                        Transaction(
                            title = "2x Café Torrado 500g, 1x Açucar 500g",
                            formattedDate = "Hoje às 09:12",
                            details = listOf(
                                TransactionItem(
                                    name = "2x Café Torrado 500g",
                                    subtotal = "39,90"
                                ),
                                TransactionItem(
                                    name = "1x Açucar 500g",
                                    subtotal = "3,00"
                                )
                            ),
                            amount = "- R$ 42,90",
                            type = TransactionType.PURCHASE
                        ),
                        Transaction(
                            title = "Pagamento parcial em dinheiro",
                            formattedDate = "Hoje às 15:00",
                            amount = "+ R$ 20,00",
                            type = TransactionType.PAYMENT
                        ),
                        Transaction(
                            title = "1x Açucar 500g",
                            formattedDate = "Hoje às 16:15",
                            amount = "- R$ 6,90",
                            type = TransactionType.PURCHASE
                        )
                    )
                )
            )
        }
    }
}