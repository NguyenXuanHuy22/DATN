package com.example.datn

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.datn.ui.theme.DATNTheme
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

class CartScreen : ComponentActivity() {
    private val viewModel: CartViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val sharedPref = getSharedPreferences("auth", Context.MODE_PRIVATE)
        val userId = sharedPref.getString("userId", null)
        Log.d("CartScreen", "USER_ID_DEBUG: $userId")

        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "Không tìm thấy người dùng. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show()
            finish()
        } else {
            setContent {
                DATNTheme {
                    CartScreenContent(viewModel = viewModel, userId = userId)
                }
            }
        }
    }
}


@Composable
fun CartScreenContent(viewModel: CartViewModel, userId: String) {
    val cartItems by viewModel.cartItems.collectAsState()
    val selectedItems by viewModel.selectedItems.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(userId) {
        viewModel.loadCart(userId)
    }

    val totalPrice by viewModel.totalPrice.collectAsState()
    val shippingFee by viewModel.shippingFee.collectAsState()
    val grandTotal by viewModel.grandTotal.collectAsState()

    Scaffold(
        bottomBar = {
            Column {
                Column(Modifier.padding(16.dp)) {
                    Text("Tạm tính: $totalPrice VND")
                    Text("Phí vận chuyển: $shippingFee VND")
                    Text("Tổng cộng: $grandTotal VND", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            if (selectedItems.isEmpty()) {
                                Toast.makeText(context, "Vui lòng chọn sản phẩm để đặt hàng", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Chức năng đặt hàng đang được phát triển", Toast.LENGTH_SHORT).show()
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = selectedItems.isNotEmpty()
                    ) {
                        Text("Đặt hàng")
                    }
                }
                BottomNavigationBarCart(currentScreen = "Cart")
            }
        }
    ) { padding ->
        when {
            errorMessage != null -> {
                Text(
                    text = errorMessage ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    textAlign = TextAlign.Center
                )
            }

            cartItems.isEmpty() -> {
                Text(
                    text = "Giỏ hàng trống",
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    textAlign = TextAlign.Center
                )
            }

            else -> {
                LazyColumn(modifier = Modifier.padding(padding)) {
                    items(cartItems) { item ->
                        CartItemRow(
                            item = item,
                            isSelected = selectedItems.contains(item.itemId),
                            onToggleSelect = { viewModel.toggleItemSelection(item.itemId) },
                            onDelete = { viewModel.deleteItem(item.itemId) },
                            onQuantityChange = { newQty ->
                                scope.launch {
                                    viewModel.updateItemQuantity(userId, item.itemId, newQty)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}


@Composable
fun CartItemRow(
    item: CartItem,
    isSelected: Boolean,
    onToggleSelect: () -> Unit,
    onDelete: () -> Unit,
    onQuantityChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { onToggleSelect() },
            modifier = Modifier.align(Alignment.CenterVertically)
        )
        Image(
            painter = rememberAsyncImagePainter(item.image),
            contentDescription = item.name,
            modifier = Modifier
                .size(90.dp)
                .padding(end = 12.dp),
            contentScale = ContentScale.Crop
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(item.name, fontWeight = FontWeight.Bold)
            Text("Size: ${item.size} | Màu: ${item.color}")
            Text("Giá: ${item.price} VND")
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    if (item.quantity > 1) onQuantityChange(item.quantity - 1)
                }) {
                    Icon(Icons.Default.Remove, contentDescription = "Giảm số lượng")
                }
                Text("${item.quantity}", modifier = Modifier.padding(horizontal = 4.dp))
                IconButton(onClick = {
                    onQuantityChange(item.quantity + 1)
                }) {
                    Icon(Icons.Default.Add, contentDescription = "Tăng số lượng")
                }
            }
        }
        IconButton(onClick = onDelete) {
            Icon(Icons.Outlined.Delete, contentDescription = "Xóa")
        }
    }
}


@Composable
fun BottomNavigationBarCart(currentScreen: String = "Cart") {
    val context = LocalContext.current

    BottomNavigation(
        backgroundColor = Color.White,
        contentColor = Color.Black,
        elevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        val items = listOf(
            Triple("Home", Icons.Default.Home, Home::class.java),
            Triple("Search", Icons.Default.Search, SearchActivity::class.java),
            Triple("Saved", Icons.Default.Favorite, Favorite::class.java),
            Triple("Cart", Icons.Default.ShoppingCart, CartScreen::class.java),
            Triple("Account", Icons.Default.AccountCircle, Account::class.java)
        )

        items.forEach { (label, icon, activityClass) ->
            BottomNavigationItem(
                selected = currentScreen == label,
                onClick = {
                    // 👉 Khi click chuyển sang màn mới nếu chưa phải màn hiện tại
                    if (currentScreen != label) {
                        context.startActivity(Intent(context, activityClass))
                    }
                },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(20.dp),
                        // 👉 Sửa tại đây: Nếu được chọn thì icon màu đen, không thì màu xám
                        tint = if (currentScreen == label) Color.Black else Color.Gray
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        // 👉 Sửa tại đây: Nếu được chọn thì chữ màu đen, không thì màu xám
                        color = if (currentScreen == label) Color.Black else Color.Gray
                    )
                }
            )
        }
    }
}


