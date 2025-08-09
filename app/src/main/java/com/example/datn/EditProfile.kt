package com.example.datn

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.datn.ui.theme.DATNTheme
import kotlinx.coroutines.launch

class EditProfile : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DATNTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    EditProfileScreen()
                }
            }
        }
    }
}

@Composable
fun EditProfileScreen() {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sharedPref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    val userId = sharedPref.getString("userId", null) ?: ""

    var user by remember { mutableStateOf<User?>(null) }
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var phone by remember { mutableStateOf(TextFieldValue("")) }
    var address by remember { mutableStateOf(TextFieldValue("")) }
    var avatarUrl by remember { mutableStateOf("") }
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.apiService.getUserById(userId)
            user = response
            name = TextFieldValue(response.name)
            email = TextFieldValue(response.email)
            password = TextFieldValue(response.password)
            phone = TextFieldValue(response.phone)
            address = TextFieldValue(response.address)
            avatarUrl = response.avatar.toString()
        } catch (e: Exception) {
            Toast.makeText(context, "Lỗi tải dữ liệu", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        Image(
            painter = rememberAsyncImagePainter(avatarUrl),
            contentDescription = null,
            modifier = Modifier.size(100.dp)
        )
        OutlinedTextField(
            value = avatarUrl,
            onValueChange = { avatarUrl = it },
            label = { Text("Link ảnh đại diện") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Họ và tên") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Mật khẩu") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Số điện thoại") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )
        OutlinedTextField(
            value = address,
            onValueChange = { address = it },
            label = { Text("Địa chỉ") },
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        )

        Button(
            onClick = { showDialog = true },
            modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
        ) {
            Text("Lưu thay đổi")
        }

        if (showDialog) {
            AlertDialog(
                onDismissRequest = { showDialog = false },
                title = { Text("Xác nhận") },
                text = { Text("Bạn có chắc muốn lưu thay đổi không?") },
                confirmButton = {
                    TextButton(onClick = {
                        showDialog = false
                        scope.launch {
                            val updatedUser = User(
                                _id = userId,
                                name = name.text,
                                email = email.text,
                                password = password.text,
                                phone = phone.text,
                                address = address.text,
                                avatar = avatarUrl,
                                role = user?.role ?: "user"
                            )
                            try {
                                val response = RetrofitClient.apiService.updateUser(userId, updatedUser)
                                if (response.isSuccessful) {
                                    Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                                    (context as? Activity)?.finish()
                                } else {
                                    Toast.makeText(context, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
                                }
                            } catch (e: Exception) {
                                Toast.makeText(context, "Lỗi cập nhật: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }) {
                        Text("Có")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDialog = false }) {
                        Text("Không")
                    }
                }
            )
        }
    }
}
