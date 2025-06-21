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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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
        bottomBar = {
            BottomNavigationBarr()
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
fun BottomNavigationBarrr() {
    val context = LocalContext.current

    BottomNavigation(
        backgroundColor = Color.White,
        contentColor = Color.Black
    ) {
        BottomNavigationItem(
            selected = true,
            onClick = {
                context.startActivity(Intent(context, Home::class.java))
            },
            icon = { androidx.compose.material.Icon(Icons.Default.Home, contentDescription = null) },
            label = { androidx.compose.material.Text("Home") }
        )
        BottomNavigationItem(
            selected = false,
            onClick = {},
            icon = { androidx.compose.material.Icon(Icons.Default.Search, contentDescription = null) },
            label = { androidx.compose.material.Text("Search") }
        )
        BottomNavigationItem(
            selected = false,
            onClick = {},
            icon = { androidx.compose.material.Icon(Icons.Default.Favorite, contentDescription = null) },
            label = { androidx.compose.material.Text("Saved") }
        )
        BottomNavigationItem(
            selected = false,
            onClick = {},
            icon = { androidx.compose.material.Icon(Icons.Default.ShoppingCart, contentDescription = null) },
            label = { androidx.compose.material.Text("Cart") }
        )
        BottomNavigationItem(
            selected = false,
            onClick = {

            },
            icon = { androidx.compose.material.Icon(Icons.Default.AccountCircle, contentDescription = null) },
            label = { androidx.compose.material.Text("Account") }
        )
    }
}


