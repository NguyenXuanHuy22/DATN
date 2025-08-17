package com.example.datn

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.datn.ui.theme.DATNTheme


class AddressScreen : ComponentActivity() {
    private val viewModel: AddressViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userId = getSharedPreferences("auth", MODE_PRIVATE)
            .getString("userId", "") ?: ""

        setContent {
            DATNTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AddressListScreen(
                        viewModel = viewModel,
                        onAddressSelected = { selected ->
                            Toast.makeText(
                                this,
                                "Đã chọn địa chỉ: ${selected.address}",
                                Toast.LENGTH_SHORT
                            ).show()
                            // TODO: trả địa chỉ selected về OrderScreen nếu cần
                        }
                    )
                }
            }
        }

        viewModel.loadAddresses(userId)
    }
}

// --- COMPOSABLE ---
@Composable
fun AddressListScreen(
    viewModel: AddressViewModel,
    onAddressSelected: (selectedAddress: Address) -> Unit
) {
    val userAddressList = viewModel.userAddress.value // List<Address> địa chỉ chính
    val addresses = viewModel.addresses.value          // List<Address> các địa chỉ khác
    val isLoading = viewModel.isLoading.value
    val errorMessage = viewModel.errorMessage.value

    var selectedAddressId by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // --- Header ---
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.ArrowBack,
                contentDescription = "Back",
                modifier = Modifier
                    .size(32.dp)
                    .clickable {
                        (context as? ComponentActivity)?.finish()
                    }
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                "Danh sách địa chỉ",
                fontSize = 24.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            isLoading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }

            errorMessage.isNotEmpty() -> Text(
                errorMessage,
                color = MaterialTheme.colorScheme.error
            )

            else -> LazyColumn {
                // --- 1. Địa chỉ chính của user ---
                items(userAddressList) { addr ->
                    AddressCard(
                        address = addr,
                        isSelected = selectedAddressId == addr._id,
                        onSelect = {
                            selectedAddressId = addr._id
                            onAddressSelected(addr)
                        },
                        onEdit = {
                            // TODO: điều hướng edit địa chỉ
                        }
                    )
                }

                // --- 2. Các địa chỉ khác ---
                items(addresses) { addr ->
                    AddressCard(
                        address = addr,
                        isSelected = selectedAddressId == addr._id,
                        onSelect = {
                            selectedAddressId = addr._id
                            onAddressSelected(addr)
                        },
                        onEdit = {
                            // TODO: điều hướng edit địa chỉ
                        }
                    )
                }

                // --- 3. Nút thêm địa chỉ ---
                item {
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = {
                            // TODO: điều hướng thêm địa chỉ
                        },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Thêm địa chỉ")
                    }
                }
            }
        }
    }
}

@Composable
fun AddressCard(
    address: Address,
    isSelected: Boolean = false,
    onSelect: () -> Unit,
    onEdit: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text("Tên: ${address.name}", fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("Địa chỉ: ${address.address}")
            Spacer(modifier = Modifier.height(2.dp))
            Text("Số điện thoại: ${address.phone}")
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(
                    onClick = onSelect,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = if (isSelected) Color.White else MaterialTheme.colorScheme.primary,
                        containerColor = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent
                    )
                ) {
                    Text(if (isSelected) "Đã chọn" else "Chọn")
                }
                Spacer(modifier = Modifier.width(8.dp))
                TextButton(
                    onClick = onEdit
                ) {
                    Text("Sửa", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}


