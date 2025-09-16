// OrderHistoryScreen.kt
package com.example.datn

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.datn.ui.theme.DATNTheme
import com.example.datn.utils.toDecimalString

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
                    // Có thể thay bằng Composable đẹp hơn
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

        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            items(statusList) { status ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        selectedTab = status
                        viewModel.loadOrders(userId)
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
                    }
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
    val context = LocalContext.current
    val totalQuantity = order.items.sumOf { it.quantity }

    Column(
        modifier = Modifier
            .padding(vertical = 8.dp)
            .fillMaxWidth()
            .background(Color.White, shape = RoundedCornerShape(12.dp))
            .border(1.dp, Color.LightGray, RoundedCornerShape(12.dp))
            .padding(12.dp)
    ) {
        Text(
            text = order.status,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )

        Spacer(modifier = Modifier.height(8.dp))

        order.items.forEach { item ->
            val imageRequest = remember(item.image) {
                item.image.takeIf { it.isNotEmpty() }?.let { img ->
                    if (img.startsWith("data:image")) {
                        val pureBase64 = img.substringAfter("base64,")
                        val decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT)
                        ImageRequest.Builder(context)
                            .data(decodedBytes)
                            .crossfade(true)
                            .build()
                    } else {
                        ImageRequest.Builder(context)
                            .data(img)
                            .crossfade(true)
                            .build()
                    }
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                if (imageRequest != null) {
                    AsyncImage(
                        model = imageRequest,
                        contentDescription = item.name,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .width(90.dp)
                            .height(90.dp)
                            .clip(RoundedCornerShape(12.dp)),
                        placeholder = painterResource(id = R.drawable.logo),
                        error = painterResource(id = R.drawable.logo)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.logo),
                        contentDescription = "Placeholder",
                        modifier = Modifier
                            .size(90.dp)
                            .clip(RoundedCornerShape(12.dp))
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    // ✅ Giới hạn tên sản phẩm 1 dòng thôi
                    Text(
                        item.name,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text("Size ${item.size}")
                    Text("Màu ${item.color}")
                    Text("x${item.quantity}")
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        "${item.subtotal.toDecimalString()}đ",
                        fontWeight = FontWeight.Bold,

                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

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
                text = "Tổng tiền: ${order.total.toDecimalString()}VND",
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp,

            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        //  Thêm nút Xem chi tiết
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End // đẩy phần tử sang phải
        ) {
            Button(
                onClick = {
                    val intent = Intent(context, OrderDetailActivity::class.java)
                    intent.putExtra("orderId", order.orderId)
                    context.startActivity(intent)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White, // nền trắng
                    contentColor = Color.Black    // chữ đen
                ),
                shape = RoundedCornerShape(12.dp), // bo góc mềm
                border = BorderStroke(1.dp, Color.Black) // viền đen
            ) {
                Text("Xem chi tiết", fontSize = 14.sp, fontWeight = FontWeight.Medium)
            }
        }


    }
}

