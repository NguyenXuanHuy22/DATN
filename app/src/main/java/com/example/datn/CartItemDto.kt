package com.example.datn

import android.util.Log
import com.google.gson.annotations.SerializedName


data class CartItemDto(
    @SerializedName(value = "_id", alternate = ["itemId"])
    val _id: String?, // ID của item trong MongoDB
    val productId: String,
    val image: String,
    val name: String,
    val price: Int,
    val size: String,
    val color: String,
    val quantity: Int,
    val userId: String
)

fun CartItemDto.toCartItem(): CartItem {
    return CartItem(
        itemId = _id ?: "",
        productId = productId,
        image = image,
        name = name,
        price = price,
        quantity = quantity,
        size = size,
        color = color,
        maxQuantity = 0,
        userId = userId
    )
}