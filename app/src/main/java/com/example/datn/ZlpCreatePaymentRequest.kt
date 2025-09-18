package com.example.datn

import com.google.gson.annotations.SerializedName

data class ZlpCreatePaymentRequest(
    val userId: String,
    val items: List<CartItem>,
    val customerName: String,
    val customerPhone: String,
    val customerAddress: String,
    @SerializedName("redirect_url")
    val redirectUrl: String,
    @SerializedName("description") // 🔥 Sửa từ orderNote thành description để khớp backend
    val description: String? = null
)

