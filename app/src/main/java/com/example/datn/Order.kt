package com.example.datn

import com.google.gson.annotations.SerializedName

data class Order(
    @SerializedName("_id") val orderId: String = "",   // mặc định rỗng
    @SerializedName("userId") val userId: String = "",
    @SerializedName("items") val items: List<OrderItem> = emptyList(),
    @SerializedName("total") val total: Int = 0,
    @SerializedName("paymentMethod") val paymentMethod: String = "",
    @SerializedName("status") val status: String = "",
    @SerializedName("date") val date: String = "",
    @SerializedName("isReviewed") val isReviewed: Boolean = false,

    @SerializedName("customerName") val customerName: String? = null,
    @SerializedName("customerPhone") val customerPhone: String? = null,
    @SerializedName("customerAddress") val customerAddress: String? = null
)

data class OrderItem(
    @SerializedName("orderDetailId") val orderDetailId: String = "",
    @SerializedName("productId") val productId: String = "",
    @SerializedName("name") val name: String = "",
    @SerializedName("image") val image: String = "",
    @SerializedName("price") val price: Int = 0,
    @SerializedName("quantity") val quantity: Int = 0,
    @SerializedName("size") val size: String = "",
    @SerializedName("color") val color: String = "",
    @SerializedName("subtotal") val subtotal: Int = 0,
    @SerializedName("isReviewed") val isReviewed: Boolean = false,
)
