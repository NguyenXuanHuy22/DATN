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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.datn.utils.toDecimalString
import kotlinx.coroutines.launch
import com.example.datn.ProductRepository
import com.google.gson.Gson


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
    val grandTotal by viewModel.grandTotal.collectAsState()

    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(userId) {
        viewModel.loadCart(userId)
    }

    Scaffold(
        bottomBar = {
            Column {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        text = "Tổng cộng: ${grandTotal.toDecimalString()} VND",
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (selectedItems.isEmpty()) {
                                Toast.makeText(context, "Vui lòng chọn sản phẩm để đặt hàng", Toast.LENGTH_SHORT).show()
                            } else {
                                val selectedProducts = cartItems.filter { selectedItems.contains(it.uniqueId()) }
                                if (selectedProducts.isEmpty()) {
                                    Toast.makeText(context, "Không có sản phẩm hợp lệ", Toast.LENGTH_SHORT).show()
                                    return@Button
                                }

                                // ✅ Truyền danh sách ID thay vì full CartItem
                                val selectedIds = ArrayList(selectedProducts.map { it.uniqueId() })

                                val intent = Intent(context, OrderScreen::class.java).apply {
                                    putExtra("userId", userId)
                                    putStringArrayListExtra("selectedProductIds", selectedIds)
                                }
                                context.startActivity(intent)
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
                        val key = item.itemId ?: item.uniqueId()

                        CartItemRow(
                            item = item,
                            isSelected = selectedItems.contains(item.uniqueId()),
                            onToggleSelect = { viewModel.toggleItemSelection(item.uniqueId()) },
                            onDelete = { viewModel.deleteItem(key) },
                            onQuantityChange = { newQty ->
                                scope.launch { viewModel.updateItemQuantity(key, newQty) }
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
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val productRepository = ProductRepository()

    val imageRequest = remember(item.image) {
        item.image?.let { base64String ->
            if (base64String.startsWith("data:image")) {
                val pureBase64 = base64String.substringAfter("base64,")
                val decodedBytes = android.util.Base64.decode(pureBase64, android.util.Base64.DEFAULT)
                coil.request.ImageRequest.Builder(context)
                    .data(decodedBytes)
                    .crossfade(true)
                    .build()
            } else {
                coil.request.ImageRequest.Builder(context)
                    .data(item.image ?: "")
                    .crossfade(true)
                    .build()
            }
        }
    }

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = isSelected,
                onCheckedChange = { onToggleSelect() }
            )

            if (imageRequest != null) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = item.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .size(90.dp)
                        .padding(end = 12.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    placeholder = painterResource(id = R.drawable.logo),
                    error = painterResource(id = R.drawable.logo)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Placeholder",
                    modifier = Modifier
                        .size(90.dp)
                        .padding(end = 12.dp)
                        .clip(RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name.orEmpty(),
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "Size: ${item.size} | Màu: ${item.color}",
                    style = MaterialTheme.typography.bodySmall
                )

                Text(
                    text = "Giá: ${item.price.toDecimalString()} VND",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color(0xFFe53935),
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = {
                        if (item.quantity > 1) {
                            onQuantityChange(item.quantity - 1)
                        } else {
                            onDelete()
                        }
                    }) {
                        Icon(Icons.Default.Remove, contentDescription = "Giảm số lượng")
                    }

                    Text(
                        "${item.quantity}",
                        modifier = Modifier.padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.bodyMedium
                    )

                    IconButton(onClick = {
                        scope.launch {
                            try {
                                val maxQty = productRepository.getMaxQuantity(
                                    item.productId.orEmpty(),
                                    item.size.orEmpty(),
                                    item.color.orEmpty()
                                )
                                if (item.quantity < maxQty) {
                                    onQuantityChange(item.quantity + 1)
                                } else {
                                    Toast.makeText(
                                        context,
                                        "Đã đạt số lượng tối đa ($maxQty)",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Lỗi kiểm tra số lượng", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "Tăng số lượng")
                    }
                }
            }

            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "Xóa", tint = Color.Red)
            }
        }

        Divider(color = Color.LightGray, thickness = 1.dp)
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
                    if (currentScreen != label) {
                        context.startActivity(Intent(context, activityClass))
                    }
                },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(20.dp),
                        tint = if (currentScreen == label) Color.Black else Color.Gray
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        color = if (currentScreen == label) Color.Black else Color.Gray
                    )
                }
            )
        }
    }
}



