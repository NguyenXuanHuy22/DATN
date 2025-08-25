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

    fun addAddress(address: Address, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val res = RetrofitClient.addressService.addAddress(address)
                if (res.isSuccessful) {
                    // Reload list for this user
                    loadAddresses(address.userId)
                    onDone?.let { it() }
                } else {
                    errorMessage.value = "Thêm địa chỉ thất bại: ${res.code()}"
                }
            } catch (e: Exception) {
                errorMessage.value = "Lỗi: ${e.message}"
            }
        }
    }

    fun updateAddress(id: String, address: Address, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val res = RetrofitClient.addressService.updateAddress(id, address)
                if (res.isSuccessful) {
                    loadAddresses(address.userId)
                    onDone?.invoke()
                } else {
                    errorMessage.value = "Cập nhật thất bại: ${res.code()}"
                }
            } catch (e: Exception) {
                errorMessage.value = "Lỗi: ${e.message}"
            }
        }
    }

    fun deleteAddress(id: String, userId: String, onDone: (() -> Unit)? = null) {
        viewModelScope.launch {
            try {
                val res = RetrofitClient.addressService.deleteAddress(id)
                if (res.isSuccessful) {
                    loadAddresses(userId)
                    onDone?.invoke()
                } else {
                    errorMessage.value = "Xóa thất bại: ${res.code()}"
                }
            } catch (e: Exception) {
                errorMessage.value = "Lỗi: ${e.message}"
            }
        }
    }

    fun setDefaultAddress(target: Address, onDone: (() -> Unit)? = null) {
        // Backend ideally ensures one default; here we send update with isDefault=true
        updateAddress(target._id, target.copy(isDefault = true)) {
            onDone?.invoke()
        }
    }
}

