package com.example.datn

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
import java.util.UUID

// Màn hình đăng ký là một ComponentActivity (hoạt động riêng biệt)
class RegisterScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Bật chế độ full màn hình
        setContent {
            DATNTheme {
                RegisterScreenContent() // Gọi hàm giao diện chính
            }
        }
    }
}

@Composable
fun RegisterScreenContent() {
    val context = LocalContext.current

    // Các biến trạng thái để lưu giá trị người dùng nhập
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) } // Hiện/ẩn mật khẩu

    // Trạng thái lỗi cho từng trường nhập liệu
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
            // Tiêu đề màn hình
            Text("Tạo tài khoản", fontSize = 28.sp, fontWeight = FontWeight.Bold)
            Text("Hãy tạo một tài khoản cho bạn", fontSize = 16.sp, color = Color.Gray)

            // Nhập tên
            OutlinedTextField(
                value = name,
                onValueChange = {
                    name = it
                    nameError = ""
                },
                label = { Text("Tên") },
                isError = nameError.isNotEmpty(),
                supportingText = {
                    if (nameError.isNotEmpty()) Text(nameError, color = MaterialTheme.colorScheme.error)
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Nhập email
            OutlinedTextField(
                value = email,
                onValueChange = {
                    email = it
                    emailError = ""
                },
                label = { Text("Email") },
                isError = emailError.isNotEmpty(),
                supportingText = {
                    if (emailError.isNotEmpty()) Text(emailError, color = MaterialTheme.colorScheme.error)
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Nhập số điện thoại
            OutlinedTextField(
                value = phone,
                onValueChange = {
                    phone = it
                    phoneError = ""
                },
                label = { Text("SĐT") },
                isError = phoneError.isNotEmpty(),
                supportingText = {
                    if (phoneError.isNotEmpty()) Text(phoneError, color = MaterialTheme.colorScheme.error)
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Nhập mật khẩu + hiện/ẩn
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
                            if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = null
                        )
                    }
                },
                isError = passwordError.isNotEmpty(),
                supportingText = {
                    if (passwordError.isNotEmpty()) Text(passwordError, color = MaterialTheme.colorScheme.error)
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Nhập địa chỉ
            OutlinedTextField(
                value = address,
                onValueChange = {
                    address = it
                    addressError = ""
                },
                label = { Text("Địa chỉ") },
                isError = addressError.isNotEmpty(),
                supportingText = {
                    if (addressError.isNotEmpty()) Text(addressError, color = MaterialTheme.colorScheme.error)
                },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(24.dp))

            // Nút đăng ký
            Button(
                onClick = {
                    // Reset lỗi trước khi kiểm tra lại
                    nameError = ""
                    emailError = ""
                    phoneError = ""
                    passwordError = ""
                    addressError = ""

                    val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")
                    var hasError = false

                    // Kiểm tra từng trường
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

                    // Nếu có lỗi, không tiếp tục
                    if (hasError) return@Button

                    // Gọi API kiểm tra email và thêm user mới
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            val users = RetrofitClient.apiService.getUsers()
                            val emailExists = users.any { it.email == email }

                            if (emailExists) {
                                withContext(Dispatchers.Main) {
                                    emailError = "Email đã được sử dụng"
                                }
                            } else {
                                // Tạo đối tượng user mới với ID ngẫu nhiên và các ID liên kết
                                val newUser = User(
                                    id = UUID.randomUUID().toString(),
                                    name = name,
                                    email = email,
                                    password = password,
                                    phone = phone,
                                    address = address,
                                    role = "user",
                                    cartId = UUID.randomUUID().toString(),
                                    wishlistId = UUID.randomUUID().toString(),
                                    orderHistoryId = UUID.randomUUID().toString()
                                )

                                // Gọi API tạo người dùng
                                RetrofitClient.apiService.createUser(newUser)

                                withContext(Dispatchers.Main) {
                                    Toast.makeText(context, "Đăng ký thành công", Toast.LENGTH_SHORT).show()
                                    // Điều hướng đến Home
                                    val intent = Intent(context, Home::class.java)
                                    context.startActivity(intent)
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

            // Link điều hướng đến màn hình đăng nhập
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
                Text("Bạn đã có tài khoản? ")
                Text(
                    "Đăng nhập",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable {
                        val intent = Intent(context, LoginScreen::class.java)
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}
