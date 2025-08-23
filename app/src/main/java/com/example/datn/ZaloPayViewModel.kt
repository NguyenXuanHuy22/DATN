package com.example.datn

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import kotlinx.coroutines.launch

class ZaloPayViewModel : ViewModel() {
    private val zaloPayService = RetrofitClient.zaloPayService

    fun createOrder(
        amount: Int,
        userId: String,
        paymentMethod: String,
        customerName: String,
        customerPhone: String,
        customerAddress: String,
        items: List<OrderItemRequest>,   // ✅ thêm items
        onResult: (ZaloPayOrderResponse?) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val request = ZaloPayOrderRequest(
                    amount = amount,
                    description = "Thanh toán đơn hàng",
                    userId = userId,
                    paymentMethod = paymentMethod,
                    customerName = customerName,
                    customerPhone = customerPhone,
                    customerAddress = customerAddress,
                    items = items
                )

                Log.d("ZaloPayViewModel", "CreateOrder request JSON: ${Gson().toJson(request)}")
                val response = zaloPayService.createOrder(request)


                if (response.isSuccessful) {
                    val wrapper = response.body()
                    Log.d("ZaloPayViewModel", "✅ Response body: $wrapper")
                    onResult(wrapper?.zalopay)
                } else {
                    Log.e("ZaloPayViewModel", "❌ Error response: ${response.errorBody()?.string()}")
                    onResult(null)
                }
            } catch (e: Exception) {
                Log.e("ZaloPayViewModel", "🔥 Exception: ${e.message}", e)
                onResult(null)
            }
        }
    }

}





