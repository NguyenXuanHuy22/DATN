package com.example.datn

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BannerViewModel : ViewModel() {
    var banners by mutableStateOf(listOf<Banner>())
        private set

    var currentIndex by mutableStateOf(0)
        private set

    var loading by mutableStateOf(false)
    var error by mutableStateOf("")

    fun loadBanners() {
        viewModelScope.launch {
            loading = true
            try {
                banners = RetrofitClient.apiService.getBanners()
                error = ""
                startAutoSlide()
            } catch (e: Exception) {
                error = e.message ?: "Lỗi tải banner"
            }
            loading = false
        }
    }

    private fun startAutoSlide() {
        viewModelScope.launch {
            while (true) {
                delay(3000) // 3 giây
                if (banners.isNotEmpty()) {
                    currentIndex = (currentIndex + 1) % banners.size
                }
            }
        }
    }
}
