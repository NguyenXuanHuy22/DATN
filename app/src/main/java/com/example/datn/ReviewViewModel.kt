package com.example.datn

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import retrofit2.Response

class ReviewViewModel : ViewModel() {

    private val api = RetrofitClient.commentService

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    fun addReview(
        userId: String,
        productId: String,
        orderId: String,
        avatar: String,
        username: String,
        ratingStar: Int,
        commentDes: String,
        onSuccess: () -> Unit
    ) {
        if (ratingStar == 0 || commentDes.isBlank()) {
            _errorMessage.value = "Vui lòng nhập đầy đủ thông tin"
            return
        }

        val review = Comment(
            userId = userId,
            productId = productId,
            orderId = orderId,
            avatar = avatar,
            username = username,
            ratingStar = ratingStar,
            commentDes = commentDes
        )

        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = api.addComment(review)
                if (response.isSuccessful) {
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string()
                    _errorMessage.value = errorBody ?: "Lỗi không xác định"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Không thể kết nối server"
                Log.e("ReviewViewModel", "Exception: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}

