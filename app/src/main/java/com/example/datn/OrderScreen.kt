package com.example.datn

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.material3.ButtonDefaults
import java.util.*


enum class PaymentMethod(val label: String) {
    COD("Thanh toán khi nhận hàng"),
    ZALOPAY("ZaloPay")
}

class OrderScreen : ComponentActivity() {

    private val paymentResult: MutableState<String?> = mutableStateOf(null)
    private val appTransId: MutableState<String?> = mutableStateOf(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleDeepLink(intent)

        // ✅ Nhận danh sách productIds thay vì full CartItem
        val selectedIds = intent.getStringArrayListExtra("selectedProductIds") ?: arrayListOf()

        // ✅ Lấy userId từ intent hoặc SharedPreferences
        val userId = intent.getStringExtra("userId")
            ?: getSharedPreferences("auth", MODE_PRIVATE).getString("userId", null)

        setContent {
            DATNTheme {
                if (userId != null) {
                    OrderScreenContent(
                        userId = userId,
                        selectedIds = selectedIds,
                        paymentResult = paymentResult,
                        appTransId = appTransId
                    )
                } else {
                    Text("Vui lòng đăng nhập")
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleDeepLink(intent)
    }

    private fun handleDeepLink(intent: Intent?) {
        intent?.data?.let { uri ->
            if (uri.scheme == "myapp" && uri.host == "payment") {
                paymentResult.value = uri.getQueryParameter("status")
                appTransId.value = uri.getQueryParameter("apptransid")
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 1001 && resultCode == RESULT_OK) {
            val status = data?.getStringExtra("status")
            val transId = data?.getStringExtra("appTransId")
            appTransId.value = transId
            paymentResult.value = if (status == "success") "success" else "failed"
        }
    }
}

@Composable
fun OrderScreenContent(
    userId: String,
    selectedIds: List<String>,
    paymentResult: MutableState<String?>,
    appTransId: MutableState<String?>
) {
    val context = LocalContext.current
    var user by remember { mutableStateOf<User?>(null) }
    var selectedAddress by remember { mutableStateOf<Address?>(null) }
    var selectedMethod by remember { mutableStateOf<PaymentMethod?>(null) }
    var isPlacing by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }
    var noteText by remember { mutableStateOf("") }
    var selectedItems by remember { mutableStateOf<List<CartItem>>(emptyList()) }

    // ✅ Load user + địa chỉ + chi tiết sản phẩm từ server
    LaunchedEffect(userId, selectedIds) {
        try {
            // Lấy thông tin user
            val usersResp = RetrofitClient.apiService.getUsers()
            if (usersResp.isSuccessful) {
                val users = usersResp.body() ?: emptyList()
                user = users.find { it._id == userId }
            }

            // Lấy địa chỉ
            val addrResp = RetrofitClient.addressService.getAddresses(userId)
            if (addrResp.isSuccessful) {
                val addresses = addrResp.body() ?: emptyList()
                selectedAddress = addresses.firstOrNull { it.isDefault } ?: addresses.firstOrNull()
            }

            // Lấy giỏ hàng để filter sản phẩm đã chọn
            val cartResp = RetrofitClient.cartService.getCartByUserId(userId)
            if (cartResp.isSuccessful) {
                val cartBody = cartResp.body()
                val allItems: List<CartItem> = cartBody?.items
                    ?.map { it.toCartItem() } ?: emptyList()
                selectedItems = allItems.filter { item -> selectedIds.contains(item.uniqueId()) }
            } else {
                Log.e("OrderScreen", "Cart API failed: ${cartResp.code()} - ${cartResp.message()}")
            }
        } catch (e: Exception) {
            Log.e("OrderScreen", "Error loading data", e)
        }
    }


    // ✅ Xử lý kết quả thanh toán
    LaunchedEffect(paymentResult.value, appTransId.value) {
        val result = paymentResult.value
        val transId = appTransId.value
        if (result == "success" && transId != null) {
            try {
                val queryRequest = ZlpQueryRequest(appTransId = transId)
                val queryResp = RetrofitClient.zaloPayService.queryStatus(queryRequest)
                if (queryResp.isSuccessful) {
                    val queryBody = queryResp.body()
                    if (queryBody?.returnCode == 1) {
                        showSuccessDialog = true
                        Toast.makeText(context, "Thanh toán thành công!", Toast.LENGTH_SHORT).show()
                    } else {
                        val errorMsg = queryBody?.returnMessage ?: "Không xác định"
                        Toast.makeText(context, "Thanh toán thất bại: $errorMsg", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Lỗi kiểm tra trạng thái: ${queryResp.code()}", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                Log.e("OrderScreen", "Query status error: ${e.message}")
            }
        } else if (result == "failed") {
            Toast.makeText(context, "Thanh toán thất bại", Toast.LENGTH_SHORT).show()
        }
        // Reset
        paymentResult.value = null
        appTransId.value = null
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
            onPlaceOrder = { note ->
                noteText = note
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
                            context,
                            currentUser,
                            selectedItems,
                            selectedAddress,
                            noteText,
                            appTransId,
                            { isPlacing = true },
                            { isPlacing = false },
                            { Toast.makeText(context, "Không tạo được thanh toán", Toast.LENGTH_SHORT).show() }
                        )
                    }
                    PaymentMethod.COD -> {
                        placeOrderNormally(
                            context,
                            selectedItems,
                            selectedAddress,
                            currentUser,
                            PaymentMethod.COD.label,
                            noteText,
                            { isPlacing = it },
                            { showSuccessDialog = true },
                            { msg -> Toast.makeText(context, msg, Toast.LENGTH_SHORT).show() }
                        )
                    }
                    null -> {}
                }
            },
            onBack = { (context as? Activity)?.finish() }
        )

        if (showSuccessDialog) {
            SuccessDialog(
                message = "Thanh toán thành công! Đơn hàng đã được xác nhận.",
                onDismiss = {
                    showSuccessDialog = false
                    val intent = Intent(context, Home::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                    context.startActivity(intent)
                    (context as? Activity)?.finish()
                }
            )
        }
    }
}

// OrderContent giữ nguyên (không thay đổi, noteText local ở đây không ảnh hưởng)
@Composable
fun OrderContent(
    selectedItems: List<CartItem>,
    selectedAddress: Address?,
    onAddressSelect: (Address) -> Unit,
    addresses: List<Address>,
    selectedMethod: PaymentMethod?,
    onMethodChange: (PaymentMethod) -> Unit,
    isPlacing: Boolean,
    onPlaceOrder: (String) -> Unit,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val totalProductPrice = selectedItems.sumOf { it.price * it.quantity }
    val grandTotal = totalProductPrice
    var noteText by remember { mutableStateOf("") } // Local note ở đây, nhưng onPlaceOrder nhận từ parent

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
                        } else onPlaceOrder(noteText)
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

                // Ghi chú cho đơn hàng
                Text(
                    "Để lại lời nhắn cho đơn hàng",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp
                )

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { newValue ->
                        if (newValue.length <= 100) {
                            noteText = newValue
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Lời nhắn cho shop (không bắt buộc)") },
                    maxLines = 3,
                    shape = RoundedCornerShape(8.dp)
                )

                // Hiển thị đếm ký tự
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${noteText.length}/100 ký tự",
                        fontSize = 12.sp,
                        color = Color.Gray,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))
            }

            items(selectedItems) { item ->
                OrderItemRow(item)
            }
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
    orderNote: String?,
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

    val newOrderRequest = CreateOrderRequest(
        userId = currentUser._id ?: "",
        items = orderItems,
        total = orderItems.sumOf { it.subtotal },
        paymentMethod = selectedMethod,
        status = "Chờ xác nhận",
        date = "",
        customerName = selectedAddress?.name ?: "",
        customerPhone = selectedAddress?.phone ?: "",
        customerAddress = selectedAddress?.address ?: "",
        orderNote = orderNote
    )

    setPlacing(true)
    CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitClient.apiService.createOrder(newOrderRequest)
            withContext(Dispatchers.Main) {
                setPlacing(false)
                if (response.isSuccessful) {
                    val createdOrder = response.body()
                    if (createdOrder != null) {
                        onSuccess()
                    } else {
                        onError("Không tạo được đơn hàng")
                    }
                } else {
                    onError("Lỗi đặt hàng: ${response.code()}")
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                setPlacing(false)
                onError("Lỗi kết nối: ${e.message}")
            }
        }
    }
}

private fun createZaloPayOrderSafe(
    context: Context,
    user: User,
    selectedItems: List<CartItem>,
    selectedAddress: Address?,
    orderNote: String?,
    appTransIdState: MutableState<String?>,
    onStart: () -> Unit,
    onFinish: () -> Unit,
    onFailure: () -> Unit
) {
    val redirectUrl = "myapp://payment?status=success" // ✅ Thêm redirectUrl cho đồng bộ
    val request = ZlpCreatePaymentRequest(
        userId = user._id ?: "",
        items = selectedItems,
        customerName = selectedAddress?.name ?: "",
        customerPhone = selectedAddress?.phone ?: "",
        customerAddress = selectedAddress?.address ?: "",
        redirectUrl = redirectUrl,
        description = "Thanh toán đơn hàng: ${selectedItems.sumOf { it.price * it.quantity }} VND - Note: $orderNote"
    )

    CoroutineScope(Dispatchers.IO).launch {
        withContext(Dispatchers.Main) { onStart() }
        try {
            val resp = RetrofitClient.zaloPayService.createZalopayPayment(request)
            withContext(Dispatchers.Main) {
                onFinish()
                if (resp.isSuccessful) {
                    val body = resp.body()
                    if (body != null) {
                        val paymentUrl = body.paymentUrl ?: body.rawZalo?.orderUrl
                        val transId = body.appTransId ?: body.rawZalo?.orderToken // Sử dụng orderToken nếu appTransId null
                        if (!transId.isNullOrEmpty()) {
                            appTransIdState.value = transId
                        }
                        if (!paymentUrl.isNullOrEmpty()) {
                            val intent = Intent(context, ZaloPayWebViewActivity::class.java).apply {
                                putExtra(ZaloPayWebViewActivity.EXTRA_PAYMENT_URL, paymentUrl)
                                putExtra(ZaloPayWebViewActivity.EXTRA_APP_TRANS_ID, transId)
                            }
                            (context as Activity).startActivityForResult(intent, 1001)
                        } else {
                            Log.e("ZaloPay", "Payment URL is null: ${body.message}")
                            onFailure()
                        }
                    } else {
                        Log.e("ZaloPay", "Response body is null")
                        onFailure()
                    }
                } else {
                    Log.e("ZaloPay", "API call failed: ${resp.code()} - ${resp.errorBody()?.string()}")
                    onFailure()
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Log.e("ZaloPay", "Exception: ${e.message}")
                onFinish()
                onFailure()
            }
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

