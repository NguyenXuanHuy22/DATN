package com.example.datn

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

class VNPayViewModel(
    private val api: ProductService // truyền từ DI hoặc RetrofitClient.apiService
) : ViewModel() {

    fun createPayment(
        userId: String,
        items: List<OrderItem>,
        customerName: String,
        customerPhone: String,
        customerAddress: String,
        addressId: String? = null,
        date: String? = null,
        onResult: (PaymentUrlResponse?) -> Unit,
        onError: (String) -> Unit
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val req = VnpCreatePaymentRequest(
                    userId = userId,
                    items = items,
                    customerName = customerName,
                    customerPhone = customerPhone,
                    customerAddress = customerAddress,
                    addressId = addressId,
                    date = date
                )
                val resp = api.createVnpayPayment(req)
                withContext(Dispatchers.Main) {
                    if (resp.isSuccessful) {
                        onResult(resp.body())
                    } else {
                        onError("Tạo link VNPay lỗi: ${resp.code()}")
                    }
                }
            } catch (e: HttpException) {
                withContext(Dispatchers.Main) { onError("HTTP ${e.code()}") }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) { onError("Lỗi: ${e.message}") }
            }
        }
    }
}