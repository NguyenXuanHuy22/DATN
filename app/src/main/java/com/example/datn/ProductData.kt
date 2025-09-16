package com.example.datn

import com.google.gson.annotations.SerializedName

data class ProductData(
    @SerializedName("_id") val _id: String?,
    @SerializedName("category") val category: String?,
    @SerializedName("name") val name: String?,
    @SerializedName("originalPrice") val originalPrice: Int?,   // ✅ giá gốc
    @SerializedName("salePrice") val salePrice: Int?,           // ✅ giá khuyến mãi
    @SerializedName("image") val image: String?,
    @SerializedName("extraImages") val extraImages: List<String>?, // ✅ danh sách ảnh phụ
    @SerializedName("description") val description: String?,
    @SerializedName("status") val status: String?,              // ✅ trạng thái hàng
    @SerializedName("variants") val variants: List<ProductVariantData>?
)

data class ProductVariantData(
    @SerializedName("size") val size: String,
    @SerializedName("color") val color: String,
    @SerializedName("quantity") val quantity: Int
)

fun ProductData.toProduct(): Product {
    return Product(
        _id = _id ?: "",
        category = category ?: "",
        name = name ?: "",
        originalPrice = originalPrice ?: 0,
        salePrice = salePrice ?: 0,
        image = image ?: "",
        extraImages = extraImages ?: emptyList(),
        description = description ?: "",
        status = status ?: "còn hàng",
        variants = variants?.map {
            ProductVariant(it.size, it.color, it.quantity)
        } ?: emptyList()
    )
}
