package com.example.visualmoney.util

import kotlin.math.round

fun formatDecimal(
    value: Double,
    decimals: Int = 2,
    grouping: Boolean = true
): String {
    // Round to N decimals without relying on platform formatting
    val factor = pow10(decimals)
    val rounded = round(value * factor) / factor

    val whole = rounded.toLong()
    val frac = ((rounded - whole) * factor).let { round(it).toLong() }.coerceIn(0, factor - 1)

    val wholeStr = if (grouping) groupThousands(whole) else whole.toString()
    val fracStr = frac.toString().padStart(decimals, '0')

    return if (decimals == 0) wholeStr else "$wholeStr.$fracStr"
}

private fun pow10(n: Int): Long {
    var r = 1L
    repeat(n.coerceAtLeast(0)) { r *= 10L }
    return r
}

private fun groupThousands(value: Long): String {
    val s = value.toString()
    val out = StringBuilder()
    var count = 0
    for (i in s.length - 1 downTo 0) {
        out.append(s[i])
        count++
        if (count % 3 == 0 && i != 0) out.append(',')
    }
    return out.reverse().toString()
}
