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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.IconButton
import androidx.compose.material.Icon
import androidx.compose.runtime.LaunchedEffect

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
fun ProductDetailScreen(productId: String, viewModel: ProductViewModel = viewModel(),favoriteViewModel: FavoriteViewModel = viewModel() ) {
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
    val variants = product.variants ?: emptyList()

    val availableSizes = variants.map { it.size }.distinct()
    var selectedSize by remember { mutableStateOf(availableSizes.firstOrNull() ?: "") }

    val availableColors = variants.filter { it.size == selectedSize }.map { it.color }.distinct()
    var selectedColor by remember { mutableStateOf(availableColors.firstOrNull() ?: "") }

    val context = LocalContext.current
    val cartViewModel: CartViewModel = viewModel()
    val sharedPref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)

    val userId = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
        .getString("userId", null)

    val wishlistItems by favoriteViewModel.wishlistItems.observeAsState(emptyList())

    val isFavorite = wishlistItems.any { it.productId == product._id }



    LaunchedEffect(userId, productDetail) {
        if (userId != null && productDetail != null) {
            favoriteViewModel.loadWishlist(userId)
        }
    }

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

                IconButton(
                    onClick = {
                        val userId = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                            .getString("userId", null)

                        if (userId == null) {
                            Toast.makeText(context, "Vui lòng đăng nhập để yêu thích sản phẩm", Toast.LENGTH_SHORT).show()
                            return@IconButton
                        }

                        favoriteViewModel.toggleFavorite(
                            userId,
                            WishlistItem(
                                productId = product._id ?: "",
                                name = product.name ?: "",
                                image = product.image ?: "",
                                price = product.price?.toInt() ?: 0
                            )
                        )
                    },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(12.dp)
                        .size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Outlined.FavoriteBorder,
                        contentDescription = if (isFavorite) "Bỏ yêu thích" else "Yêu thích",
                        tint = if (isFavorite) Color.Red else Color.Gray
                    )
                }


            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(product.name ?: "", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = product.description ?: "",
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(16.dp))

            Text("Chọn size", fontWeight = FontWeight.Bold)
            Row {
                availableSizes.forEach { size ->
                    val totalQuantityForSize = variants
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
                                selectedColor = variants
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
                    val quantityForVariant = variants
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
                    text = "${(product.price ?: 0).toDecimalString()} VND",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )

                LaunchedEffect(userId) {
                    userId?.let { cartViewModel.loadCart(it) }
                }

                val sharedPref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                val userId = sharedPref.getString("userId", null) ?: ""

                Button(
                    onClick = {
                        val variant = variants.find {
                            it.size == selectedSize && it.color == selectedColor
                        }

                        if (userId.isNotEmpty() && variant != null) {
                            val itemId = "${product._id}_${selectedSize}_${selectedColor}"

                            val cartItem = CartItem(
                                itemId = itemId,
                                productId = product._id ?: "",
                                name = product.name ?: "",
                                image = product.image ?: "",
                                price = product.price ?: 0,
                                quantity = 1,
                                size = selectedSize,
                                color = selectedColor,
                                maxQuantity = variant.quantity,
                                userId = userId
                            )

                            cartViewModel.addToCart(cartItem)
                            Toast.makeText(context, " Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(context, " Không thể thêm sản phẩm", Toast.LENGTH_SHORT).show()
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


