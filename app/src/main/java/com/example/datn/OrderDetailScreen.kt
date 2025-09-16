package com.example.datn

import com.example.datn.ui.theme.DATNTheme
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
import java.text.SimpleDateFormat
import java.util.*
import com.example.datn.utils.toDecimalString

class OrderDetailActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val orderId = intent.getStringExtra("orderId") ?: run {
            Toast.makeText(this, "Không tìm thấy orderId", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // 🔹 Lấy thông tin user từ SharedPreferences
        val prefs = getSharedPreferences("auth", MODE_PRIVATE)
        val userId = prefs.getString("userId", "") ?: ""
        val username = prefs.getString("username", "Người dùng") ?: "Người dùng"
        val avatar = prefs.getString("avatar", "") ?: ""

        val viewModel: OrderDetailViewModel = ViewModelProvider(
            this,
            OrderDetailViewModelFactory(orderId)
        )[OrderDetailViewModel::class.java]

        val reviewLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                viewModel.markOrderAsReviewed()
            }
        }

        setContent {
            DATNTheme {
                val uiState = viewModel.uiState

                OrderDetailScreen(
                    uiState = uiState,
                    onCancelConfirmed = { note -> viewModel.cancelOrder(note) },
                    onBack = { finish() },
                    onReviewed = {
                        val order = uiState.order ?: return@OrderDetailScreen

                        // ✅ Lấy userId ưu tiên từ SharedPreferences
                        val safeUserId = if (userId.isNotBlank()) userId else order.userId

                        // ✅ Đảm bảo orderId không rỗng trước khi mở màn hình đánh giá
                        val safeOrderId = order.orderId.ifBlank {
                            Log.e("OrderDetail", "❌ orderId trống, không thể mở ReviewActivity!")
                            Toast.makeText(
                                this,
                                "Không thể mở đánh giá: orderId trống",
                                Toast.LENGTH_SHORT
                            ).show()
                            return@OrderDetailScreen
                        }

                        Log.d("OrderDetail", "DEBUG mở ReviewActivity với orderId=$safeOrderId, userId=$safeUserId")

                        val intent = Intent(this, ReviewActivity::class.java).apply {
                            putExtra("orderId", safeOrderId) // ✅ luôn là ObjectId thật
                            putExtra("userId", safeUserId)
                            putExtra("username", username)
                            putExtra("avatar", avatar)
                            putExtra("productList", ArrayList(order.items))
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
    onCancelConfirmed: (String) -> Unit,
    onBack: () -> Unit,
    onReviewed: () -> Unit
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current

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
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            when {
                uiState.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }

                uiState.errorMessage != null -> {
                    Text(
                        "Lỗi: ${uiState.errorMessage}",
                        color = Color.Red,
                        modifier = Modifier.align(Alignment.CenterHorizontally)
                    )
                }

                uiState.order != null -> {
                    val order = uiState.order

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
                            Text(order.customerName ?: "Không có tên", fontWeight = FontWeight.Bold)
                            Text(order.customerPhone ?: "Không có số điện thoại")
                            Text(order.customerAddress ?: "Không có địa chỉ")
                        }
                    }

                    // Danh sách sản phẩm
                    if (order.items.isEmpty()) {
                        Text(
                            "Không có sản phẩm",
                            modifier = Modifier.padding(8.dp),
                            color = Color.Gray
                        )
                    } else {
                        order.items.forEach { item ->
                            OrderItemRow(item)
                        }
                    }

                    // Tổng tiền & thanh toán
                    Text(
                        "Thành tiền: ${order.total.toDecimalString()} vnđ",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(8.dp)
                    )
                    Text(
                        "Phương thức thanh toán: ${order.paymentMethod.ifBlank { "Không rõ" }}",
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )

                    // Ngày đặt hàng
                    Text(
                        "Ngày đặt hàng: ${formatOrderDateTime(order.date)}",
                        modifier = Modifier.padding(8.dp)
                    )

                    // Ghi chú đơn hàng
                    if (order.notes.isNotEmpty()) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Text("Ghi chú đơn hàng:", fontWeight = FontWeight.SemiBold)
                            order.notes.forEach { note ->
                                Card(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    colors = CardDefaults.cardColors(
                                        containerColor = Color(0xFFF5F5F5)
                                    )
                                ) {
                                    Text(
                                        text = note.message,
                                        modifier = Modifier.padding(8.dp),
                                        fontStyle = if (note.type == "system") FontStyle.Italic else FontStyle.Normal,
                                        color = when (note.type) {
                                            "user" -> Color.Black
                                            "system" -> Color.Gray
                                            else -> Color.DarkGray
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 👉 Nút đánh giá đơn hàng
                    if (order.status == "Đã giao") {
                        if (!order.isReviewed) {
                            Button(
                                onClick = { onReviewed() },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp)
                            ) {
                                Text("Đánh giá đơn hàng")
                            }
                        } else {
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

                    // 👉 Nút hành động theo trạng thái
                    when (order.status) {
                        "Chờ xác nhận" -> {
                            Button(
                                onClick = { showCancelDialog = true },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                            ) {
                                Text("Huỷ đơn", color = Color.White)
                            }
                        }

                        "Đã huỷ" -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    "Đơn hàng đã huỷ",
                                    color = Color.Red,
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                )
                                if (!order.cancelNote.isNullOrBlank()) {
                                    Text(
                                        "Lý do huỷ: ${order.cancelNote}",
                                        color = Color.Gray,
                                        fontStyle = FontStyle.Italic,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier.padding(top = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 👉 AlertDialog xác nhận huỷ
        if (showCancelDialog) {
            var cancelNote by remember { mutableStateOf("") }

            AlertDialog(
                onDismissRequest = { showCancelDialog = false },
                title = { Text("Lý do huỷ đơn") },
                text = {
                    Column {
                        Text("Vui lòng nhập lý do huỷ đơn hàng:")
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = cancelNote,
                            onValueChange = { newValue ->
                                if (newValue.length <= 100) cancelNote = newValue
                            },
                            placeholder = { Text("...") },
                            modifier = Modifier.fillMaxWidth(),
                            maxLines = 3,
                            shape = RoundedCornerShape(8.dp)
                        )
                        Spacer(Modifier.height(4.dp))
                        Box(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "${cancelNote.length}/100 ký tự",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                modifier = Modifier.align(Alignment.CenterEnd)
                            )
                        }
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            if (cancelNote.isBlank()) {
                                Toast.makeText(context, "Vui lòng nhập lý do", Toast.LENGTH_SHORT).show()
                            } else {
                                showCancelDialog = false
                                onCancelConfirmed(cancelNote)
                            }
                        }
                    ) {
                        Text("Xác nhận huỷ", color = Color.Red)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showCancelDialog = false }) {
                        Text("Đóng")
                    }
                }
            )
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
                "${item.price?.toDecimalString() ?: "0"} VND",
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

fun formatOrderDateTime(rawDate: String?): String {
    if (rawDate.isNullOrBlank()) return "--/--/---- --:--:--"
    return try {
        val parser = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault()).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date: Date? = parser.parse(rawDate)
        val formatter = SimpleDateFormat("dd-MM-yyyy HH:mm:ss", Locale.getDefault())
        date?.let { formatter.format(it) } ?: "--/--/---- --:--:--"
    } catch (e: Exception) {
        "--/--/---- --:--:--"
    }
}


