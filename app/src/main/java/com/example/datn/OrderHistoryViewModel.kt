package com.example.datn

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
            val fetchedOrders = OrderRepository().getOrdersByUser(userId)
            _orders.value = fetchedOrders
        }
    }

    fun filterOrdersByStatus(status: String): List<Order> {
        return _orders.value.filter { it.status == status }
    }
}
