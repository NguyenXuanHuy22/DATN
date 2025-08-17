package com.example.datn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

/**
 * Factory để tạo OrderDetailViewModel có tham số orderId
 */
class OrderDetailViewModelFactory(
    private val orderId: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(OrderDetailViewModel::class.java)) {
            return OrderDetailViewModel(orderId) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
