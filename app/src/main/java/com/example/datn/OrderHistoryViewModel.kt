package com.example.datn

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OrderHistoryViewModel : ViewModel() {
    private val _orders = MutableStateFlow<List<Order>>(emptyList())
    val orders: StateFlow<List<Order>> = _orders


    fun loadOrders(userId: String) {
        viewModelScope.launch {
            try {
                val fetchedOrders = OrderRepository().getOrdersByUser(userId)
                _orders.value = fetchedOrders
            } catch (e: Exception) {
                Log.e("OrderHistoryViewModel", "Lỗi tải đơn hàng: ${e.message}")
                _orders.value = emptyList() // hoặc xử lý lỗi khác
            }
        }
    }
}
