package com.example.datn

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DoneOutline
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModelProvider
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.datn.ui.theme.DATNTheme
import com.example.datn.utils.toDecimalString
import java.text.SimpleDateFormat
import java.util.*

class OrderDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val orderId = intent.getStringExtra("orderId") ?: run {
            Toast.makeText(this, "Không tìm thấy orderId", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // SharedPreferences lấy thông tin user
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val userId = prefs.getString("userId", "") ?: ""
        val username = prefs.getString("username", "Người dùng") ?: "Người dùng"
        val avatar = prefs.getString("avatar", "") ?: ""

        val viewModel: OrderDetailViewModel = ViewModelProvider(
            this,
            OrderDetailViewModelFactory(orderId)
        )[OrderDetailViewModel::class.java]

        // RegisterForActivityResult để chờ ReviewActivity trả kết quả
        val reviewLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                viewModel.markOrderAsReviewed()
            }
        }

        setContent {
            DATNTheme {
                OrderDetailScreen(
                    uiState = viewModel.uiState,
                    onCancelConfirmed = { viewModel.cancelOrder() },
                    onBack = { finish() },
                    onReviewed = { productId ->
                        val intent = Intent(this, ReviewActivity::class.java).apply {
                            putExtra("productId", productId)
                            putExtra("orderId", orderId)
                            putExtra("userId", userId)
                            putExtra("username", username)
                            putExtra("avatar", avatar)
                        }
                        reviewLauncher.launch(intent)
                    }
                )
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OrderDetailScreen(
    uiState: OrderDetailUiState,
    onCancelConfirmed: () -> Unit,
    onBack: () -> Unit,
    onReviewed: (String) -> Unit
) {
    var showCancelDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thông tin đơn hàng") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            when {
                uiState.isLoading -> CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))

                uiState.errorMessage != null -> Text(
                    "Lỗi: ${uiState.errorMessage}",
                    color = Color.Red,
                    modifier = Modifier.align(Alignment.Center)
                )

                uiState.order != null -> {
                    val order = uiState.order
                    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                        // Trạng thái
                        Text(
                            "Trạng thái: ${order.status ?: "Không xác định"}",
                            color = when (order.status) {
                                "Đã huỷ" -> Color.Red
                                "Đã giao" -> Color.Green
                                else -> Color.Black
                            },
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(8.dp)
                        )
                        OrderStatusProgressBar(order.status ?: "")

                        // Thông tin khách hàng
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(8.dp)
                        ) {
                            Column(modifier = Modifier.padding(8.dp)) {
                                Text(text = order.customerName ?: "Không có tên", fontWeight = FontWeight.Bold)
                                Text(text = order.customerPhone ?: "Không có số điện thoại")
                                Text(text = order.customerAddress ?: "Không có địa chỉ")
                            }
                        }

                        // Danh sách sản phẩm
                        if (order.items.isNullOrEmpty()) {
                            Text(
                                "Không có sản phẩm",
                                modifier = Modifier.padding(8.dp),
                                color = Color.Gray
                            )
                        } else {
                            order.items.forEach { item ->
                                OrderItemRow(item)
                            }

                            // Nút đánh giá đơn hàng ở cuối danh sách
                            if (order.status == "Đã giao" && order.isReviewed != true) {
                                Button(
                                    onClick = {
                                        // chỉ cần truyền productId của sản phẩm đầu tiên (hoặc null)
                                        val firstProductId = order.items.firstOrNull()?.productId ?: ""
                                        onReviewed(firstProductId)
                                    },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Text("Đánh giá đơn hàng")
                                }
                            }

                            // Nếu đã đánh giá thì hiện thông báo
                            if (order.status == "Đã giao" && order.isReviewed == true) {
                                Text(
                                    "Bạn đã đánh giá đơn hàng này",
                                    color = Color.Gray,
                                    fontStyle = FontStyle.Italic,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }

                        // Tổng tiền & thanh toán
                        Text(
                            "Thành tiền: ${order.total?.toDecimalString() ?: "0"} đ",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(8.dp)
                        )
                        Text(
                            "Phương thức thanh toán: ${order.paymentMethod ?: "Không rõ"}",
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )

                        // Ngày đặt hàng
                        Text(
                            "Ngày đặt hàng: ${formatOrderDate(order.date)}",
                            modifier = Modifier.padding(8.dp)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Nút hành động theo trạng thái
                        when (order.status) {
                            "Chờ xác nhận" -> {
                                Button(
                                    onClick = { showCancelDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                ) {
                                    Text("Huỷ đơn")
                                }
                            }

                            "Đã huỷ" -> {
                                Text(
                                    "Đơn hàng đã huỷ",
                                    color = Color.Red,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }

            // AlertDialog xác nhận huỷ
            if (showCancelDialog) {
                AlertDialog(
                    onDismissRequest = { showCancelDialog = false },
                    title = { Text("Xác nhận") },
                    text = { Text("Bạn có chắc chắn muốn huỷ đơn hàng này không?") },
                    confirmButton = {
                        TextButton(
                            onClick = {
                                showCancelDialog = false
                                onCancelConfirmed()
                            }
                        ) {
                            Text("Huỷ đơn")
                        }
                    },
                    dismissButton = {
                        TextButton(onClick = { showCancelDialog = false }) {
                            Text("Hủy")
                        }
                    }
                )
            }
        }
    }
}


@Composable
fun OrderItemRow(item: OrderItem) {
    val context = LocalContext.current

    val imageRequest = when {
        item.image.isNullOrEmpty() -> null
        item.image.startsWith("data:image") -> {
            val pureBase64 = item.image.substringAfter("base64,", "")
            val decodedBytes = try {
                Base64.decode(pureBase64, Base64.DEFAULT)
            } catch (_: Exception) {
                null
            }
            decodedBytes?.let {
                ImageRequest.Builder(context)
                    .data(it)
                    .crossfade(true)
                    .build()
            }
        }
        else -> ImageRequest.Builder(context).data(item.image).crossfade(true).build()
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (imageRequest != null) {
            AsyncImage(
                model = imageRequest,
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp)),
                placeholder = painterResource(id = R.drawable.logo),
                error = painterResource(id = R.drawable.logo)
            )
        } else {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "Placeholder",
                modifier = Modifier.size(60.dp).clip(RoundedCornerShape(8.dp))
            )
        }

        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                item.name ?: "Không rõ sản phẩm",
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text("Size: ${item.size ?: "-"}, Màu: ${item.color ?: "-"}")
            Text(
                "${item.price?.toDecimalString() ?: "0"} vnđ",
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun OrderStatusProgressBar(status: String) {
    val steps = listOf("Chờ xác nhận", "Đã xác nhận", "Đang giao hàng", "Đã giao")
    val icons = listOf(
        Icons.Default.Schedule,
        Icons.Default.Check,
        Icons.Default.LocalShipping,
        Icons.Default.DoneOutline
    )
    val currentIndex = steps.indexOf(status).takeIf { it >= 0 } ?: 0

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        steps.forEachIndexed { index, _ ->
            // Circle icon
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        if (index <= currentIndex) Color(0xFF4CAF50) else Color.Gray,
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icons[index],
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }

            // Line nối sang phải
            if (index < steps.lastIndex) {
                Spacer(
                    modifier = Modifier
                        .height(3.dp)
                        .width(50.dp)
                        .background(
                            if (index < currentIndex) Color(0xFF4CAF50) else Color.Gray
                        )
                )
            }
        }
    }
}

fun formatOrderDate(rawDate: String?): String {
    if (rawDate.isNullOrBlank()) return "--/--/----"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date: Date? = parser.parse(rawDate)
        val formatter = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        date?.let { formatter.format(it) } ?: "--/--/----"
    } catch (e: Exception) {
        "--/--/----"
    }
}

