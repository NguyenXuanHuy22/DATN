package com.example.datn

import com.google.gson.annotations.SerializedName

data class ProductData(
    @SerializedName("id") val id: String,
    @SerializedName("category") val category: String,
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Int,
    @SerializedName("image") val image: String,
    @SerializedName("description") val description: String,
    @SerializedName("variants") val variants: List<ProductVariantData>
)

data class ProductVariantData(
    @SerializedName("size") val size: String,
    @SerializedName("color") val color: String,
    @SerializedName("quantity") val quantity: Int
)

fun ProductData.toProduct(): Product {
    return Product(
        id = id,
        category = category,
        name = name,
        price = price,
        image = image,
        description = description,
        variants = variants.map {
            ProductVariant(it.size, it.color, it.quantity)
        }
    )
}
