package com.example.datn

import com.google.gson.annotations.SerializedName

data class ProductData(
    @SerializedName("id") val id: String,
    @SerializedName("name") val name: String,
    @SerializedName("price") val price: Int,
    @SerializedName("sizes") val sizes: List<String>,
    @SerializedName("colors") val colors: List<String>,
    @SerializedName("image") val image: String,
    @SerializedName("description") val description: String,
)

fun ProductData.toProduct(): Product {
    return Product(
        id = id,
        name = name,
        price = price,
        sizes = sizes,
        colors = colors,
        image = image,
        description = description
    )
}
