package com.heracles.troco.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heracles.troco.R
import com.heracles.troco.components.ButtonComponent
import com.heracles.troco.components.TrocoTextField
import com.heracles.troco.ui.theme.DMSansFontFamily
import com.heracles.troco.ui.theme.TrocoTheme
import com.heracles.troco.ui.theme.RubikFontFamily

@Composable
fun ProductScreen(
    scaffoldPaddingValues: PaddingValues,
    productState: ProductUiState,
    onProdNameChange: (String) -> Unit,
    onProdPriceChange: (String) -> Unit,
    onProdQuantityChange: (String) -> Unit,
    onAvailabilityChange: (Boolean) -> Unit,
    onSave: () -> Unit
) {
    Column(
        verticalArrangement = Arrangement.SpaceBetween,
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            TrocoTextField(
                value = productState.name,
                onValueChange = onProdNameChange,
                label = "Nome do produto",
                placeholder = "Digite o nome do produto",
                leadingIcon = painterResource(R.drawable.ic_tag),
                keyboardType = KeyboardType.Text,
                keyboardCapitalization = KeyboardCapitalization.Words,
                hasError = productState.errors.name,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TrocoTextField(
                value = productState.price,
                onValueChange = onProdPriceChange,
                label = "Preço",
                isMoney = true,
                placeholder = "Digite o preço",
                leadingIcon = painterResource(R.drawable.ic_money),
                keyboardType = KeyboardType.Decimal,
                hasError = productState.errors.price,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            TrocoTextField(
                value = productState.quantity,
                onValueChange = onProdQuantityChange,
                label = "Quantidade",
                placeholder = "Digite a quantidade",
                leadingIcon = painterResource(R.drawable.ic_mgmt_product),
                keyboardType = KeyboardType.Number,
                hasError = productState.errors.quantity,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 20.dp),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    Text(
                        text = "Disponível para venda",
                        style = TextStyle(
                            fontSize = 15.sp,
                            fontWeight = FontWeight.SemiBold,
                            fontFamily = RubikFontFamily
                        )
                    )
                    Text(
                        text = "Mostrar este produto no catálogo",
                        style = TextStyle(
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Normal,
                            fontFamily = DMSansFontFamily,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
                Switch(
                    checked = productState.isAvailable,
                    onCheckedChange = onAvailabilityChange
                )
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth()
        ) {
            ButtonComponent(
                content = {
                    if (productState.isLoading) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
                    } else {
                        Text(
                            text = "Salvar Produto",
                            style = TextStyle(
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                fontStyle = FontStyle.Normal,
                                fontFamily = RubikFontFamily
                            ),
                        )
                    }
                },
                onClick = onSave
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductScreenPreview() {
    TrocoTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            ProductScreen(
                scaffoldPaddingValues = PaddingValues(),
                productState = ProductUiState(),
                onProdNameChange = {},
                onProdPriceChange = {},
                onProdQuantityChange = {},
                onAvailabilityChange = {},
                onSave = {}
            )
        }
    }
}