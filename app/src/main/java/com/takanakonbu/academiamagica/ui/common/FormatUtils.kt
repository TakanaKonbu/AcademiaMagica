package com.takanakonbu.academiamagica.ui.common

import java.math.BigDecimal
import java.math.RoundingMode
import java.text.DecimalFormat

fun formatInflationNumber(value: BigDecimal): String {
    if (value.compareTo(BigDecimal.ZERO) == 0) return "0.00"

    // 100万未満の数値はカンマ区切りで表示
    val million = BigDecimal("1E6")
    if (value < million) {
        return DecimalFormat("#,##0.00").format(value)
    }

    // 整数部分の桁数を取得
    val numDigits = value.toBigInteger().toString().length

    // 桁数から、どの単位（10^3, 10^6, ...）に属するかを計算
    // 例: 7桁(10^6) -> magnitude = 2, 10桁(10^9) -> magnitude = 3
    val magnitude = (numDigits - 1) / 3

    // サフィックスのインデックスを計算。'A'は10^6 (magnitude 2)から始まる
    val index = magnitude - 2

    // インデックスを基にアルファベットのサフィックスを生成する (A, B, ... Z, AA, AB, ...)
    fun getSuffix(i: Int): String {
        if (i < 0) return "" // Fallback for values between 1,000 and 999,999 if the initial check was not there
        var n = i
        val sb = StringBuilder()
        while (n >= 0) {
            sb.insert(0, ('A'.code + n % 26).toChar())
            n = n / 26 - 1
        }
        return sb.toString()
    }

    val suffix = getSuffix(index)

    // 表示用の数値を計算 (例: 1,230,000 -> 1.23)
    val divisor = BigDecimal.TEN.pow(magnitude * 3)
    val displayedValue = value.divide(divisor, 2, RoundingMode.FLOOR)

    return "${displayedValue}${suffix}"
}
