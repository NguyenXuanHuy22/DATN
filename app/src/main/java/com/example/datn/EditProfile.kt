package com.example.datn

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import com.example.datn.ui.theme.DATNTheme
import kotlinx.coroutines.launch
import android.graphics.BitmapFactory
import android.util.Base64
import android.graphics.Bitmap
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.graphics.asImageBitmap
import java.io.ByteArrayOutputStream


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
fun EditProfileScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val sharedPref = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    val userId = sharedPref.getString("userId", null) ?: return

    var user by remember { mutableStateOf<User?>(null) }
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var password by remember { mutableStateOf(TextFieldValue("")) }
    var phone by remember { mutableStateOf(TextFieldValue("")) }
    var address by remember { mutableStateOf(TextFieldValue("")) }
    var pickedImageUri by remember { mutableStateOf<Uri?>(null) }
    var avatarBitmap by remember { mutableStateOf<Bitmap?>(null) }

    val imagePicker = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri ->
        pickedImageUri = uri
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            avatarBitmap = BitmapFactory.decodeStream(inputStream)
        }
    }

    // Load user từ API
    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.apiService.getUserById(userId)
            user = response
            name = TextFieldValue(response.name ?: "")
            email = TextFieldValue(response.email ?: "")
            phone = TextFieldValue(response.phone ?: "")
            address = TextFieldValue(response.address ?: "")
            password = TextFieldValue(response.password ?: "")

            response.avatar?.let { base64 ->
                val cleanBase64 = base64.substringAfter("base64,", base64)
                val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                avatarBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            }
        } catch (e: Exception) {
            Log.e("EditProfile", "Lỗi load user", e)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Row trên cùng: Back + Title
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.ArrowBack,
                contentDescription = "Back",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .size(40.dp)
                    .clickable {
                        (context as? Activity)?.finish()
                    }
                    .padding(8.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Chỉnh sửa thông tin",
                style = MaterialTheme.typography.titleMedium
            )
        }


        Spacer(modifier = Modifier.height(24.dp))

        // Avatar
        Box(
            modifier = Modifier
                .size(100.dp)
                .align(Alignment.CenterHorizontally)
                .clip(CircleShape)
                .clickable { imagePicker.launch("image/*") }
        ) {
            avatarBitmap?.let {
                Image(
                    bitmap = it.asImageBitmap(),
                    contentDescription = "Avatar",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            } ?: Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Gray)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { imagePicker.launch("image/*") }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
            Text("Đổi ảnh")
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(name, { name = it }, label = { Text("Tên") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(phone, { phone = it }, label = { Text("SĐT") }, modifier = Modifier.fillMaxWidth())
        OutlinedTextField(address, { address = it }, label = { Text("Địa chỉ") }, modifier = Modifier.fillMaxWidth())

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                scope.launch {
                    try {
                        // Chuyển ảnh mới sang Base64 nếu có
                        val avatarBase64 = pickedImageUri?.let { uri ->
                            val inputStream = context.contentResolver.openInputStream(uri)
                            val bitmap = BitmapFactory.decodeStream(inputStream)
                            val outputStream = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                            val bytes = outputStream.toByteArray()
                            Base64.encodeToString(bytes, Base64.DEFAULT)
                        }

                        // Dữ liệu cập nhật
                        val updateData = UpdateUserRequest(
                            name = name.text,
                            email = email.text,
                            phone = phone.text,
                            address = address.text,
                            avatar = avatarBase64 ?: user?.avatar
                        )

                        // Gọi API update
                        val res = RetrofitClient.apiService.updateUserJson(userId, updateData)

                        if (res.isSuccessful) {
                            Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show()
                            user = res.body()
                            pickedImageUri = null

                            // Cập nhật avatarBitmap mới từ server
                            res.body()?.avatar?.let { base64 ->
                                val cleanBase64 = base64.substringAfter("base64,", base64)
                                val bytes = Base64.decode(cleanBase64, Base64.DEFAULT)
                                avatarBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                            }

                            // --- Thêm vào đây ---
                            val activity = context as Activity
                            activity.setResult(Activity.RESULT_OK)
                            activity.finish()

                        } else {
                            Toast.makeText(context, "Cập nhật thất bại", Toast.LENGTH_SHORT).show()
                        }

                    } catch (e: Exception) {
                        Log.e("EditProfile", "Lỗi cập nhật", e)
                        Toast.makeText(context, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Lưu thay đổi")
        }

    }
}







