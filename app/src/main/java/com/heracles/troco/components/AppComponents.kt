package com.heracles.troco.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Divider
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxColors
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.heracles.troco.R
import com.heracles.troco.domain.model.PhoneVisualTransformation
import com.heracles.troco.ui.theme.DMSansFontFamily
import com.heracles.troco.ui.theme.DmSansBodyRegular15
import com.heracles.troco.ui.theme.GreenContainer
import com.heracles.troco.ui.theme.GreenPrimary
import com.heracles.troco.ui.theme.PurpleGrey80
import com.heracles.troco.ui.theme.RubikTitleBold28
import com.heracles.troco.ui.theme.TerracottaSecondary
import com.heracles.troco.ui.theme.TextColor

@Composable
fun NormalTextComponent(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 40.dp),
        style = TextStyle(
            fontSize = 24.sp,
            fontWeight = FontWeight.Normal,
            fontStyle = FontStyle.Normal
        ),
        color = TextColor,
        textAlign = TextAlign.Center
    )
}

@Composable
fun SubtitleTextComponent(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth(),
        style = DmSansBodyRegular15,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
fun HeaderTextComponent(text: String) {
    Text(
        text = text,
        modifier = Modifier
            .fillMaxWidth(),
        style = RubikTitleBold28,
        color = MaterialTheme.colorScheme.onBackground
    )
}

@Composable
fun TrocoTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    leadingIcon: ImageVector,
    modifier: Modifier = Modifier,
    hasError: String? = null,
    isPassword: Boolean = false,
    isPhone: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text,
    keyboardCapitalization: KeyboardCapitalization = KeyboardCapitalization.Sentences
) {
    var passwordVisible by remember { mutableStateOf(false) }

    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        label = {
            Text(
                text = label.uppercase(),
                style = RubikTitleBold28.copy(fontSize = 12.sp) // Ou o estilo que preferir para a label
            )
        },
        placeholder = {
            Text(
                text = placeholder,
                color = Color(0xFF8D9A8F) // Cor secundária extraída do Figma
            )
        },
        leadingIcon = {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        },
        trailingIcon = if (isPassword) {
            {
                val image = if (passwordVisible) {
                    painterResource(id = R.drawable.ic_visibility)
                } else {
                    painterResource(id = R.drawable.ic_visibility_off)
                }
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        painter = image,
                        contentDescription = if (passwordVisible) "Ocultar senha" else "Mostrar senha",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else null,
        visualTransformation = if (isPassword && !passwordVisible && value.isNotEmpty()) {
            PasswordVisualTransformation()
        } else if (isPhone) {
            PhoneVisualTransformation()
        } else {
            VisualTransformation.None
        },
        keyboardOptions = KeyboardOptions(
            keyboardType = keyboardType,
            capitalization = keyboardCapitalization
        ),
        singleLine = true,
        shape = RoundedCornerShape(
            topStart = 16.dp,
            topEnd = 16.dp,
            bottomStart = 0.dp,
            bottomEnd = 0.dp
        ),
        isError = !hasError.isNullOrBlank(),
        supportingText = {
            hasError?.let { text ->
                Text(
                    text = text,
                    fontFamily = DMSansFontFamily,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        colors = TextFieldDefaults.colors(
            // Fundo
            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,

            // Indicador / Borda inferior de 2dp
            focusedIndicatorColor = MaterialTheme.colorScheme.primary,
            unfocusedIndicatorColor = MaterialTheme.colorScheme.primary,

            // Cores das Labels
            focusedLabelColor = MaterialTheme.colorScheme.primary,
            unfocusedLabelColor = MaterialTheme.colorScheme.primary,

            // Texto digitado
            focusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,

            //Erro
            errorIndicatorColor = MaterialTheme.colorScheme.error,
            errorLabelColor = MaterialTheme.colorScheme.error
        )
    )
}


@Composable
fun CheckboxComponent() {
    Row(
        modifier = Modifier
            .heightIn(56.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        val checked = remember {
            mutableStateOf(false)
        }

        Checkbox(
            checked = checked.value,
            onCheckedChange = { isChecked -> checked.value = isChecked },
            colors = CheckboxColors(
                checkedBorderColor = GreenPrimary,
                checkedBoxColor = GreenContainer,
                uncheckedBoxColor = Color.Transparent,
                uncheckedBorderColor = GreenPrimary,
                checkedCheckmarkColor = GreenPrimary,
                disabledBorderColor = GreenPrimary,
                uncheckedCheckmarkColor = GreenPrimary,
                disabledCheckedBoxColor = GreenPrimary,
                disabledUncheckedBoxColor = GreenPrimary,
                disabledUncheckedBorderColor = GreenPrimary,
                disabledIndeterminateBoxColor = GreenPrimary,
                disabledIndeterminateBorderColor = GreenPrimary,
            )
        )
    }
}

@Composable
fun ClickableTermsPoliticsTextComponent(
    onTermsSelected: () -> Unit,
    onPrivacyPoliticsSelected: () -> Unit
) {
    val initialText = "Ao clicar em Criar conta, você concorda com os "
    val termOfUse = "Termos de uso"
    val andText = " e a "
    val politics = "Política de privacidade"

    val annotatedString = buildAnnotatedString {
        append(initialText)
        withLink(
            LinkAnnotation.Clickable(
                tag = "terms_of_use",
                styles = TextLinkStyles(
                    SpanStyle(
                        color = TerracottaSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                ),
                linkInteractionListener = { onTermsSelected() }
            )
        ) {
            append(termOfUse)
        }
        append(andText)
        withLink(
            LinkAnnotation.Clickable(
                tag = "privacy_policy",
                styles = TextLinkStyles(
                    SpanStyle(
                        color = TerracottaSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                ),
                linkInteractionListener = { onPrivacyPoliticsSelected() }
            )
        ) {
            append(politics)
        }
    }

    Text(
        text = annotatedString,
        modifier = Modifier
            .fillMaxWidth(),
        style = TextStyle(
            fontSize = 13.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = DMSansFontFamily,
            textAlign = TextAlign.Start
        )
    )
}

@Composable
fun ClickableTextComponent(
    initialText: String,
    clickableText: String,
    onTextSelected: () -> Unit
) {
    val annotatedString = buildAnnotatedString {
        append(initialText)
        withLink(
            LinkAnnotation.Clickable(
                tag = clickableText,
                styles = TextLinkStyles(
                    SpanStyle(
                        color = TerracottaSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                ),
                linkInteractionListener = { onTextSelected() }
            )
        ) {
            append(clickableText)
        }
    }

    Text(
        text = annotatedString,
        modifier = Modifier
            .fillMaxWidth(),
        style = TextStyle(
            color = MaterialTheme.colorScheme.onBackground,
            fontSize = 14.sp,
            fontWeight = FontWeight.Normal,
            fontFamily = DMSansFontFamily,
            textAlign = TextAlign.Center
        )
    )
}

@Composable
fun ButtonComponent(content: @Composable (() -> Unit), onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(),
        contentPadding = PaddingValues(),
        colors = ButtonDefaults.buttonColors(Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(52.dp)
                .background(
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(50.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            content()
//            Text(
//                text = value,
//                style = TextStyle(
//                    color = MaterialTheme.colorScheme.onPrimary,
//                    fontSize = 16.sp,
//                    fontWeight = FontWeight.SemiBold,
//                    fontStyle = FontStyle.Normal,
//                    fontFamily = RubikFontFamily
//                ),
//            )
        }
    }
}

@Composable
fun DividerTextComponent() {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            color = PurpleGrey80,
            thickness = 1.dp
        )

        Text(
            text = "ou",
            style = TextStyle(
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                fontStyle = FontStyle.Normal
            ),
            color = PurpleGrey80,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            color = PurpleGrey80,
            thickness = 1.dp
        )
    }
}
