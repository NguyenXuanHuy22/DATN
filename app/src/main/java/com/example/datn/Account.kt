package com.example.datn

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

class Account : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                AccountScreen()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen() {
    val context = LocalContext.current
    val sharedPreferences = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    val userRole = remember { mutableStateOf("") }

    //  Load role từ SharedPreferences khi màn hình khởi tạo
    LaunchedEffect(Unit) {
        userRole.value = sharedPreferences.getString("userRole", "") ?: ""
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Tài khoản") },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color.White,
                    titleContentColor = Color.Black
                )
            )
        },
        bottomBar = {
            BottomNavigationBarr()
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            //  - chức năng cá nhân
            if(userRole.value != "admin"){
                AccountMenuItem(icon = Icons.Default.Inventory, label = "Đơn hàng của tôi") {
                    Toast.makeText(context, "Đơn hàng của tôi", Toast.LENGTH_SHORT).show()
                }
                Divider()
                AccountMenuItem(icon = Icons.Default.Person, label = "Thông tin cá nhân") {
                    Toast.makeText(context, "Thông tin cá nhân", Toast.LENGTH_SHORT).show()
                }
                Divider()
                AccountMenuItem(icon = Icons.Default.Home, label = "Địa chỉ") {
                    Toast.makeText(context, "Địa chỉ", Toast.LENGTH_SHORT).show()
                }
                Divider()
                AccountMenuItem(icon = Icons.Outlined.FavoriteBorder, label = "Sản phẩm yêu thích") {
                    Toast.makeText(context, "Sản phẩm yêu thích", Toast.LENGTH_SHORT).show()
                }
                Divider()
            }


            //  Mục admin - chỉ hiện nếu là admin
            if (userRole.value == "admin") {
                AccountMenuItem(icon = Icons.Default.ProductionQuantityLimits, label = "Quản lý sản phẩm") {
                    Toast.makeText(context, "Quản lý sản phẩm", Toast.LENGTH_SHORT).show()
                }
                Divider()
                AccountMenuItem(icon = Icons.Default.BreakfastDining, label = "Quản lý đơn hàng") {
                    Toast.makeText(context, "Quản lý đơn hàng", Toast.LENGTH_SHORT).show()
                }
                Divider()
                AccountMenuItem(icon = Icons.Default.RealEstateAgent, label = "Doanh thu") {
                    Toast.makeText(context, "Doanh thu", Toast.LENGTH_SHORT).show()
                }
                Divider()
                AccountMenuItem(icon = Icons.Default.Category, label = "Quản lý danh mục sản phẩm") {
                    Toast.makeText(context, "Quản lý danh mục sản phẩm", Toast.LENGTH_SHORT).show()
                }
                Divider()
            }

            Spacer(Modifier.height(16.dp))
            Divider(thickness = 8.dp, color = Color.LightGray)

            //  Hỗ trợ
            AccountMenuItem(icon = Icons.Outlined.Info, label = "Câu hỏi trợ giúp") {
                Toast.makeText(context, "Câu hỏi trợ giúp", Toast.LENGTH_SHORT).show()
            }
            Divider()
            AccountMenuItem(icon = Icons.Outlined.HeadsetMic, label = "Trung tâm hỗ trợ") {
                Toast.makeText(context, "Trung tâm hỗ trợ", Toast.LENGTH_SHORT).show()
            }

            Spacer(Modifier.height(16.dp))
            Divider(thickness = 8.dp, color = Color.LightGray)

            //  Đăng xuất
            AccountMenuItem(
                icon = Icons.Default.Logout,
                label = "Đăng xuất",
                color = Color.Red
            ) {
                sharedPreferences.edit().clear().apply()
                Toast.makeText(context, "Đăng xuất", Toast.LENGTH_SHORT).show()

                val intent = Intent(context, LoginScreen::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                context.startActivity(intent)
            }
        }
    }
}

@Composable
fun AccountMenuItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    color: Color = Color.Black,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontWeight = FontWeight.Normal,
            color = color,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
fun BottomNavigationBarr() {
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
            onClick = {
                context.startActivity(Intent(context, SearchActivity::class.java))
            },
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
