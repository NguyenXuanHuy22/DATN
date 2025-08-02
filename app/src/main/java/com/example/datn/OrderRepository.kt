package com.example.datn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.datn.ui.theme.DATNTheme

class OrderRepository {
    suspend fun getOrdersByUser(userId: String): List<Order> {
        val response = RetrofitClient.apiService.getOrders()
        if (response.isSuccessful) {
            return response.body()?.filter { it.userId == userId } ?: emptyList()
        }
        return emptyList()
    }
}


