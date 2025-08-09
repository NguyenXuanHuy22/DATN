package com.example.datn

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.datn.ui.theme.DATNTheme
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.text.style.TextOverflow


class Favorite : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DATNTheme {
                FavoriteScreen()
            }
        }
    }
}

@Composable
fun FavoriteScreen(favoriteViewModel: FavoriteViewModel = viewModel()) {
    val context = LocalContext.current
    val sharedPref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    val userId = sharedPref.getString("userId", null)

    val wishlistItems by favoriteViewModel.wishlistItems.observeAsState(emptyList())
    val error by favoriteViewModel.error.observeAsState()

    LaunchedEffect(userId) {
        userId?.let { favoriteViewModel.loadWishlist(it) }
    }

    LaunchedEffect(error) {
        error?.let {
            Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sản phẩm yêu thích") },
                backgroundColor = Color.White,
                contentColor = Color.Black
            )
        },
        bottomBar = {
            BottomNavigationBarrr(currentScreen = "Saved")
        }
    ) { innerPadding ->

        if (userId == null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFEEEEEE))
                    .padding(innerPadding)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Vui lòng đăng nhập để xem danh sách yêu thích", color = Color.Gray)
            }
            return@Scaffold
        }

        if (wishlistItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFEEEEEE))
                    .padding(innerPadding)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Chưa có sản phẩm yêu thích nào", color = Color.Gray)
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color(0xFFEEEEEE))
                    .padding(innerPadding)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                items(items = wishlistItems) { item ->
                    FavoriteItem(
                        product = Product(
                            _id = item.productId,
                            category = "",
                            name = item.name,
                            price = item.price,
                            image = item.image,
                            description = "",
                            variants = emptyList()
                        ),
                        onDelete = {
                            userId?.let { favoriteViewModel.deleteWishlistItem(it, item.productId) }
                        },
                        onClick = {
                            val intent = Intent(context, ProductDetail::class.java)
                            intent.putExtra("productId", item.productId)
                            context.startActivity(intent)
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun FavoriteItem(
    product: Product,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF5F5F5))
            .clickable { onClick() }
            .padding(bottom = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(Color.LightGray)
        ) {
            AsyncImage(
                model = product.image,
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
            )

            IconButton(
                onClick = { onDelete() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(8.dp)
                    .size(28.dp)
                    .background(Color.White.copy(alpha = 0.7f), shape = CircleShape)
                    .clip(CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Filled.Favorite,
                    contentDescription = "Xoá khỏi yêu thích",
                    tint = Color.Red
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = product.name ?: "",
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 8.dp),
            color = Color.Black
        )

        Text(
            text = "${product.price.toInt()} VND",
            color = Color(0xFFD32F2F), // đỏ đậm giống hình mẫu
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}


@Composable
fun BottomNavigationBarrr(currentScreen: String) {
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



