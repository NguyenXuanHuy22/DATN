package com.example.datn

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ReviewViewModel : ViewModel() {

    private val api = RetrofitClient.commentService

    // Loading state
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Error message
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // Danh sách comment
    private val _comments = MutableStateFlow<List<Comment>>(emptyList())
    val comments: StateFlow<List<Comment>> = _comments

    // ⭐ Điểm trung bình
    private val _avgRating = MutableStateFlow(0.0)
    val avgRating: StateFlow<Double> = _avgRating



    /**
     * Lấy danh sách comment theo productId
     */
    fun getCommentsByProduct(productId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            try {
                val response = api.getComments(productId)
                if (response.isSuccessful && response.body() != null) {
                    val list = response.body()!!
                    _comments.value = list

                    // ✅ Tính điểm trung bình rating
                    _avgRating.value = if (list.isNotEmpty()) {
                        list.map { it.ratingStar }.average()
                    } else {
                        0.0
                    }
                } else {
                    _errorMessage.value = "Không thể tải đánh giá (${response.code()})"
                    _comments.value = emptyList()
                    _avgRating.value = 0.0
                }
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi kết nối server: ${e.localizedMessage}"
                _comments.value = emptyList()
                _avgRating.value = 0.0
                Log.e("ReviewViewModel", "getCommentsByProduct error: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Thêm review mới
     */
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
                    // Refresh lại danh sách sau khi thêm thành công
                    getCommentsByProduct(productId)
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string()
                    _errorMessage.value = errorBody ?: "Không thể gửi đánh giá"
                }
            } catch (e: Exception) {
                _errorMessage.value = "Lỗi kết nối server"
                Log.e("ReviewViewModel", "addReview error: ${e.message}", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}

