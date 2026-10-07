package com.heracles.troco.ui.screen

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heracles.troco.components.TrocoSearchBar
import com.heracles.troco.domain.ProductAvailability
import com.heracles.troco.domain.model.CatalogProduct
import com.heracles.troco.ui.theme.DMSansFontFamily
import com.heracles.troco.ui.theme.RubikFontFamily

@Composable
fun CatalogScreen(
    query: String,
    onQueryChange: (String) -> Unit,
    scaffoldPadding: PaddingValues
) {
    val products = listOf(
        CatalogProduct(
            bg = Color.Yellow,
            name = "Café Torrado",
            price = "R$ 18,80",
            status = ProductAvailability.AVAILABLE
        ),
        CatalogProduct(
            bg = Color.Cyan,
            name = "Bolacha",
            price = "R$ 3,50",
            status = ProductAvailability.AVAILABLE
        ),
        CatalogProduct(
            bg = Color.Magenta,
            name = "Arroz 2Kg",
            price = "R$ 10,00",
            status = ProductAvailability.OUT_OF_STOCK
        ),
        CatalogProduct(
            bg = Color.Red,
            name = "Feijão",
            price = "R$ 5,80",
            status = ProductAvailability.LAST_UNITS
        ),
        CatalogProduct(
            bg = Color.Magenta,
            name = "Arroz 2Kg",
            price = "R$ 10,00",
            status = ProductAvailability.OUT_OF_STOCK
        ),
        CatalogProduct(
            bg = Color.Red,
            name = "Feijão",
            price = "R$ 5,80",
            status = ProductAvailability.LAST_UNITS
        ),
    )

    Column(
        modifier = Modifier
            .fillMaxSize(),
    ) {
        TrocoSearchBar(
            query = query,
            onQueryChange = onQueryChange,
            placeholder = "Buscar produtos..."
        )

        Text(
            text = "Destaques da Mercearia",
            style = TextStyle(
                fontFamily = RubikFontFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp
            ),
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
        )

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 160.dp),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(bottom = scaffoldPadding.calculateBottomPadding()),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(
                items = products,
                key = { it.id } // Usa o ID único do produto como chave
            ) { item ->
                CatalogCardItem(
                    bg = item.bg,
                    name = item.name,
                    price = item.price,
                    status = item.status
                )
            }
        }
    }
}

@Composable
fun CatalogCardItem(
    bg: Color,
    name: String,
    price: String,
    status: ProductAvailability
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
        ),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.outlineVariant),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .background(color = bg)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(color = MaterialTheme.colorScheme.surface)
                    .padding(top = 12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .padding(start = 12.dp, end = 12.dp, bottom = 8.dp)
                ) {
                    Text(
                        text = name,
                        style = TextStyle(
                            fontFamily = RubikFontFamily,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                    Text(
                        text = price,
                        style = TextStyle(
                            fontFamily = DMSansFontFamily,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = MaterialTheme.colorScheme.primary
                        ),
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    CatalogStatusBadge(status = status)
                }
            }
        }
    }
}

@Composable
fun CatalogStatusBadge(status: ProductAvailability) {
    val (backgroundColor, textColor) = when (status) {
        ProductAvailability.AVAILABLE -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
        ProductAvailability.LAST_UNITS -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
        ProductAvailability.OUT_OF_STOCK -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
    }

    Text(
        text = status.label,
        color = textColor,
        modifier = Modifier
            .background(backgroundColor, shape = RoundedCornerShape(8.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp),
        style = TextStyle(
            fontFamily = DMSansFontFamily,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            color = textColor
        )
    )
}

@Preview(showBackground = true)
@Composable
private fun CatalogScreenPreview() {
    CatalogScreen(
        "",
        {},
        PaddingValues()
    )
}