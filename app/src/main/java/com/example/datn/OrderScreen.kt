package com.example.datn

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.rememberAsyncImagePainter
import com.example.datn.ui.theme.DATNTheme
import com.example.datn.utils.toDecimalString
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.*


class OrderScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val selectedItemsJson = intent.getStringExtra("selectedItemsJson")
        val shippingFee = intent.getIntExtra("shippingFee", 0)

        val selectedItems: List<CartItem> = if (!selectedItemsJson.isNullOrEmpty()) {
            val type = object : TypeToken<List<CartItem>>() {}.type
            Gson().fromJson(selectedItemsJson, type)
        } else emptyList()

        val sharedPref = getSharedPreferences("auth", MODE_PRIVATE)
        val userId = sharedPref.getString("userId", null)

        setContent {
            DATNTheme {
                if (userId != null) {
                    var user by remember { mutableStateOf<User?>(null) }
                    var selectedMethod by remember { mutableStateOf("") }
                    var showSuccessDialog by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        val response = RetrofitClient.apiService.getUsers()
                        if (response.isSuccessful) {
                            val users = response.body() ?: emptyList()
                            user = users.find { it.id == userId }
                        }
                    }

                    user?.let { currentUser ->
                        val context = LocalContext.current

                        OrderContent(
                            selectedItems = selectedItems,
                            shippingFee = shippingFee,
                            user = currentUser,
                            selectedMethod = selectedMethod,
                            onMethodChange = { selectedMethod = it },
                            onPlaceOrder = {
                                if (selectedMethod.isEmpty()) {
                                    Toast.makeText(context, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show()
                                    return@OrderContent
                                }

                                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                val currentDate = dateFormat.format(Date())

                                val orderItems = selectedItems.map {
                                    OrderItem(
                                        orderDetailId = UUID.randomUUID().toString(),
                                        productId = it.productId,

                                        name = it.name,
                                        image = it.image,
                                        price = it.price,
                                        quantity = it.quantity,
                                        size = it.size,
                                        color = it.color,
                                        subtotal = it.price * it.quantity,
                                        date = currentDate,
                                        paymentMethod = selectedMethod,
                                        customerName = currentUser.name,
                                        customerPhone = currentUser.phone,
                                        customerAddress = currentUser.address
                                    )
                                }

                                val total = orderItems.sumOf { it.subtotal } + shippingFee
                                val newOrder = Order(
                                    id = UUID.randomUUID().toString(),
                                    userId = currentUser.id,
                                    total = total,
                                    status = "Chờ Xác nhận",
                                    items = orderItems
                                )

                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val response = RetrofitClient.apiService.addOrder(newOrder)
                                        if (response.isSuccessful) {
                                            selectedItems.forEach {
                                                RetrofitClient.apiService.deleteCartItemById(it.itemId)
                                            }
                                            withContext(Dispatchers.Main) {
                                                showSuccessDialog = true
                                            }
                                        } else {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, "Lỗi đặt hàng", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(context, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            onBack = { finish() }
                        )
                                if (showSuccessDialog) {
                                    Dialog(onDismissRequest = { showSuccessDialog = false }) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(horizontal = 32.dp)
                                                .background(
                                                    Color.White,
                                                    shape = RoundedCornerShape(20.dp)
                                                )
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(24.dp),
                                                horizontalAlignment = Alignment.CenterHorizontally
                                            ) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(60.dp)
                                                        .background(
                                                            color = Color(0xFF4CAF50).copy(
                                                                alpha = 0.1f
                                                            ), shape = CircleShape
                                                        ),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = Color(0xFF4CAF50),
                                                        modifier = Modifier.size(40.dp)
                                                    )
                                                }

                                                Spacer(modifier = Modifier.height(16.dp))

                                                Text(
                                                    "Đặt hàng",
                                                    fontWeight = FontWeight.Bold,
                                                    fontSize = 18.sp,
                                                    color = Color.Black
                                                )

                                                Spacer(modifier = Modifier.height(8.dp))

                                                Text(
                                                    text = "Bạn đã đặt hàng thành công",
                                                    textAlign = TextAlign.Center,
                                                    fontSize = 14.sp,
                                                    color = Color.Gray
                                                )

                                                Spacer(modifier = Modifier.height(24.dp))

                                                Button(
                                                    onClick = {
                                                        showSuccessDialog = false
                                                        finish()
                                                    },
                                                    modifier = Modifier.fillMaxWidth(),
                                                    shape = RoundedCornerShape(12.dp),
                                                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black)
                                                ) {
                                                    Text("Về giỏ hàng", color = Color.White)
                                                }
                                            }
                                        }
                                    }
                                }

                    }
                }
            }
        }
    }
}


@Composable
fun OrderContent(
    selectedItems: List<CartItem>,
    shippingFee: Int,
    user: User,
    selectedMethod: String,
    onMethodChange: (String) -> Unit,
    onPlaceOrder: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val totalProductPrice = selectedItems.sumOf { it.price * it.quantity }
    val grandTotal = totalProductPrice + shippingFee

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đặt hàng", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                backgroundColor = Color.White
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Tổng tiền")
                    Text("${totalProductPrice.toDecimalString()} VND")
                }
                Spacer(Modifier.height(4.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Phí vận chuyển")
                    Text(if (shippingFee == 0) "Miễn phí" else "${shippingFee.toDecimalString()} VND")
                }
                Divider(Modifier.padding(vertical = 8.dp))
                Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween) {
                    Text("Tổng", fontWeight = FontWeight.Bold)
                    Text("${grandTotal.toDecimalString()} VND", color = Color(0xFFd32f2f), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (selectedItems.isEmpty()) {
                            Toast.makeText(context, "Vui lòng chọn sản phẩm", Toast.LENGTH_SHORT).show()
                        } else {
                            onPlaceOrder()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(Color.Black)
                ) {
                    Text("Đặt hàng", color = Color.White)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Địa chỉ", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Default.LocationOn, contentDescription = null)
                    Column(modifier = Modifier.padding(start = 8.dp)) {
                        Text(user.name, fontWeight = FontWeight.Bold)
                        Text(user.phone)
                        Text(user.address)
                    }
                    Spacer(Modifier.weight(1f))
                    Text("Thêm", color = Color(0xFF1976D2))
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Phương thức thanh toán", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

                val paymentMethods = listOf("Thanh toán khi nhận hàng")
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    paymentMethods.forEach { label ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(8.dp))
                        ) {
                            RadioButton(
                                selected = selectedMethod == label,
                                onClick = { onMethodChange(label) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color.Black,
                                    unselectedColor = Color.Gray
                                )
                            )
                            Text(label, modifier = Modifier.padding(start = 4.dp))
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            items(selectedItems) { item ->
                OrderItemRow(item)
            }
        }
    }
}

@Composable
fun OrderItemRow(item: CartItem) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            Image(
                painter = rememberAsyncImagePainter(item.image),
                contentDescription = item.name,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp)),
                contentScale = ContentScale.Crop
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.name, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                Text("Size: ${item.size} | Màu: ${item.color}", fontSize = 13.sp, color = Color.Gray)
                Text("Giá: ${item.price.toDecimalString()} VND", fontSize = 14.sp, color = Color(0xFFe53935), fontWeight = FontWeight.Medium)
                Text("Số lượng: ${item.quantity}", fontSize = 14.sp)
            }
        }
        Divider(color = Color.LightGray, thickness = 1.dp)
    }
}
