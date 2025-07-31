package com.example.datn

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.datn.ui.theme.DATNTheme
import com.example.datn.utils.toDecimalString
import kotlinx.coroutines.launch

class ProductDetail : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Nhận productId được truyền từ Intent
        val productId = intent.getStringExtra("productId") ?: ""

        setContent {
            DATNTheme {
                ProductDetailScreen(productId = productId)
            }
        }
    }
}



@Composable
fun ProductDetailScreen(productId: String, viewModel: ProductViewModel = viewModel()) {
    LaunchedEffect(productId) {
        viewModel.getProductDetail(productId)
    }

    val productDetail by viewModel.productDetail.observeAsState()

    if (productDetail == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(text = "Đang tải hoặc không tìm thấy sản phẩm", color = Color.Gray)
        }
        return
    }

    val product = productDetail!!

    // Lấy size từ variants
    val availableSizes = product.variants.map { it.size }.distinct()
    var selectedSize by remember { mutableStateOf(availableSizes.firstOrNull() ?: "") }

    // Lấy danh sách color tương ứng với size đang chọn
    val availableColors = product.variants.filter { it.size == selectedSize }.map { it.color }.distinct()
    var selectedColor by remember { mutableStateOf(availableColors.firstOrNull() ?: "") }

    val context = LocalContext.current
    val cartViewModel: CartViewModel = viewModel()
    val sharedPref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    val userId = sharedPref.getString("userId", null)

    Box(modifier = Modifier.fillMaxSize()) {
        // App bar
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
                .align(Alignment.TopCenter)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = {
                    (context as? Activity)?.finish()
                }) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Quay lại")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = "Chi tiết sản phẩm", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 72.dp, start = 16.dp, end = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            ) {
                AsyncImage(
                    model = product.image,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(product.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = product.description, fontSize = 14.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))

            Text("Chọn size", fontWeight = FontWeight.Bold)
            Row {
                availableSizes.forEach { size ->
                    // Tổng số lượng của tất cả màu cho size này
                    val totalQuantityForSize = product.variants
                        .filter { it.size == size }
                        .sumOf { it.quantity }

                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp, top = 8.dp)
                            .border(
                                width = 1.dp,
                                color = if (selectedSize == size) Color.Black else Color.Gray,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .background(
                                color = if (selectedSize == size) Color.LightGray else Color.White
                            )
                            .clickable {
                                selectedSize = size
                                selectedColor = product.variants
                                    .filter { it.size == size }
                                    .map { it.color }
                                    .firstOrNull() ?: ""
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = size)
                            Text(
                                text = "$totalQuantityForSize cái",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }


            Spacer(modifier = Modifier.height(16.dp))

            Text("Chọn màu", fontWeight = FontWeight.Bold)
            Row {
                availableColors.forEach { color ->
                    val quantityForVariant = product.variants
                        .find { it.size == selectedSize && it.color == color }
                        ?.quantity ?: 0

                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp, top = 8.dp)
                            .border(
                                width = 1.dp,
                                color = if (selectedColor == color) Color.Black else Color.Gray,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .background(
                                color = if (selectedColor == color) Color.LightGray else Color.White
                            )
                            .clickable {
                                selectedColor = color
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = color)
                            Text(
                                text = "$quantityForVariant cái",
                                fontSize = 10.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(80.dp))
        }

        // Footer
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${product.price.toDecimalString()} VND",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                LaunchedEffect(userId) {
                    userId?.let { cartViewModel.loadCart(it) }
                }

                Button(
                    onClick = {
                        val variant = product.variants.find {
                            it.size == selectedSize && it.color == selectedColor
                        }

                        if (userId != null && variant != null) {
                            val itemId = "${product.id}_${selectedSize}_${selectedColor}"
                            val cartItem = CartItem(
                                itemId = itemId,
                                productId = product.id,
                                name = product.name,
                                image = product.image,
                                price = product.price,
                                quantity = 1,
                                size = selectedSize,
                                color = selectedColor,
                                maxQuantity = variant.quantity
                            )
                            cartViewModel.addToCart(cartItem)
                            Toast.makeText(context, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, "Không thể thêm sản phẩm", Toast.LENGTH_SHORT).show()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Thêm vào giỏ hàng", color = Color.White)
                }
            }
        }
    }
}

