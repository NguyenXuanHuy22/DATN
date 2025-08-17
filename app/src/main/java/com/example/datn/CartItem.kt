package com.example.datn


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
)

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


