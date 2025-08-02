package com.example.datn

import com.google.gson.annotations.SerializedName

data class Order(
    val id: String? = null,
    val userId: String,
    val total: Int,
    val status: String,
    val items: List<OrderItem>
)

data class OrderItem(
    val orderDetailId: String? = null,
    val name: String,
    val productId: String,
    val image: String,
    val price: Int,
    val quantity: Int,
    val size: String,
    val color: String,
    val subtotal: Int,
    val date: String,

    @SerializedName("Payment method")
    val paymentMethod: String,

    val customerName: String,
    val customerPhone: String,
    val customerAddress: String
)
