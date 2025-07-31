package com.example.datn


data class ProductResponse(
    val id: String,
    val category: String,
    val name: String,
    val price: Int,
    val image: String,
    val description: String,
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
