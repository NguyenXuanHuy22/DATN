package com.example.datn

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class OrderDetailViewModel(private val orderId: String) : ViewModel() {

    var uiState by mutableStateOf(OrderDetailUiState())
        private set

    init {
        fetchOrderDetail()
    }

    // Lấy chi tiết đơn hàng từ API
    private fun fetchOrderDetail() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                val response = RetrofitClient.apiService.getOrderDetail(orderId)
                if (response.isSuccessful) {
                    val body = response.body()
                    uiState = if (body != null) {
                        uiState.copy(order = body, errorMessage = null)
                    } else {
                        uiState.copy(errorMessage = "API trả về rỗng")
                    }
                } else {
                    uiState = uiState.copy(
                        errorMessage = "Lỗi API: ${response.code()} - ${response.message()}"
                    )
                }
            } catch (e: Exception) {
                uiState = uiState.copy(errorMessage = "Exception: ${e.message}")
            } finally {
                uiState = uiState.copy(isLoading = false)
            }
        }
    }

    // Huỷ đơn hàng
    fun cancelOrder() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true)
            try {
                val response = RetrofitClient.apiService.cancelOrder(orderId)
                if (response.isSuccessful) {
                    val body = response.body()
                    if (body != null) {
                        uiState = uiState.copy(order = body.order, errorMessage = null)
                    } else {
                        uiState = uiState.copy(errorMessage = "Huỷ đơn thành công nhưng body rỗng")
                    }
                } else {
                    uiState = uiState.copy(
                        errorMessage = "Lỗi API huỷ đơn: ${response.code()} - ${response.message()}"
                    )
                }
            } catch (e: Exception) {
                uiState = uiState.copy(errorMessage = "Exception huỷ đơn: ${e.message}")
            } finally {
                uiState = uiState.copy(isLoading = false)
            }
        }
    }

    // Đánh dấu toàn bộ đơn hàng đã được đánh giá (gọi API)
    // Khi đánh giá xong (ReviewActivity trả về RESULT_OK) thì gọi hàm này
    fun markOrderAsReviewed() {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.commentService.markOrderAsReviewed(orderId)
                if (response.isSuccessful) {
                    val updatedOrder = response.body()
                    if (updatedOrder != null) {
                        uiState = uiState.copy(order = updatedOrder)
                    } else {
                        // fallback: tự cập nhật cờ nếu server không trả order
                        uiState = uiState.copy(
                            order = uiState.order?.copy(isReviewed = true)
                        )
                    }
                }
            } catch (e: Exception) {
                uiState = uiState.copy(
                    errorMessage = "Lỗi đánh dấu đơn hàng đã đánh giá: ${e.message}"
                )
            }
        }
    }



    // Đánh dấu 1 sản phẩm trong đơn hàng đã được đánh giá (gọi API)
    fun markProductAsReviewed(productId: String) {
        viewModelScope.launch {
            try {
                val response =
                    RetrofitClient.commentService.markProductAsReviewed(orderId, productId)
                if (response.isSuccessful) {
                    val updatedOrder = response.body()
                    if (updatedOrder != null) {
                        uiState = uiState.copy(order = updatedOrder)
                    }
                }
            } catch (_: Exception) {
            }
        }
    }
}


data class OrderDetailUiState(
    val order: Order? = null,
    val isLoading: Boolean = true,
    val errorMessage: String? = null,
)
