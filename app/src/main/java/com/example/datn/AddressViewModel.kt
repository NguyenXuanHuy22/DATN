package com.example.datn

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class AddressViewModel : ViewModel() {
    var userAddress = mutableStateOf<List<Address>>(emptyList()) // chứa 1 item mặc định
        private set

    var addresses = mutableStateOf<List<Address>>(emptyList())
        private set

    var isLoading = mutableStateOf(false)
        private set

    var errorMessage = mutableStateOf("")
        private set

    fun loadUserAddress(user: User) {
        // Chỉ tạo 1 item list từ thông tin user
        userAddress.value = listOf(
            Address(
                _id = "user",
                userId = user._id ?: "",
                name = user.name ?: "",
                address = user.address ?: "",
                phone = user.phone ?: ""
            )
        )
    }

    fun loadAddresses(userId: String) {
        if (userId.isBlank()) {
            errorMessage.value = "UserId trống"
            return
        }

        viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = ""
            try {
                val response = RetrofitClient.addressService.getAddresses(userId)
                if (response.isSuccessful) {
                    addresses.value = response.body() ?: emptyList()
                } else {
                    errorMessage.value = "Server trả lỗi: ${response.code()} ${response.message()}"
                }
            } catch (e: Exception) {
                errorMessage.value = "Lỗi: ${e.message}"
            } finally {
                isLoading.value = false
            }
        }
    }
}

