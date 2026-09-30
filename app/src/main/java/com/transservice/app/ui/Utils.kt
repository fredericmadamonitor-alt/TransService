package com.transservice.app.ui

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.FRANCE)
private val currencyFormat = NumberFormat.getNumberInstance(Locale.FRANCE).apply {
    minimumFractionDigits = 2
    maximumFractionDigits = 2
}

fun formatDate(millis: Long): String = dateFormat.format(millis)

fun parseDateOrNull(text: String): Long? = try {
    dateFormat.parse(text)?.time
} catch (e: Exception) {
    null
}

fun formatMontant(value: Double): String = "${currencyFormat.format(value)} Ar"

fun formatNombre(value: Double, decimales: Int = 2): String {
    val f = NumberFormat.getNumberInstance(Locale.FRANCE)
    f.minimumFractionDigits = decimales
    f.maximumFractionDigits = decimales
    return f.format(value)
}
