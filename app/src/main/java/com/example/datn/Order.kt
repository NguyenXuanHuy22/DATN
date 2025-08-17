package com.example.datn

import com.google.gson.annotations.SerializedName

data class Order(
    @SerializedName("_id") val orderId: String,
    @SerializedName("userId") val userId: String,
    @SerializedName("items") val items: List<OrderItem>,
    @SerializedName("total") val total: Int,
    @SerializedName("paymentMethod") val paymentMethod: String,
    @SerializedName("status") val status: String,
    @SerializedName("date") val date: String,

    // ✅ Map đúng field từ MongoDB
    @SerializedName("customerName") val customerName: String?,
    @SerializedName("customerPhone") val customerPhone: String?,
    @SerializedName("customerAddress") val customerAddress: String?
)

data class OrderItem(
    // ✅ MongoDB dùng orderDetailId, không phải _id
    @SerializedName("orderDetailId") val orderDetailId: String,
    @SerializedName("productId") val productId: String,
    @SerializedName("name") val name: String,
    @SerializedName("image") val image: String,
    @SerializedName("price") val price: Int,
    @SerializedName("quantity") val quantity: Int,
    @SerializedName("size") val size: String,
    @SerializedName("color") val color: String,
    @SerializedName("subtotal") val subtotal: Int
)
