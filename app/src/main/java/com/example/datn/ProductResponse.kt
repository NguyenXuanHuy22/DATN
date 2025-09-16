package com.example.datn

data class ProductResponse(
    val _id: String,
    val category: String,
    val name: String,
    val originalPrice: Int,       // ✅ giá gốc
    val salePrice: Int,           // ✅ giá khuyến mãi
    val image: String,
    val extraImages: List<String>, // ✅ nhiều ảnh phụ
    val description: String,
    val status: String,           // ✅ trạng thái sản phẩm
    val variants: List<ProductVariantResponse>
)

data class ProductVariantResponse(
    val size: String,
    val color: String,
    val quantity: Int
)

// ✅ Mapper function
fun ProductResponse.toProduct(): Product {
    return Product(
        _id = _id,
        category = category,
        name = name,
        originalPrice = originalPrice,
        salePrice = salePrice,
        image = image,
        extraImages = extraImages,
        description = description,
        status = status,
        variants = variants.map {
            ProductVariant(it.size, it.color, it.quantity)
        }
    )
}
