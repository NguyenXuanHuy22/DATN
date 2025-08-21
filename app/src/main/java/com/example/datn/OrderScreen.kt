package com.example.datn

import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
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
                    val context = LocalContext.current
                    var user by remember { mutableStateOf<User?>(null) }
                    var selectedAddress by remember { mutableStateOf<Address?>(null) }
                    var selectedMethod by remember { mutableStateOf("") }
                    var showSuccessDialog by remember { mutableStateOf(false) }
                    var isPlacing by remember { mutableStateOf(false) }

                    // --- Load user & address ---
                    LaunchedEffect(userId) {
                        if (userId.isNullOrEmpty()) return@LaunchedEffect

                        try {
                            // --- Load user ---
                            val userResp = RetrofitClient.apiService.getUsers()
                            if (userResp.isSuccessful) {
                                val users = userResp.body() ?: emptyList()
                                user = users.find { it._id == userId }
                            } else {
                                Log.e("OrderScreen", "Failed to load users: ${userResp.code()}")
                            }

                            // --- Load default address ---
                            val addrResp = RetrofitClient.addressService.getDefaultAddress(userId)
                            if (addrResp.isSuccessful) {
                                selectedAddress = addrResp.body()
                            }

                            // --- Fallback: nếu chưa có address, lấy từ user.address ---
                            if (selectedAddress == null && user != null) {
                                selectedAddress = Address(
                                    _id = "temp",
                                    userId = user!!._id ?: "",
                                    name = user!!.name ?: "",
                                    phone = user!!.phone ?: "",
                                    address = user!!.address ?: "",
                                )
                                Log.d("OrderScreen", "Using fallback address from user: $selectedAddress")
                            }

                        } catch (e: Exception) {
                            Log.e("OrderScreen", "Error loading user/address: ${e.message}")
                        }
                    }

                    user?.let { currentUser ->

                        OrderContent(
                            selectedItems = selectedItems,
                            selectedAddress = selectedAddress,
                            onAddressSelect = { selectedAddress = it },
                            selectedMethod = selectedMethod,
                            onMethodChange = { selectedMethod = it },
                            isPlacing = isPlacing,
                            onPlaceOrder = {
                                if (selectedMethod.isEmpty()) {
                                    Toast.makeText(context, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show()
                                    return@OrderContent
                                }

                                if (selectedAddress == null) {
                                    Toast.makeText(context, "Vui lòng chọn địa chỉ giao hàng", Toast.LENGTH_SHORT).show()
                                    return@OrderContent
                                }

                                val orderItems = selectedItems.map { ci ->
                                    OrderItem(
                                        orderDetailId = UUID.randomUUID().toString(),
                                        productId = ci.productId ?: "",
                                        name = ci.name ?: "",
                                        image = ci.image ?: "",
                                        price = ci.price,
                                        quantity = ci.quantity,
                                        size = ci.size ?: "",
                                        color = ci.color ?: "",
                                        subtotal = ci.price * ci.quantity
                                    )
                                }

                                val itemsTotal = orderItems.sumOf { it.subtotal }

                                val newOrder = Order(
                                    orderId = "", // backend sẽ sinh ra
                                    userId = currentUser._id ?: "",
                                    items = orderItems,
                                    total = itemsTotal,
                                    paymentMethod = selectedMethod,
                                    status = "Chờ xác nhận",
                                    date = "", // backend set date
                                    customerName = selectedAddress?.name ?: "",
                                    customerPhone = selectedAddress?.phone ?: "",
                                    customerAddress = selectedAddress?.address ?: ""
                                )



                                isPlacing = true
                                CoroutineScope(Dispatchers.IO).launch {
                                    try {
                                        val response = RetrofitClient.apiService.createOrder(newOrder)
                                        if (response.isSuccessful) {
                                            selectedItems.forEach {
                                                try {
                                                    RetrofitClient.apiService.deleteCartItemById(it.itemId ?: "")
                                                } catch (_: Exception) {}
                                            }
                                            withContext(Dispatchers.Main) {
                                                isPlacing = false
                                                showSuccessDialog = true
                                            }
                                        } else {
                                            withContext(Dispatchers.Main) {
                                                isPlacing = false
                                                Toast.makeText(context, "Lỗi đặt hàng: ${response.code()}", Toast.LENGTH_SHORT).show()
                                            }
                                        }
                                    } catch (e: Exception) {
                                        withContext(Dispatchers.Main) {
                                            isPlacing = false
                                            Toast.makeText(context, "Lỗi kết nối: ${e.message}", Toast.LENGTH_SHORT).show()
                                        }
                                    }
                                }
                            },
                            onBack = { finish() },
                            addresses = emptyList() // hiện tại không dùng list address
                        )

                        // --- Dialog báo thành công ---
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
    selectedAddress: Address?,
    onAddressSelect: (Address) -> Unit,
    addresses: List<Address>,
    selectedMethod: String,
    onMethodChange: (String) -> Unit,
    isPlacing: Boolean,
    onPlaceOrder: () -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val totalProductPrice = selectedItems.sumOf { it.price * it.quantity }
    val grandTotal = totalProductPrice

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
                if (selectedAddress != null) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                        Icon(Icons.Default.LocationOn, contentDescription = null)
                        Column(modifier = Modifier.padding(start = 8.dp)) {
                            Text(selectedAddress.name, fontWeight = FontWeight.Bold)
                            Text(selectedAddress.phone)
                            Text(selectedAddress.address)
                        }
                        Spacer(Modifier.weight(1f))
                        Text("Thêm", color = Color(0xFF1976D2),
                            modifier = Modifier.clickable {
                                val intent = Intent(context, AddressScreen::class.java)
                                context.startActivity(intent)
                            }
                        )
                    }
                } else {
                    Text("Vui lòng chọn địa chỉ", color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Phương thức thanh toán", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

                val paymentMethods = listOf(
                    "Thanh toán khi nhận hàng",
                    "Chuyển khoản ngân hàng"
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    paymentMethods.forEach { label ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f) // ✅ chia đều không gian
                                .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onMethodChange(label) }
                                .padding(horizontal = 8.dp, vertical = 6.dp) // padding gọn hơn để text không bị tràn
                        ) {
                            RadioButton(
                                selected = selectedMethod == label,
                                onClick = { onMethodChange(label) },
                                colors = RadioButtonDefaults.colors(
                                    selectedColor = Color.Black,
                                    unselectedColor = Color.Gray
                                )
                            )
                            Text(
                                text = label,
                                modifier = Modifier.padding(start = 4.dp),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis // ✅ nếu text quá dài thì "..."
                            )
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
    val context = LocalContext.current

    val imageRequest = remember(item.image) {
        coil.request.ImageRequest.Builder(context)
            .data(
                if (item.image?.startsWith("data:image") == true) {
                    android.util.Base64.decode(item.image.substringAfter("base64,"), android.util.Base64.DEFAULT)
                } else {
                    item.image
                }
            )
            .crossfade(true)
            .build()
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp)
        ) {
            AsyncImage(
                model = imageRequest,
                contentDescription = item.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(12.dp)),
                placeholder = painterResource(id = R.drawable.logo),
                error = painterResource(id = R.drawable.logo)
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    item.name ?: "",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text("Size: ${item.size} | Màu: ${item.color}", fontSize = 13.sp, color = Color.Gray)
                Text(
                    "Giá: ${item.price.toDecimalString()} VND",
                    fontSize = 14.sp,
                    color = Color(0xFFe53935),
                    fontWeight = FontWeight.Medium
                )
                Text("Số lượng: ${item.quantity}", fontSize = 14.sp)
            }
        }
        Divider(color = Color.LightGray, thickness = 1.dp)
    }
}

