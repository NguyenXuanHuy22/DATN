package com.example.datn

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.datn.ui.theme.DATNTheme

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
fun FavoriteScreen() {
    Scaffold(

        topBar = {
            TopAppBar(
                title = { Text("Sản phẩm yêu thích") }, //san pham yeu thich
                backgroundColor = Color.White,
                contentColor = Color.Black
            )

        },
        bottomBar = { //test
            BottomNavigationBarrr(currentScreen = "Saved")
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(8.dp)
        ) {
            // Chưa có dữ liệu nên hiển thị thông báo
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Chưa có sản phẩm yêu thích nào", color = Color.Gray)
            }

            // Nếu sau này có sản phẩm, bạn chỉ cần thay phần trên bằng LazyVerticalGrid như sau:
            /*
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxHeight()
            ) {
                items(favoriteList) { product ->
                    FavoriteItem(product = product, onDelete = { /* xoá sản phẩm */ })
                }
            }
            */
        }
    }
}

@Composable
fun FavoriteItem(product: Product, onDelete: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.LightGray)
            .padding(8.dp)
    ) {
        // Hiện chưa cần hiển thị ảnh thật
        Box(
            modifier = Modifier
                .height(150.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp))
                .background(Color.Gray)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = product.name,
            fontWeight = FontWeight.Bold
        )
        Text(text = "${product.price} vnd", color = Color.DarkGray)

        IconButton(
            onClick = onDelete,
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Default.Delete, contentDescription = "Xoá khỏi yêu thích")
        }
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



