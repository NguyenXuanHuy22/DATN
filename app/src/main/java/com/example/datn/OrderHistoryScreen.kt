package com.example.datn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.sp
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
        "Đã xác nhận",
        "Đang giao hàng",
        "Đã giao",
        "Đã huỷ"
    )

    var selectedTab by remember { mutableStateOf(statusList.first()) }
    val filteredOrders = orders.filter { it.status == selectedTab }

    LaunchedEffect(userId) {
        viewModel.loadOrders(userId)
    }

    Column(modifier = Modifier.fillMaxSize().padding(8.dp)) {
        // Thanh tiêu đề
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

        // Menu trạng thái
        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(statusList) { status ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .clickable {
                            selectedTab = status
                            viewModel.loadOrders(userId) // Load lại dữ liệu khi đổi tab
                        }
                ) {
                    Text(
                        text = status,
                        fontWeight = if (selectedTab == status) FontWeight.Bold else FontWeight.Normal,
                        color = Color.Black
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (selectedTab == status) {
                        Box(
                            modifier = Modifier
                                .height(2.dp)
                                .width(24.dp)
                                .background(Color.Black, shape = RoundedCornerShape(1.dp))
                        )
                    } else {
                        Spacer(modifier = Modifier.height(2.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Danh sách đơn hàng
        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filteredOrders) { order ->
                OrderCard(order = order)
            }
        }
    }
}

@Composable
fun OrderCard(order: Order) {
    val totalQuantity = order.items.sumOf { it.quantity }

    Column(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        // Trạng thái đơn hàng ở trên cùng
        Text(
            text = order.status ?: "",
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        // Hiển thị tất cả sản phẩm
        order.items.forEach { item ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                AsyncImage(
                    model = item.image ?: "",
                    contentDescription = null,
                    modifier = Modifier.size(70.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(item.name ?: "", fontWeight = FontWeight.Bold)
                    Text("Size ${item.size ?: ""}")
                    Text("Màu ${item.color ?: ""}")
                    Text("x${item.quantity}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${item.subtotal.toDecimalString()}đ",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFe53935)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Hiển thị tổng số sản phẩm và tổng tiền đơn hàng
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "Tổng sản phẩm: $totalQuantity",
                fontWeight = FontWeight.Medium,
                fontSize = 14.sp,
                color = Color.Gray
            )
            Text(
                text = "Tổng tiền: ${order.total.toDecimalString()}đ",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,
                color = Color(0xFFd32f2f)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Nút hành động
        if (order.status == "Đã giao") {
            Button(
                onClick = { /* Navigate to review */ },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
            ) {
                Text("Đánh giá", color = Color.White)
            }
        } else if (order.status == "Đã huỷ") {
            // Có thể thêm hành động khác nếu cần
        }
    }
}





