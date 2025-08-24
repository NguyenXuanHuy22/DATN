package com.example.datn

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class ProductViewModel : ViewModel() {
    val products = MutableLiveData<List<Product>>()
    val productDetail = MutableLiveData<ProductData?>()
    val isLoading = MutableLiveData<Boolean>()
    val error = MutableLiveData<String?>()

    // Lấy danh sách sản phẩm
    fun getListProducts() {
        viewModelScope.launch {
            isLoading.postValue(true)
            try {
                val response = RetrofitClient.apiService.getListProducts()
                if (response.isSuccessful) {
                    products.postValue(response.body()?.map { it.toProduct() } ?: emptyList())
                    error.postValue(null)
                } else {
                    error.postValue("Lỗi ${response.code()}: ${response.message()}")
                    products.postValue(emptyList())
                }
            } catch (e: Exception) {
                error.postValue("Lỗi tải danh sách sản phẩm: ${e.message}")
                products.postValue(emptyList())
                Log.e("ProductViewModel", "Lỗi gọi API danh sách sản phẩm: ${e.message}", e)
            } finally {
                isLoading.postValue(false)
            }
        }
    }

    // Lấy chi tiết sản phẩm
    fun getProductDetail(productId: String) {
        viewModelScope.launch {
            isLoading.postValue(true)
            try {
                val response = RetrofitClient.apiService.getProduct(productId)
                val body = response.body()
                if (response.isSuccessful && body != null) {
                    productDetail.postValue(body)
                    error.postValue(null)
                } else {
                    error.postValue("Lỗi ${response.code()}: ${response.message()} (productId: $productId)")
                    productDetail.postValue(null)
                }
            } catch (e: Exception) {
                error.postValue("Lỗi tải chi tiết sản phẩm: ${e.message} (productId: $productId)")
                productDetail.postValue(null)
                Log.e("ProductViewModel", "Lỗi gọi API chi tiết sản phẩm: ${e.message}", e)
            } finally {
                isLoading.postValue(false)
            }
        }
    }
}


