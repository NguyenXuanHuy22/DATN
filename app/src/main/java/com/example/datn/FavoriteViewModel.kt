package com.example.datn

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class FavoriteViewModel : ViewModel() {
    private val _wishlistItems = MutableLiveData<List<WishlistItem>>(emptyList())
    val wishlistItems: LiveData<List<WishlistItem>> = _wishlistItems

    val error = MutableLiveData<String?>()

    fun loadWishlist(userId: String) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.apiService.getWishlistByUserId(userId)
                if (response.isSuccessful) {
                    val wishlist = response.body()
                    _wishlistItems.postValue(wishlist?.items ?: emptyList())
                    error.postValue(null)
                } else {
                    error.postValue("Lỗi tải danh sách yêu thích: ${response.code()}")
                    _wishlistItems.postValue(emptyList())
                }
            } catch (e: Exception) {
                error.postValue("Lỗi: ${e.message}")
                _wishlistItems.postValue(emptyList())
            }
        }
    }

    fun deleteWishlistItem(userId: String?, productId: String) {
        if (userId == null) {
            error.postValue("Vui lòng đăng nhập để thực hiện chức năng này")
            return
        }
        viewModelScope.launch {
            try {
                val product = WishlistItem(productId, "", "", 0)
                val request = ToggleWishlistRequest(userId, product)
                val response = RetrofitClient.apiService.toggleWishlist(request) // đổi tên cho thống nhất
                if (response.isSuccessful) {
                    _wishlistItems.postValue(response.body()?.items ?: emptyList())
                    error.postValue(null)
                } else {
                    error.postValue("Lỗi xóa sản phẩm: ${response.code()}")
                }
            } catch (e: Exception) {
                error.postValue("Lỗi: ${e.message}")
            }
        }
    }
    fun toggleFavorite(userId: String, product: WishlistItem) {
        viewModelScope.launch {
            try {
                val request = ToggleWishlistRequest(userId, product)
                val response = RetrofitClient.apiService.toggleWishlist(request)
                if (response.isSuccessful) {
                    // Cập nhật lại danh sách wishlist từ response trả về
                    _wishlistItems.postValue(response.body()?.items ?: emptyList())
                    error.postValue(null)
                } else {
                    error.postValue("Lỗi khi cập nhật yêu thích: ${response.code()}")
                }
            } catch (e: Exception) {
                error.postValue("Lỗi khi cập nhật yêu thích: ${e.message}")
            }
        }
    }

}



