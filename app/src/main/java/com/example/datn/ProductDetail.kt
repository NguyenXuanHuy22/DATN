package com.example.datn

import android.app.Activity
import android.content.Context
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
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.outlined.Star
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import kotlin.math.roundToInt

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
fun ExpandableText(
    text: String,
    minimizedMaxLines: Int = 3
) {
    var isExpanded by remember { mutableStateOf(false) }

    Column {
        Text(
            text = text,
            fontSize = 14.sp,
            color = Color.Gray,
            maxLines = if (isExpanded) Int.MAX_VALUE else minimizedMaxLines,
            overflow = TextOverflow.Ellipsis
        )

        Spacer(Modifier.height(4.dp))

        if (text.length > 100) { // chỉ hiện nút nếu mô tả dài
            Text(
                text = if (isExpanded) "Ẩn bớt" else "Xem thêm",
                color = Color.Blue,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable { isExpanded = !isExpanded }
            )
        }
    }
}

@Composable
fun ProductDetailScreen(
    productId: String,
    viewModel: ProductViewModel = viewModel(),
    favoriteViewModel: FavoriteViewModel = viewModel(),
    reviewViewModel: ReviewViewModel = viewModel(),
    cartViewModel: CartViewModel = viewModel()
) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    val userId = sharedPref.getString("userId", null)

    LaunchedEffect(productId) {
        viewModel.getProductDetail(productId)
        reviewViewModel.getCommentsByProduct(productId)
        userId?.let { favoriteViewModel.loadWishlist(it) }
        userId?.let { cartViewModel.loadCart(it) }
    }

    val productDetail by viewModel.productDetail.observeAsState()
    val avgRating by reviewViewModel.avgRating.collectAsState()
    val comments by reviewViewModel.comments.collectAsState()
    val wishlistItems by favoriteViewModel.wishlistItems.observeAsState(emptyList())

    if (productDetail == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Đang tải hoặc không tìm thấy sản phẩm", color = Color.Gray)
        }
        return
    }

    val product = productDetail!!
    val variants = product.variants ?: emptyList()
    val availableSizes = variants.map { it.size }.distinct()
    var selectedSize by remember { mutableStateOf(availableSizes.firstOrNull() ?: "") }
    val availableColors = variants.filter { it.size == selectedSize }.map { it.color }.distinct()
    var selectedColor by remember { mutableStateOf(availableColors.firstOrNull() ?: "") }

    val isFavorite by remember(wishlistItems) {
        mutableStateOf(wishlistItems.any { it.productId == product._id })
    }

    val allImages = listOfNotNull(product.image) + (product.extraImages ?: emptyList())
    var selectedImage by remember { mutableStateOf(allImages.firstOrNull()) }

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
                IconButton(onClick = { (context as? Activity)?.finish() }) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text("Chi tiết sản phẩm", fontSize = 20.sp, fontWeight = FontWeight.Bold)
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 72.dp, start = 16.dp, end = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(Modifier.height(16.dp))

            // ================== GALLERY ==================
            if (allImages.isNotEmpty()) {
                selectedImage?.let { img ->
                    val imageModel = remember(img) {
                        img.takeIf { it.isNotEmpty() }?.let { url ->
                            if (url.startsWith("data:image")) {
                                val bytes = android.util.Base64.decode(
                                    url.substringAfter("base64,"),
                                    android.util.Base64.DEFAULT
                                )
                                bytes
                            } else url
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp)
                            .clip(RoundedCornerShape(12.dp))
                    ) {
                        AsyncImage(
                            model = imageModel,
                            contentDescription = product.name ?: "",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Fit,
                            placeholder = painterResource(R.drawable.logo),
                            error = painterResource(R.drawable.logo)
                        )

                        // ✅ Icon Yêu thích
                        val context = LocalContext.current
                        IconButton(
                            onClick = {
                                val userId = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                                    .getString("userId", null)

                                if (userId == null) {
                                    Toast.makeText(
                                        context,
                                        "Vui lòng đăng nhập để yêu thích sản phẩm",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    return@IconButton
                                }

                                favoriteViewModel.toggleFavorite(
                                    userId,
                                    WishlistItem(
                                        productId = product._id ?: "",
                                        name = product.name ?: "",
                                        image = product.image ?: "",
                                        price = product.salePrice?.toInt() ?: product.originalPrice?.toInt() ?: 0
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
                }

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.Center
                ) {
                    allImages.forEach { img ->
                        val imageModel = remember(img) {
                            img.takeIf { it.isNotEmpty() }?.let { url ->
                                if (url.startsWith("data:image")) {
                                    val bytes = android.util.Base64.decode(
                                        url.substringAfter("base64,"),
                                        android.util.Base64.DEFAULT
                                    )
                                    bytes
                                } else url
                            }
                        }

                        Box(
                            modifier = Modifier
                                .padding(4.dp)
                                .size(70.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .border(
                                    width = 2.dp,
                                    color = if (img == selectedImage) Color.Red else Color.Gray,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedImage = img }
                        ) {
                            AsyncImage(
                                model = imageModel,
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                placeholder = painterResource(R.drawable.logo),
                                error = painterResource(R.drawable.logo)
                            )
                        }
                    }
                }
            }
            // ================== END GALLERY ==================

            Spacer(Modifier.height(16.dp))

            // Tên & mô tả
            Text(product.name ?: "", fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Spacer(Modifier.height(8.dp))
            ExpandableText(product.description ?: "")
            Spacer(Modifier.height(16.dp))

            // Giá sản phẩm
            Row(verticalAlignment = Alignment.CenterVertically) {
                val originalPrice = product.originalPrice ?: 0
                val salePrice = product.salePrice ?: 0
                if (salePrice > 0 && salePrice < originalPrice) {
                    Text(
                        "${originalPrice.toDecimalString()} VND",
                        color = Color.Gray,
                        textDecoration = TextDecoration.LineThrough,
                        fontSize = 14.sp
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "${salePrice.toDecimalString()} VND",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Red
                    )
                } else {
                    Text(
                        "${originalPrice.toDecimalString()} VND",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // Đánh giá
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.clickable {
                    val intent = Intent(context, ReviewListScreen::class.java)
                    intent.putExtra("productId", productId)
                    context.startActivity(intent)
                }
            ) {
                repeat(5) { i ->
                    Icon(
                        imageVector = if (i < avgRating.roundToInt()) Icons.Filled.Star else Icons.Outlined.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFC107),
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(Modifier.width(8.dp))
                Text(
                    String.format("%.1f", avgRating) + " (${comments.size} đánh giá)",
                    fontSize = 14.sp,
                    color = Color.Gray
                )
            }

            Spacer(Modifier.height(16.dp))

            // Chọn size
            Text("Chọn size", fontWeight = FontWeight.Bold)
            Row {
                availableSizes.forEach { size ->
                    val qty = variants.filter { it.size == size }.sumOf { it.quantity }
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp, top = 8.dp)
                            .border(
                                1.dp,
                                if (selectedSize == size) Color.Black else Color.Gray,
                                RoundedCornerShape(4.dp)
                            )
                            .background(if (selectedSize == size) Color.LightGray else Color.White)
                            .clickable {
                                selectedSize = size
                                selectedColor = variants.filter { it.size == size }
                                    .map { it.color }.firstOrNull() ?: ""
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(size)
                            Text("$qty cái", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Chọn màu
            Text("Chọn màu", fontWeight = FontWeight.Bold)
            Row {
                availableColors.forEach { color ->
                    val qty =
                        variants.find { it.size == selectedSize && it.color == color }?.quantity
                            ?: 0
                    Box(
                        modifier = Modifier
                            .padding(end = 8.dp, top = 8.dp)
                            .border(
                                1.dp,
                                if (selectedColor == color) Color.Black else Color.Gray,
                                RoundedCornerShape(4.dp)
                            )
                            .background(if (selectedColor == color) Color.LightGray else Color.White)
                            .clickable { selectedColor = color }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(color)
                            Text("$qty cái", fontSize = 10.sp, color = Color.Gray)
                        }
                    }
                }
            }

            Spacer(Modifier.height(80.dp))
        }

        // Bottom bar
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
                val price =
                    if ((product.salePrice ?: 0) > 0) product.salePrice!! else product.originalPrice!!
                Text("${price.toDecimalString()} VND", fontWeight = FontWeight.Bold, fontSize = 18.sp)

                Button(
                    onClick = {
                        val variant =
                            variants.find { it.size == selectedSize && it.color == selectedColor }
                        if (variant == null || variant.quantity <= 0) {
                            Toast.makeText(context, "Sản phẩm đã hết", Toast.LENGTH_SHORT).show()
                            return@Button
                        }
                        if (!userId.isNullOrEmpty()) {
                            val itemId = "${product._id}_${selectedSize}_${selectedColor}"
                            val cartItem = CartItem(
                                itemId = itemId,
                                productId = product._id ?: "",
                                name = product.name ?: "",
                                image = product.image ?: "",
                                price = price,
                                quantity = 1,
                                size = selectedSize,
                                color = selectedColor,
                                maxQuantity = variant.quantity,
                                userId = userId
                            )
                            cartViewModel.addToCart(cartItem)
                            Toast.makeText(context, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show()
                        } else {
                            Toast.makeText(
                                context,
                                "Vui lòng đăng nhập để thêm sản phẩm",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(Icons.Default.ShoppingCart, contentDescription = null, tint = Color.White)
                    Spacer(Modifier.width(8.dp))
                    Text("Thêm vào giỏ hàng", color = Color.White)
                }
            }
        }
    }
}



