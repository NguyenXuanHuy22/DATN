package com.example.datn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import coil.request.ImageRequest
import com.example.datn.ui.theme.DATNTheme
import android.app.Activity
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.platform.LocalContext

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
    // Khi productId thay đổi, gọi API lấy chi tiết sản phẩm
    LaunchedEffect(productId) {
        viewModel.getProductDetail(productId)
    }

    // Quan sát LiveData productDetail từ ViewModel
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

    val sizes = product.sizes.ifEmpty { listOf("S", "M", "L") }
    val colors = product.colors.ifEmpty { listOf("Đen", "Trắng", "Xanh") }

    var selectedSize by remember { mutableStateOf(sizes.first()) }
    var selectedColor by remember { mutableStateOf(colors.first()) }

    Box(modifier = Modifier.fillMaxSize()) {
        // Fixed top bar with Back button and title
        // Quan trọng: Thanh trên cố định để luôn hiển thị nút Back và tiêu đề
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
                .align(Alignment.TopCenter)
        ) {
            val context = LocalContext.current
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
                .padding(top = 72.dp, start = 16.dp, end = 16.dp) // Điều chỉnh padding top để tránh che nội dung
                .verticalScroll(rememberScrollState())
        ) {
            // Quan trọng: Nội dung chính có thể cuộn để xử lý mô tả dài
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
                IconButton(
                    onClick = { /* TODO: Favorite */ },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.White, shape = CircleShape)
                        .border(1.dp, Color.Gray, CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(product.name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFFC107))
                Spacer(modifier = Modifier.width(4.dp))
                Text("5.0", fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(4.dp))
                Text("(1 đánh giá)", color = Color.Gray, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Quan trọng: Mô tả sản phẩm có thể dài, hiển thị toàn bộ nội dung
            Text(
                text = product.description ?: "Không có mô tả",
                fontSize = 14.sp,
                color = Color.Gray,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text("Chọn size", fontWeight = FontWeight.Bold)
            Row {
                sizes.forEach { size ->
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp, top = 8.dp)
                            .border(
                                width = 1.dp,
                                color = if (selectedSize == size) Color.Gray else Color.Gray,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .background(
                                color = if (selectedSize == size) Color.LightGray else Color.White
                            )
                            .clickable { selectedSize = size }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(text = size)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text("Chọn màu", fontWeight = FontWeight.Bold)
            Row {
                colors.forEach { color ->
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp, top = 8.dp)
                            .border(
                                width = 1.dp,
                                color = if (selectedColor == color) Color.Gray else Color.Gray,
                                shape = RoundedCornerShape(4.dp)
                            )
                            .background(
                                color = if (selectedColor == color) Color.LightGray else Color.White
                            )
                            .clickable { selectedColor = color }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(text = color)
                    }
                }
            }

            // Thêm khoảng trống ở dưới để nút "Thêm vào giỏ hàng" không che nội dung
            Spacer(modifier = Modifier.height(80.dp))
        }

        // Fixed price and Add to Cart button at the bottom
        // Quan trọng: Thanh dưới cố định để hiển thị giá và nút Thêm vào giỏ hàng
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
                    text = "${product.price} đ",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
                Button(
                    onClick = {
                        // TODO: Thêm vào giỏ hàng
                    },
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