package com.example.datn

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.datn.ui.theme.DATNTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.security.SecureRandom
import java.util.Base64

class RegisterScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DATNTheme {
                RegisterScreenContent()
            }
        }
    }
}

fun generateShortId(): String {
    val random = SecureRandom()
    val bytes = ByteArray(6)
    random.nextBytes(bytes)
    return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes).substring(0, 8)
}

@Composable
fun RegisterScreenContent() {
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var nameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var phoneError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var addressError by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text("Tạo tài khoản", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Hãy tạo một tài khoản cho bạn", fontSize = 16.sp, color = Color.Gray)

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = ""
                },
                label = { Text("Tên") },
                isError = nameError.isNotEmpty(),
                supportingText = { if (nameError.isNotEmpty()) Text(nameError, color = MaterialTheme.colorScheme.error) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = ""
                },
                label = { Text("Email") },
                isError = emailError.isNotEmpty(),
                supportingText = { if (emailError.isNotEmpty()) Text(emailError, color = MaterialTheme.colorScheme.error) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    phoneError = ""
                },
                label = { Text("Số điện thoại") },
                isError = phoneError.isNotEmpty(),
                supportingText = { if (phoneError.isNotEmpty()) Text(phoneError, color = MaterialTheme.colorScheme.error) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = password,
                onValueChange = {
                    password = it
                    passwordError = ""
                },
                label = { Text("Mật khẩu") },
                visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                isError = passwordError.isNotEmpty(),
                supportingText = { if (passwordError.isNotEmpty()) Text(passwordError, color = MaterialTheme.colorScheme.error) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = address,
                onValueChange = {
                    address = it
                    addressError = ""
                },
                label = { Text("Địa chỉ") },
                isError = addressError.isNotEmpty(),
                supportingText = { if (addressError.isNotEmpty()) Text(addressError, color = MaterialTheme.colorScheme.error) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    nameError = ""
                    emailError = ""
                    phoneError = ""
                    passwordError = ""
                    addressError = ""

                    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
                    var hasError = false

                    if (name.isBlank()) {
                        nameError = "Vui lòng nhập tên"
                        hasError = true
                    }
                    if (email.isBlank()) {
                        emailError = "Vui lòng nhập email"
                        hasError = true
                    } else if (!email.matches(emailRegex)) {
                        emailError = "Email không hợp lệ"
                        hasError = true
                    }
                    if (phone.isBlank()) {
                        phoneError = "Vui lòng nhập số điện thoại"
                        hasError = true
                    }
                    if (password.length < 6) {
                        passwordError = "Mật khẩu phải có ít nhất 6 ký tự"
                        hasError = true
                    }
                    if (address.isBlank()) {
                        addressError = "Vui lòng nhập địa chỉ"
                        hasError = true
                    }

                    if (hasError) return@Button

                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val response = RetrofitClient.apiService.getUsers()
                            if (response.isSuccessful) {
                                val users = response.body() ?: emptyList()
                                val emailExists = users.any { user -> user.email == email }
                                val phoneExists = users.any { user -> user.phone == phone }

                                if (emailExists || phoneExists) {
                                    withContext(Dispatchers.Main) {
                                        if (emailExists) emailError = "Email đã được sử dụng"
                                        if (phoneExists) phoneError = "Số điện thoại đã được sử dụng"
                                    }
                                } else {
                                    val userId = generateShortId()
                                    val cartId = generateShortId()
                                    val wishlistId = generateShortId()
                                    val orderId = generateShortId()

                                    val newUser = User(
                                        id = userId,
                                        name = name,
                                        email = email,
                                        password = password,
                                        phone = phone,
                                        address = address,
                                        role = "user",
                                        avatar = "https://i.pinimg.com/736x/fd/bf/6f/fdbf6fa788ed6f1a0ff9432e61393489.jpg"
                                    )

                                    val newCart = Cart(id = cartId, userId = userId, items = emptyList())
                                    val newWishlist = Wishlist(id = wishlistId, userId = userId, items = emptyList())
                                    val newOrder = Order(
                                        id = orderId,
                                        userId = userId,
                                        total = 0,
                                        status = "Chưa có đơn hàng",
                                        items = emptyList(),
                                    )

                                    val createUserResponse = RetrofitClient.apiService.createUser(newUser)
                                    if (createUserResponse.isSuccessful) {
                                        val cartResponse = RetrofitClient.apiService.createCart(newCart)
                                        val wishlistResponse = RetrofitClient.apiService.createWishlist(newWishlist)
                                        val orderResponse = RetrofitClient.apiService.createOrder(newOrder)

                                        if (cartResponse.isSuccessful && wishlistResponse.isSuccessful && orderResponse.isSuccessful) {
                                            // Save userId to SharedPreferences
                                            context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                                                .edit()
                                                .putString("userId", userId)
                                                .apply()

                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(context, "Đăng ký thành công", Toast.LENGTH_SHORT).show()
                                                context.startActivity(Intent(context, Home::class.java))
                                                (context as? Activity)?.finish()
                                            }
                                        } else {
                                            withContext(Dispatchers.Main) {
                                                Toast.makeText(
                                                    context,
                                                    "Lỗi khi tạo dữ liệu phụ: ${
                                                        when {
                                                            !cartResponse.isSuccessful -> "giỏ hàng (${cartResponse.message()})"
                                                            !wishlistResponse.isSuccessful -> "danh sách yêu thích (${wishlistResponse.message()})"
                                                            else -> "đơn hàng (${orderResponse.message()})"
                                                        }
                                                    }",
                                                    Toast.LENGTH_LONG
                                                ).show()
                                            }
                                        }
                                    } else {
                                        withContext(Dispatchers.Main) {
                                            Toast.makeText(
                                                context,
                                                "Lỗi tạo người dùng: ${createUserResponse.message()}",
                                                Toast.LENGTH_LONG
                                            ).show()
                                        }
                                    }
                                }
                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        context,
                                        "Lỗi lấy danh sách người dùng: ${response.message()}",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                            }
                        } catch (e: Exception) {
                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4A3AFF)),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Đăng ký", color = Color.White)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text("Bạn đã có tài khoản? ")
                Text(
                    text = "Đăng nhập",
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.clickable {
                        context.startActivity(Intent(context, LoginScreen::class.java))
                    }
                )
            }
        }
    }
}