package com.example.datn

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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

        val selectedItems: List<CartItem> = if (!selectedItemsJson.isNullOrEmpty()) {
            val type = object : TypeToken<List<CartItem>>() {}.type
            Gson().fromJson(selectedItemsJson, type)
        } else emptyList()

        val sharedPref = getSharedPreferences("auth", MODE_PRIVATE)
        val userId = sharedPref.getString("userId", null)

        setContent {
            DATNTheme {
                if (userId != null) {
                    val viewContext = LocalContext.current
                    var user by remember { mutableStateOf<User?>(null) }
                    var selectedMethod by remember { mutableStateOf("") }
                    var showSuccessDialog by remember { mutableStateOf(false) }
                    var isPlacing by remember { mutableStateOf(false) }

                    LaunchedEffect(Unit) {
                        try {
                            val response = RetrofitClient.apiService.getUsers()
                            if (response.isSuccessful) {
                                val users = response.body() ?: emptyList()
                                user = users.find { it._id == userId }
                            }
                        } catch (_: Exception) {}
                    }

                    user?.let { currentUser ->
                        val context = LocalContext.current

                        OrderContent(
                            selectedItems = selectedItems,
                            user = currentUser,
                            selectedMethod = selectedMethod,
                            onMethodChange = { selectedMethod = it },
                            isPlacing = isPlacing,
                            onPlaceOrder = {
                                if (selectedMethod.isEmpty()) {
                                    Toast.makeText(context, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show()
                                    return@OrderContent
                                }

                                val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                                val currentDate = dateFormat.format(Date())

                                val orderItems = selectedItems.map { ci ->
                                    OrderItem(
                                        orderDetailId = UUID.randomUUID().toString(),
                                        productId = ci.productId,
                                        name = ci.name,
                                        image = ci.image,
                                        price = ci.price,
                                        quantity = ci.quantity,
                                        size = ci.size ?: "",
                                        color = ci.color ?: "",
                                        subtotal = ci.price * ci.quantity,
                                        date = currentDate,
                                        paymentMethod = selectedMethod,
                                        customerName = currentUser.name,
                                        customerPhone = currentUser.phone,
                                        customerAddress = currentUser.address
                                    )
                                }

                                val itemsTotal = orderItems.sumOf { it.subtotal }

                                val newOrder = Order(
                                    userId = currentUser._id!!,
                                    total = itemsTotal,
                                    paymentMethod = selectedMethod,
                                    status = null,
                                    items = orderItems
                                )

                                val json = Gson().toJson(newOrder)
                                android.util.Log.d("ORDER_JSON", json)

                                isPlacing = true
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val response = RetrofitClient.apiService.createOrder(newOrder)
                                        if (response.isSuccessful) {
                                            selectedItems.forEach {
                                                try {
                                                    RetrofitClient.apiService.deleteCartItemById(it.itemId)
                                                } catch (_: Exception) {}
                                            }
                                            withContext(Dispatchers.Main) {
                                                isPlacing = false
                                                showSuccessDialog = true
                                            }
                                        } else {
                                            val errBody = response.errorBody()?.string()
                                            android.util.Log.e("ORDER_ERR", "code=${response.code()} body=$errBody")
                                            withContext(Dispatchers.Main) {
                                                isPlacing = false
                                                Toast.makeText(context, "Lỗi đặt hàng: ${response.code()}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } catch (e: Exception) {
                                        android.util.Log.e("ORDER_EX", e.message ?: "exception")
                                        withContext(Dispatchers.Main) {
                                            isPlacing = false
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
                                        .background(Color.White, shape = RoundedCornerShape(20.dp))
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .size(60.dp)
                                                .background(color = Color(0xFF4CAF50).copy(alpha = 0.1f), shape = CircleShape),
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
                                        Text("Đặt hàng", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                                        Spacer(modifier = Modifier.height(8.dp))
                                        Text("Bạn đã đặt hàng thành công", textAlign = TextAlign.Center, fontSize = 14.sp, color = Color.Gray)
                                        Spacer(modifier = Modifier.height(24.dp))
                                        Button(
                                            onClick = {
                                                val intent = Intent(context, Home::class.java)
                                                context.startActivity(intent)
                                            },
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(12.dp),
                                            colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black)
                                        ) {
                                            Text("Về trang chủ", color = Color.White)
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
    user: User,
    selectedMethod: String,
    onMethodChange: (String) -> Unit,
    isPlacing: Boolean,
    onPlaceOrder: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val totalProductPrice = selectedItems.sumOf { it.price * it.quantity }
    val grandTotal = totalProductPrice // Không cộng phí ship

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
            Column(modifier = Modifier.fillMaxWidth().background(Color.White).padding(16.dp)) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tổng tiền")
                    Text("${totalProductPrice.toDecimalString()} VND")
                }
                Divider(Modifier.padding(vertical = 8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tổng", fontWeight = FontWeight.Bold)
                    Text("${grandTotal.toDecimalString()} VND", color = Color(0xFFd32f2f), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { if (selectedItems.isEmpty()) Toast.makeText(context, "Vui lòng chọn sản phẩm", Toast.LENGTH_SHORT).show() else onPlaceOrder() },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isPlacing,
                    colors = ButtonDefaults.buttonColors(backgroundColor = Color.Black)
                ) {
                    Text(if (isPlacing) "Đang xử lý..." else "Đặt hàng", color = Color.White)
                }
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
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
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    paymentMethods.forEach { label ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onMethodChange(label) }
                        ) {
                            RadioButton(
                                selected = selectedMethod == label,
                                onClick = { onMethodChange(label) },
                                colors = RadioButtonDefaults.colors(selectedColor = Color.Black, unselectedColor = Color.Gray)
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
                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp)) {
                    Image(
                        painter = rememberAsyncImagePainter(item.image),
                        contentDescription = item.name,
                        modifier = Modifier.size(90.dp).clip(RoundedCornerShape(12.dp)),
                        contentScale = androidx.compose.ui.layout.ContentScale.Crop
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(item.name, fontWeight = androidx.compose.ui.text.font.FontWeight.SemiBold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                        Text("Size: ${item.size} | Màu: ${item.color}", fontSize = 13.sp, color = Color.Gray)
                        Text("Giá: ${item.price.toDecimalString()} VND", fontSize = 14.sp, color = Color(0xFFe53935), fontWeight = androidx.compose.ui.text.font.FontWeight.Medium)
                        Text("Số lượng: ${item.quantity}", fontSize = 14.sp)
                    }
                }
                Divider(color = Color.LightGray, thickness = 1.dp)
            }
        }