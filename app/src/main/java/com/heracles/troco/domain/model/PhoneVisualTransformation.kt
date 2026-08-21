package com.heracles.troco.domain.model

import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class PhoneVisualTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val trimmed = text.text.filter { it.isDigit() }.take(11)
        val isNineDigits = trimmed.length > 10

        val out = StringBuilder()
        for (i in trimmed.indices) {
            when {
                i == 0 -> out.append("(${trimmed[i]}")
                i == 1 -> out.append("${trimmed[i]}) ")
                // Se tiver mais de 10 dígitos (celular), o hífen vai após o 5º dígito do número (índice 6)
                // Se tiver 10 ou menos (fixo), o hífen vai após o 4º dígito do número (índice 5)
                i == 5 && !isNineDigits -> out.append("${trimmed[i]}-")
                i == 6 && isNineDigits -> out.append("${trimmed[i]}-")
                else -> out.append(trimmed[i])
            }
        }

        val phoneNumberOffsetTranslator = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                val clamped = offset.coerceIn(0, trimmed.length)
                return when {
                    clamped <= 0 -> 0
                    clamped <= 2 -> clamped + 1 // Adiciona '('
                    clamped <= (if (isNineDigits) 6 else 5) -> clamped + 3 // Adiciona ') '
                    clamped <= 11 -> clamped + 4 // Adiciona '-'
                    else -> out.length
                }
            }

            override fun transformedToOriginal(offset: Int): Int {
                val hyphenPos = if (isNineDigits) 10 else 9
                val rawOffset = when {
                    offset <= 1 -> 0
                    offset <= 4 -> offset - 1
                    offset <= hyphenPos -> offset - 3
                    offset <= 15 -> offset - 4
                    else -> trimmed.length
                }
                return rawOffset.coerceIn(0, trimmed.length)
            }
        }

        return TransformedText(
            text = AnnotatedString(out.toString()),
            offsetMapping = phoneNumberOffsetTranslator
        )
    }
}