package com.example.datn

import android.support.v4.os.IResultReceiver._Parcel
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.*


class ProductViewModel : ViewModel() {
    private val lstProduct = MutableLiveData<List<Product>>()
    val product: LiveData<List<Product>> = lstProduct

    private val _productDetail = MutableLiveData<Product?>()
    val productDetail: LiveData<Product?> = _productDetail



    fun getListProduct() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getListProducts()
                if (response.isSuccessful && response.body() != null) {
                    val products = response.body()!! // Không cần map vì đã là List<Product>
                    lstProduct.postValue(products)
                    Log.d("ProductViewModel", "Danh sách sản phẩm: $products")
                } else {
                    lstProduct.postValue(emptyList())
                    Log.e("ProductViewModel", "Lỗi phản hồi API: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                lstProduct.postValue(emptyList())
                Log.e("ProductViewModel", "Lỗi gọi API: ${e.message}", e)
            }
        }
    }
    fun getProductDetail(id: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getProductDetail(id)
                if (response.isSuccessful && response.body() != null) {
                    _productDetail.postValue(response.body())
                    Log.d("ProductViewModel", "Chi tiết sản phẩm: ${response.body()}")
                } else {
                    _productDetail.postValue(null)
                    Log.e("ProductViewModel", "Lỗi phản hồi chi tiết: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                _productDetail.postValue(null)
                Log.e("ProductViewModel", "Lỗi khi gọi chi tiết: ${e.message}", e)
            }
        }
    }
    fun getProductById(id: String): Product? {
        return product.value?.find { it.id == id }
    }

}