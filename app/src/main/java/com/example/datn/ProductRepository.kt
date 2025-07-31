package com.example.datn

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ProductRepository {

    /**
     * Lấy số lượng tồn kho của một biến thể sản phẩm theo productId, size và color.
     */
    suspend fun getMaxQuantity(productId: String, size: String, color: String): Int {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.getProduct(productId)
                if (response.isSuccessful) {
                    val product = response.body()
                    product?.variants
                        ?.firstOrNull { it.size == size && it.color == color }
                        ?.quantity ?: 0
                } else {
                    0
                }
            } catch (e: Exception) {
                e.printStackTrace()
                0
            }
        }
    }

    /**
     * Lấy thông tin chi tiết sản phẩm.
     */
    suspend fun getProductDetail(productId: String): ProductData? {
        return withContext(Dispatchers.IO) {
            try {
                val response = RetrofitClient.apiService.getProduct(productId)
                if (response.isSuccessful) {
                    response.body()
                } else {
                    null
                }
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }
}