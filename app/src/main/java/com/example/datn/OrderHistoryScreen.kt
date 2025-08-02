package com.example.datn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.datn.ui.theme.DATNTheme
import com.example.datn.utils.toDecimalString
import kotlinx.coroutines.delay

class OrderHistoryScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val sharedPref = getSharedPreferences("auth", MODE_PRIVATE)
        val userId = sharedPref.getString("userId", null)

        setContent {
            DATNTheme {
                if (userId != null) {
                    OrderHistoryContent(
                        userId = userId,
                        onBack = { finish() }
                    )
                } else {
                    Text("Không tìm thấy thông tin người dùng. Vui lòng đăng nhập.")
                }
            }
        }
    }
}

@Composable
fun OrderHistoryContent(
    userId: String,
    onBack: () -> Unit = {}
) {
    val viewModel: OrderHistoryViewModel = viewModel()
    val orders by viewModel.orders.collectAsState()

    val statusList = listOf(
        "Chờ xác nhận",
        "Đã xác nhận đơn hàng",
        "Đang chuẩn bị đơn hàng",
        "Đang giao hàng",
        "Đã giao",
        "Đã huỷ"
    )

    var selectedTab by remember { mutableStateOf(statusList.first()) }

    val filteredOrders = orders.filter { it.status == selectedTab }

    LaunchedEffect(userId) {
        while (true) {
            viewModel.loadOrders(userId)
            delay(5000L)
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
            }
            Text(
                text = "Lịch sử mua hàng",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(statusList) { status ->
                OutlinedButton(
                    onClick = { selectedTab = status },
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (selectedTab == status) Color.Black else Color.LightGray
                    )
                ) {
                    Text(
                        text = status,
                        color = Color.White,
                        fontSize = MaterialTheme.typography.labelMedium.fontSize
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredOrders) { order ->
                OrderCard(order = order)
            }
        }
    }
}

@Composable
fun OrderCard(order: Order) {
    val firstItem = order.items.firstOrNull()

    Column(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        if (firstItem != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AsyncImage(
                    model = firstItem.image,
                    contentDescription = null,
                    modifier = Modifier.size(70.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(firstItem.name, fontWeight = FontWeight.Bold)
                    Text("Size ${firstItem.size}")
                    Text("Màu ${firstItem.color}")
                    Text("x${firstItem.quantity}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${firstItem.subtotal.toDecimalString()}đ",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFe53935)
                    )
                }

                val statusColor = when (order.status) {
                    "Đã giao" -> Color(0xFF4CAF50)
                    "Đã huỷ" -> Color.Red
                    else -> Color.Gray
                }

                Box(
                    modifier = Modifier
                        .background(statusColor, shape = RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        order.status,
                        color = Color.White,
                        fontSize = MaterialTheme.typography.labelSmall.fontSize
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (order.status == "Đã giao") {
                Button(
                    onClick = { /* Navigate to review */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Đánh giá", color = Color.White)
                }
            } else if (order.status == "Đã huỷ") {
                Button(
                    onClick = { /* Mua lại */ },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) {
                    Text("Mua lại", color = Color.White)
                }
            }
        }
    }
}
