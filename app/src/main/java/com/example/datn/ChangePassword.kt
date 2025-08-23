package com.example.datn

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

class ChangePassword : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                ChangePasswordContent()
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangePasswordContent() {
    val context = LocalContext.current
    val shared = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    val userId = shared.getString("userId", "") ?: ""

    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }

    var showOld by remember { mutableStateOf(false) }
    var showNew by remember { mutableStateOf(false) }
    var showConfirm by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Đổi mật khẩu") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (context is Activity) context.finish()
                    }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Quay lại"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // TODO: Card + TextFields đẹp như bạn đã làm (mình rút gọn phần UI ở đây)
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = oldPassword,
                    onValueChange = { oldPassword = it },
                    label = { Text("Mật khẩu cũ") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showOld) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showOld = !showOld }) {
                            Icon(
                                imageVector = if (showOld) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = newPassword,
                    onValueChange = { newPassword = it },
                    label = { Text("Mật khẩu mới (≥ 6 ký tự)") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showNew) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showNew = !showNew }) {
                            Icon(
                                imageVector = if (showNew) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    }
                )

                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Xác nhận mật khẩu mới") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = if (showConfirm) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showConfirm = !showConfirm }) {
                            Icon(
                                imageVector = if (showConfirm) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                                contentDescription = null
                            )
                        }
                    }
                )

                Button(
                    onClick = {
                        // Validate phía client
                        when {
                            oldPassword.isBlank() || newPassword.isBlank() || confirmPassword.isBlank() ->
                                Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()

                            newPassword.length < 6 ->
                                Toast.makeText(context, "Mật khẩu mới phải từ 6 ký tự", Toast.LENGTH_SHORT).show()

                            newPassword != confirmPassword ->
                                Toast.makeText(context, "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show()

                            oldPassword == newPassword ->
                                Toast.makeText(context, "Mật khẩu mới không được trùng mật khẩu cũ", Toast.LENGTH_SHORT).show()

                            userId.isBlank() ->
                                Toast.makeText(context, "Không tìm thấy userId. Vui lòng đăng nhập lại.", Toast.LENGTH_SHORT).show()

                            else -> {
                                isLoading = true
                                scope.launch {
                                    try {
                                        val res = RetrofitClient.apiService.changePassword(
                                            userId,
                                            ChangePasswordRequest(oldPassword, newPassword)
                                        )
                                        if (res.isSuccessful) {
                                            Toast.makeText(context, res.body()?.message ?: "Đổi mật khẩu thành công", Toast.LENGTH_SHORT).show()
                                            // Optional: đóng màn hình
                                            if (context is Activity) context.finish()
                                        } else {
                                            val msg = try {
                                                // cố gắng đọc message từ server
                                                val err = res.errorBody()?.string()
                                                // rất ngắn gọn: tìm "message":"...".
                                                Regex("\"message\"\\s*:\\s*\"(.*?)\"").find(err ?: "")?.groupValues?.getOrNull(1)
                                            } catch (_: Exception) { null }
                                            Toast.makeText(context, msg ?: "Đổi mật khẩu thất bại (${res.code()})", Toast.LENGTH_SHORT).show()
                                        }
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Lỗi mạng: ${e.message}", Toast.LENGTH_SHORT).show()
                                    } finally {
                                        isLoading = false
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text(if (isLoading) "Đang xử lý..." else "Xác nhận")
                }
            }

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp)
                )
            }
        }
    }
}

