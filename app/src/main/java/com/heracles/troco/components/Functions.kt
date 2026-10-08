package com.heracles.troco.components

fun String.formatMoney(): String {
    if (isEmpty()) return ""

    val value = filter { it.isDigit() }.padStart(3, '0')

    val cents = value.takeLast(2)
    val integer = value.dropLast(2)

    val formattedInteger = integer
        .reversed()
        .chunked(3)
        .joinToString(".")
        .reversed()

    return "R$ $formattedInteger,$cents"
}