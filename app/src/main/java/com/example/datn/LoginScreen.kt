package com.example.datn

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

class LoginScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DATNTheme {
                LoginScreenContent()
            }
        }
    }
}

@Composable
fun LoginScreenContent() {
    val context = LocalContext.current

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(24.dp))

        Text("Đăng nhập", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Text(
            "Thật vui khi gặp được bạn",
            fontSize = 16.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                emailError = ""
            },
            label = { Text("Email") },
            placeholder = { Text("Nhập địa chỉ email của bạn") },
            isError = emailError.isNotEmpty(),
            supportingText = {
                if (emailError.isNotEmpty())
                    Text(emailError, color = MaterialTheme.colorScheme.error)
            },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = ""
            },
            label = { Text("Mật khẩu") },
            placeholder = { Text("Nhập mật khẩu của bạn") },
            isError = passwordError.isNotEmpty(),
            supportingText = {
                if (passwordError.isNotEmpty())
                    Text(passwordError, color = MaterialTheme.colorScheme.error)
            },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = icon, contentDescription = null)
                }

            },
            singleLine = true,

        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                emailError = ""
                passwordError = ""

                var hasError = false

                if (email.isBlank()) {
                    emailError = "Vui lòng nhập email"
                    hasError = true
                }
                if (password.isBlank()) {
                    passwordError = "Vui lòng nhập mật khẩu"
                    hasError = true
                }

                if (hasError) return@Button

                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val response = RetrofitClient.apiService.getUsers()
                        withContext(Dispatchers.Main) {
                            if (response.isSuccessful) {
                                val users = response.body() ?: emptyList()
                                val user = users.find { u -> u.email == email && u.password == password }

                                if (user != null) {
                                    // Save userId to SharedPreferences
                                    context.getSharedPreferences("auth", Context.MODE_PRIVATE)
                                        .edit()
                                        .clear()
                                        .putString("userId", user._id)
                                        .putString("userRole", user.role)
                                        .apply()

                                    Toast.makeText(context, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()
                                    val intent = Intent(context, Home::class.java)
                                    context.startActivity(intent)
                                    (context as ComponentActivity).finish()
                                } else {
                                    emailError = "Email hoặc mật khẩu không đúng"
                                    passwordError = "Email hoặc mật khẩu không đúng"
                                }
                            } else {
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
            Text("Đăng nhập", color = Color.White)
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ) {
            Text("Bạn chưa có tài khoản? ")
            Text(
                text = "Đăng ký",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable {
                    val intent = Intent(context, RegisterScreen::class.java)
                    context.startActivity(intent)
                }
            )
        }
    }
}