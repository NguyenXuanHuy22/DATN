package com.example.datn

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.datn.ui.theme.DATNTheme
import com.example.datn.utils.toDecimalString
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import androidx.compose.ui.window.Dialog
import java.util.*
import android.net.Uri

enum class PaymentMethod(val label: String) {
    COD("Thanh toán khi nhận hàng"),
    ZALOPAY("ZaloPay")
}

class OrderScreen : ComponentActivity() {

    private val paymentResult: MutableState<String?> = mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Parse danh sách sản phẩm đã chọn từ Intent
        val selectedItemsJson = intent.getStringExtra("selectedItemsJson")
        val selectedItems: List<CartItem> = if (!selectedItemsJson.isNullOrEmpty()) {
            val type = object : TypeToken<List<CartItem>>() {}.type
            Gson().fromJson(selectedItemsJson, type)
        } else {
            emptyList()
        }

        // Lấy userId từ SharedPreferences
        val sharedPref = getSharedPreferences("auth", MODE_PRIVATE)
        val userId = sharedPref.getString("userId", null)

        // Check deep link khi Activity được mở
        handleDeepLink(intent)

        setContent {
            DATNTheme {
                if (userId != null) {
                    OrderScreenContent(
                        userId = userId,
                        selectedItems = selectedItems,
                        paymentResult = paymentResult
                    )
                } else {
                    Text("Vui lòng đăng nhập")
                }
            }
        }
    }

    // Bắt sự kiện khi activity đang mở mà deep link redirect lại
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    // Xử lý deep link từ ZaloPay redirect
    private fun handleDeepLink(intent: Intent?) {
        intent?.data?.let { uri ->
            if (uri.scheme == "myapp" && uri.host == "payment") {
                val status = uri.getQueryParameter("status")
                paymentResult.value = status // "success" hoặc "fail"
            }
        }
    }
}

@Composable
fun OrderScreenContent(
    userId: String,
    selectedItems: List<CartItem>,
    paymentResult: MutableState<String?>
) {
    val context = LocalContext.current
    var user by remember { mutableStateOf<User?>(null) }
    var selectedAddress by remember { mutableStateOf<Address?>(null) }
    var selectedMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    var isPlacing by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Load user & default address
    LaunchedEffect(userId) {
        try {
            val usersResp = RetrofitClient.apiService.getUsers()
            if (usersResp.isSuccessful) {
                val users = usersResp.body() ?: emptyList()
                user = users.find { it._id == userId }
            }
            val addrResp = RetrofitClient.addressService.getDefaultAddress(userId)
            if (addrResp.isSuccessful) selectedAddress = addrResp.body()

            if (selectedAddress == null && user != null) {
                selectedAddress = Address(
                    _id = "temp",
                    userId = user!!._id ?: "",
                    name = user!!.name ?: "",
                    phone = user!!.phone ?: "",
                    address = user!!.address ?: ""
                )
            }
        } catch (e: Exception) {
            Log.e("OrderScreen", "Error loading user/address: ${e.message}")
        }
    }

    // Lắng nghe kết quả từ deep link (ZaloPay)
    LaunchedEffect(paymentResult.value) {
        when (paymentResult.value) {
            "success" -> showSuccessDialog = true
            "fail" -> Toast.makeText(context, "Thanh toán thất bại", Toast.LENGTH_SHORT).show()
        }
        paymentResult.value = null // reset để không lặp lại
    }

    user?.let { currentUser ->
        OrderContent(
            selectedItems = selectedItems,
            selectedAddress = selectedAddress,
            onAddressSelect = { selectedAddress = it },
            addresses = emptyList(),
            selectedMethod = selectedMethod,
            onMethodChange = { selectedMethod = it },
            isPlacing = isPlacing,
            onPlaceOrder = {
                if (selectedMethod == null) {
                    Toast.makeText(context, "Vui lòng chọn phương thức thanh toán", Toast.LENGTH_SHORT).show()
                    return@OrderContent
                }
                if (selectedAddress == null) {
                    Toast.makeText(context, "Vui lòng chọn địa chỉ giao hàng", Toast.LENGTH_SHORT).show()
                    return@OrderContent
                }

                when (selectedMethod) {
                    PaymentMethod.ZALOPAY -> {
                        createZaloPayOrderSafe(
                            context = context,
                            user = currentUser,
                            selectedItems = selectedItems,
                            selectedAddress = selectedAddress,
                            onStart = { isPlacing = true },
                            onFinish = { isPlacing = false },
                            onSuccess = { /* không dùng ở đây, deep link xử lý */ },
                            onFailure = {
                                Toast.makeText(context, "Không tạo được thanh toán", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }
                    PaymentMethod.COD -> {
                        placeOrderNormally(
                            context = context,
                            selectedItems = selectedItems,
                            selectedAddress = selectedAddress,
                            currentUser = currentUser,
                            selectedMethod = PaymentMethod.COD.label,
                            setPlacing = { isPlacing = it },
                            onSuccess = { showSuccessDialog = true },
                            onError = { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
                        )
                    }
                    null -> {}
                }
            },
            onBack = { (context as? Activity)?.finish() }
        )

        // ✅ Dialog hiển thị khi thanh toán thành công (COD hoặc ZaloPay)
        if (showSuccessDialog) {
            SuccessDialog(
                message = "Thanh toán thành công!",
                onDismiss = {
                    showSuccessDialog = false
                    val intent = Intent(context, Home::class.java)
                    context.startActivity(intent)
                }
            )
        }
    }
}

@Composable
fun SuccessDialog(
    message: String,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Thông báo",
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp // dùng size cố định để tránh lỗi version typography
            )
        },
        text = {
            Text(text = message)
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng")
            }
        },
        shape = RoundedCornerShape(12.dp)
    )
}


// ----- ZaloPay -----
private fun createZaloPayOrderSafe(
    context: Context,
    user: User,
    selectedItems: List<CartItem>,
    selectedAddress: Address?,
    onStart: () -> Unit = {},
    onFinish: () -> Unit = {},
    onSuccess: () -> Unit = {},
    onFailure: () -> Unit = {}
) {
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

    val request = ZlpCreatePaymentRequest(
        userId = user._id ?: "",
        items = orderItems,
        customerName = selectedAddress?.name ?: "",
        customerPhone = selectedAddress?.phone ?: "",
        customerAddress = selectedAddress?.address ?: "",
        redirectUrl = "myapp://payment?status=success" // khi thanh toán ok
        // nếu fail, backend Zalo sẽ tự redirect về ...?status=fail
    )

    CoroutineScope(Dispatchers.IO).launch {
        withContext(Dispatchers.Main) { onStart() }
        try {
            val resp = RetrofitClient.zaloPayService.createZalopayPayment(request)
            withContext(Dispatchers.Main) {
                onFinish()
                if (resp.isSuccessful) {
                    val body = resp.body()
                    val paymentUrl = body?.paymentUrl ?: body?.rawZalo?.orderUrl
                    if (!paymentUrl.isNullOrEmpty()) {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl))
                        context.startActivity(intent)
                    } else onFailure()
                } else onFailure()
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { onFinish(); onFailure() }
        }
    }
}



// ----- COD -----
private fun placeOrderNormally(
    context: Context,
    selectedItems: List<CartItem>,
    selectedAddress: Address?,
    currentUser: User,
    selectedMethod: String,
    setPlacing: (Boolean) -> Unit,
    onSuccess: () -> Unit,
    onError: (String) -> Unit
) {
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

    val newOrder = Order(
        orderId = "",
        userId = currentUser._id ?: "",
        items = orderItems,
        total = orderItems.sumOf { it.subtotal },
        paymentMethod = selectedMethod,
        status = "Chờ xác nhận",
        date = "",
        customerName = selectedAddress?.name ?: "",
        customerPhone = selectedAddress?.phone ?: "",
        customerAddress = selectedAddress?.address ?: ""
    )

    setPlacing(true)
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitClient.apiService.createOrder(newOrder)
            withContext(Dispatchers.Main) {
                setPlacing(false)
                if (response.isSuccessful) onSuccess()
                else onError("Lỗi đặt hàng: ${response.code()}")
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) { setPlacing(false); onError("Lỗi kết nối: ${e.message}") }
        }
    }
}

@Composable
fun OrderContent(
    selectedItems: List<CartItem>,
    selectedAddress: Address?,
    onAddressSelect: (Address) -> Unit,
    addresses: List<Address>,
    selectedMethod: PaymentMethod?,
    onMethodChange: (PaymentMethod) -> Unit,
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
                }
            )
        },
        bottomBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(16.dp)
            ) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tổng tiền")
                    Text("${totalProductPrice} VND")
                }
                Divider(Modifier.padding(vertical = 8.dp))
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("Tổng", fontWeight = FontWeight.Bold)
                    Text("${grandTotal} VND", color = Color(0xFFd32f2f), fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = {
                        if (selectedItems.isEmpty()) {
                            Toast.makeText(context, "Vui lòng chọn sản phẩm", Toast.LENGTH_SHORT).show()
                        } else onPlaceOrder()
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isPlacing,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
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
                    }
                } else {
                    Text("Vui lòng chọn địa chỉ", color = Color.Gray)
                }

                Spacer(modifier = Modifier.height(16.dp))
                Text("Phương thức thanh toán", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)

                val paymentMethods = listOf(PaymentMethod.COD, PaymentMethod.ZALOPAY)

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    paymentMethods.forEach { method ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .weight(1f)
                                .border(1.dp, Color.Black, RoundedCornerShape(8.dp))
                                .clip(RoundedCornerShape(8.dp))
                                .clickable { onMethodChange(method) }
                                .padding(horizontal = 8.dp, vertical = 6.dp)
                        ) {
                            RadioButton(
                                selected = selectedMethod == method,
                                onClick = { onMethodChange(method) }
                            )
                            Text(method.label, modifier = Modifier.padding(start = 4.dp))
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

