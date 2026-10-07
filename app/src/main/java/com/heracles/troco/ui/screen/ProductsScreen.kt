package com.heracles.troco.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heracles.troco.components.TrocoSearchBar
import com.heracles.troco.domain.model.Product
import com.heracles.troco.domain.model.ProductStatus
import com.heracles.troco.domain.model.Products
import com.heracles.troco.ui.theme.DMSansFontFamily
import com.heracles.troco.ui.theme.MerceariaDaMariaTheme
import com.heracles.troco.ui.theme.RubikFontFamily

@Composable
fun ProductsScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    scaffoldPadding: PaddingValues
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        val products = Products(
            products = listOf(
                Product(
                    name = "Café Torrado 500g",
                    price = "R$ 18,90",
                    quantity = 15,
                    status = ProductStatus.ACTIVE
                ),
                Product(
                    name = "Leite Integral 1L",
                    price = "R$ 5,49",
                    quantity = 3,
                    status = ProductStatus.CRITIC
                ),
                Product(
                    name = "Açúcar Refinado 1kg",
                    price = "R$ 5,10",
                    quantity = 0,
                    status = ProductStatus.INACTIVE
                ),
                Product(
                    name = "Leite Integral 1L",
                    price = "R$ 5,49",
                    quantity = 3,
                    status = ProductStatus.CRITIC
                ),
                Product(
                    name = "Açúcar Refinado 1kg",
                    price = "R$ 5,10",
                    quantity = 0,
                    status = ProductStatus.INACTIVE
                ),
                Product(
                    name = "Leite Integral 1L",
                    price = "R$ 5,49",
                    quantity = 3,
                    status = ProductStatus.CRITIC
                ),
                Product(
                    name = "Açúcar Refinado 1kg",
                    price = "R$ 5,10",
                    quantity = 0,
                    status = ProductStatus.INACTIVE
                ),
                Product(
                    name = "Leite Integral 1L",
                    price = "R$ 5,49",
                    quantity = 3,
                    status = ProductStatus.CRITIC
                ),
                Product(
                    name = "Açúcar Refinado 1kg",
                    price = "R$ 5,10",
                    quantity = 0,
                    status = ProductStatus.INACTIVE
                ),
                Product(
                    name = "Leite Integral 1L",
                    price = "R$ 5,49",
                    quantity = 3,
                    status = ProductStatus.CRITIC
                ),
                Product(
                    name = "Açúcar Refinado 1kg",
                    price = "R$ 5,10",
                    quantity = 0,
                    status = ProductStatus.INACTIVE
                ),
            )
        )

        TrocoSearchBar(
            query = query,
            onQueryChange = onQueryChange,
            placeholder = "Buscar produtos..."
        )

        Spacer(modifier = Modifier.height(28.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(1),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = scaffoldPadding.calculateBottomPadding() + 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = products.products,
                key = { product -> product.id }
            ) { product ->
                ProductItem(product = product)
            }
        }
    }
}

@Composable
fun ProductItem(
    product: Product
) {
    val isEnabled = product.status != ProductStatus.INACTIVE
    Card(
        onClick = {},
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        enabled = product.status != ProductStatus.INACTIVE,
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.outlineVariant)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max)
                .padding(all = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .padding(end = 12.dp)
                    .size(60.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {}
            Column(
                modifier = Modifier
                    .weight(1f)
            ) {
                Text(
                    text = product.name,
                    style = TextStyle(
                        fontFamily = RubikFontFamily,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 15.sp
                    ),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Text(
                    text = product.price,
                    style = TextStyle(
                        fontFamily = DMSansFontFamily,
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = if (isEnabled) MaterialTheme.colorScheme.primary else Color.Unspecified
                    ),
                    modifier = Modifier.padding(bottom = 4.dp)
                )
                Row() {
                    Text(
                        text = "Estoque: ${product.quantity} un",
                        style = TextStyle(
                            fontFamily = DMSansFontFamily,
                            fontWeight = FontWeight.Normal,
                            fontSize = 12.sp,
                            color = if (isEnabled) MaterialTheme.colorScheme.onSurfaceVariant else Color.Unspecified
                        ),
                        modifier = Modifier.padding(end = 8.dp)
                    )
                    ProductStatusBadge(
                        productStatus = product.status,
                    )
                }
            }
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "Editar"
            )
        }
    }
}

@Composable
fun ProductStatusBadge(
    productStatus: ProductStatus,
) {
    val (bgColor, textColor) = when (productStatus) {
        ProductStatus.ACTIVE -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
        ProductStatus.CRITIC -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        ProductStatus.INACTIVE -> MaterialTheme.colorScheme.surfaceVariant to Color.Unspecified
    }

    Text(
        text = productStatus.label,
        color = textColor,
        modifier = Modifier
            .background(bgColor, shape = RoundedCornerShape(4.dp))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        style = TextStyle(
            fontFamily = DMSansFontFamily,
            fontWeight = FontWeight.SemiBold,
            fontSize = 10.sp,
            color = textColor
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun ProductsScreenPreview() {
    MerceariaDaMariaTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ProductsScreen(
                query = "",
                onQueryChange = {},
                scaffoldPadding = PaddingValues()
            )
        }
    }
}