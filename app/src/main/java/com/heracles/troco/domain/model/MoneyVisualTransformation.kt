package com.heracles.troco.domain.model

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class MoneyVisualTransformation : VisualTransformation {

    override fun filter(text: AnnotatedString): TransformedText {
        val digits = text.text.filter(Char::isDigit)

        val formatted = formatMoney(digits)

        return TransformedText(
            text = AnnotatedString(formatted),
            offsetMapping = object : OffsetMapping {

                override fun originalToTransformed(offset: Int): Int {
                    return formatted.length
                }

                override fun transformedToOriginal(offset: Int): Int {
                    return digits.length
                }
            }
        )
    }

    private fun formatMoney(digits: String): String {
        if (digits.isEmpty()) {
            return ""
        }

        val value = digits
            .padStart(3, '0')

        val cents = value.takeLast(2)
        val integer = value.dropLast(2)

        val formattedInteger = integer
            .reversed()
            .chunked(3)
            .joinToString(".")
            .reversed()

        return "R$ $formattedInteger,$cents"
    }
}