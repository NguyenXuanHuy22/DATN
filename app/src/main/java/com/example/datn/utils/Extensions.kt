package com.example.datn.utils

import java.text.NumberFormat
import java.util.*

fun Int.toDecimalString(): String {
    val format = NumberFormat.getInstance(Locale("vi", "VN"))
    return format.format(this)
}
