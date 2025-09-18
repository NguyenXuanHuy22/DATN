package com.example.datn

import java.util.UUID
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class CartItem(
    val itemId: String? = null,   // _id từ MongoDB
    val productId: String? = null,
    val name: String? = null,
    val image: String? = null,
    val price: Int = 0,
    val quantity: Int = 0,
    val size: String? = null,
    val color: String? = null,
    val maxQuantity: Int = 0,
    val userId: String? = null
) : Parcelable { // ✅ Bây giờ CartItem có thể truyền qua Intent
    fun uniqueId(): String = "${productId}_${size}_${color}"
}


fun CartItem.toDtoForCreate(): CartItemDto {
    return CartItemDto(
        _id = null,
        productId = productId ?: "",
        name = name ?: "",
        image = image ?: "",
        price = price,
        quantity = quantity,
        size = size ?: "",
        color = color ?: "",
        userId = userId ?: ""
    )
}

fun CartItem.toDtoForUpdate(): CartItemDto {
    return CartItemDto(
        _id = itemId ?: "",
        productId = productId ?: "",
        name = name ?: "",
        image = image ?: "",
        price = price,
        quantity = quantity,
        size = size ?: "",
        color = color ?: "",
        userId = userId ?: ""
    )
}

fun CartItem.toOrderItemRequest(): OrderItemRequest {
    // nếu price/quantity là non-null Int thì bỏ ?: 0
    val unitPrice = this.price
    val qty = this.quantity
    val subtotal = unitPrice * qty

    return OrderItemRequest(
        orderDetailId = UUID.randomUUID().toString(), // bắt buộc
        productId = this.productId ?: "",
        name = this.name ?: "",
        image = this.image ?: "",
        price = unitPrice,
        quantity = qty,
        size = this.size ?: "",
        color = this.color ?: "",
        subtotal = subtotal // bắt buộc
    )
}



