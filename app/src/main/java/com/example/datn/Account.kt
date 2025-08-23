package com.example.datn

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch

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
    val scope = rememberCoroutineScope()

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    val userRole = remember { mutableStateOf("") }
    val userId = sharedPreferences.getString("userId", "") ?: ""

    // Hàm load dữ liệu user
    suspend fun loadUser() {
        try {
            val response = RetrofitClient.apiService.getUsers()
            if (response.isSuccessful) {
                response.body()?.firstOrNull { it._id == userId }?.let { user ->
                    name = user.name ?: ""
                    email = user.email ?: ""
                    phone = user.phone ?: ""
                    address = user.address ?: ""
                    password = user.password ?: ""
                    imageUrl = user.avatar ?: ""
                    userRole.value = user.role ?: "user"
                }
            }
        } catch (e: Exception) {
            Toast.makeText(context, "Lỗi tải thông tin: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    // Launcher để mở EditProfileScreen và nhận kết quả
    val editProfileLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            // Load lại dữ liệu khi quay về
            scope.launch { loadUser() }
        }
    }

    // Load dữ liệu lần đầu
    LaunchedEffect(Unit) {
        scope.launch { loadUser() }
    }

    // Chuyển Base64 sang Bitmap
    val avatarBitmap = remember(imageUrl) {
        imageUrl.takeIf { it.isNotBlank() }?.let { base64 ->
            try {
                val cleanBase64 = base64.substringAfter("base64,", base64)
                val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            } catch (e: Exception) {
                null
            }
        }
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
            BottomNavigationBarr(currentScreen = "Account")
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (avatarBitmap != null) {
                    Image(
                        bitmap = avatarBitmap.asImageBitmap(),
                        contentDescription = "Avatar",
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.Gray)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))
                Column {
                    Text(text = name, fontWeight = FontWeight.Bold, fontSize = 20.sp)
                    Text(text = email, fontSize = 14.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Divider()

            AccountMenuItem(icon = Icons.Default.Edit, label = "Chỉnh sửa thông tin") {
                val intent = Intent(context, EditProfile::class.java)
                editProfileLauncher.launch(intent)
            }
            Divider()

                AccountMenuItem(icon = Icons.Default.BreakfastDining, label = "Lịch sử mua hàng") {
                    val intent = Intent(context, OrderHistoryScreen::class.java)
                    context.startActivity(intent)
                }
                Divider()
                AccountMenuItem(icon = Icons.Default.ShoppingCart, label = "Giỏ hàng") {
                    val intent = Intent(context, CartScreen::class.java)
                    context.startActivity(intent)
                }
                Divider()

            AccountMenuItem(icon = Icons.Default.LockReset, label = "Đổi mật khẩu") {
                val intent = Intent(context, ChangePassword::class.java)
                context.startActivity(intent)
            }
            Divider()

            Spacer(modifier = Modifier.height(16.dp))
            Divider(thickness = 8.dp, color = Color.LightGray)

            AccountMenuItem(icon = Icons.Default.LocationOn, label = "Địa chỉ cửa hàng") {
                val latitude = 21.03832
                val longitude = 105.74726
                val label = "Cửa hàng thể thao Poly Sport FPT"

                val gmmIntentUri = android.net.Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($label)")
                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps") // ưu tiên Google Maps

                try {
                    context.startActivity(mapIntent)
                } catch (e: Exception) {
                    Toast.makeText(context, "Không tìm thấy Google Maps", Toast.LENGTH_SHORT).show()
                }
            }

            Divider()



            AccountMenuItem(icon = Icons.Default.Logout, label = "Đăng xuất", color = Color.Red) {
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
        Icon(imageVector = icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = label, fontWeight = FontWeight.Normal, color = color, style = MaterialTheme.typography.bodyLarge)
    }
}

@Composable
fun BottomNavigationBarr(currentScreen: String) {
    val context = LocalContext.current
    BottomNavigation(
        backgroundColor = Color.White,
        contentColor = Color.Black,
        elevation = 2.dp,
        modifier = Modifier.fillMaxWidth().height(48.dp)
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
