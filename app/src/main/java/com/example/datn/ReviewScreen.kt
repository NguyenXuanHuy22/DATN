package com.example.datn

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.Button
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.IconButton
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.Card
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.lifecycle.viewmodel.compose.viewModel

class ReviewActivity : ComponentActivity() {
    private val reviewViewModel: ReviewViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val orderId = intent.getStringExtra("orderId").orEmpty()
        val userId = intent.getStringExtra("userId").orEmpty()
        val username = intent.getStringExtra("username").orEmpty()
        val avatar = intent.getStringExtra("avatar").orEmpty()

        // ✅ Nhận danh sách sản phẩm từ order
        val productList = intent.getSerializableExtra("productList") as? ArrayList<OrderItem> ?: arrayListOf()

        setContent {
            MaterialTheme {
                MultiProductReviewScreen(
                    userId = userId,
                    orderId = orderId,
                    avatar = avatar,
                    username = username,
                    products = productList,
                    viewModel = reviewViewModel,
                    onReviewSuccess = {
                        Toast.makeText(this, "Đã gửi đánh giá!", Toast.LENGTH_SHORT).show()
                        setResult(RESULT_OK)
                        finish()
                    }
                )
            }
        }
    }
}

@Composable
fun MultiProductReviewScreen(
    userId: String,
    orderId: String,
    avatar: String,
    username: String,
    products: List<OrderItem>,
    viewModel: ReviewViewModel,
    onReviewSuccess: () -> Unit
) {
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Map lưu rating & comment cho từng productId
    val ratings = remember { mutableStateMapOf<String, Int>() }
    val comments = remember { mutableStateMapOf<String, String>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Đánh giá sản phẩm", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(16.dp))

        products.forEach { product ->
            Card (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(product.name, fontWeight = FontWeight.Bold)

                    Spacer(Modifier.height(8.dp))

                    // ⭐ Rating
                    Row {
                        for (i in 1..5) {
                            IconButton(onClick = { ratings[product.productId] = i }) {
                                Icon(
                                    imageVector = if (i <= (ratings[product.productId] ?: 0))
                                        Icons.Default.Star else Icons.Default.StarBorder,
                                    contentDescription = null,
                                    tint = if (i <= (ratings[product.productId] ?: 0))
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // 📝 Nội dung đánh giá
                    OutlinedTextField(
                        value = comments[product.productId] ?: "",
                        onValueChange = { comments[product.productId] = it },
                        label = { Text("Nội dung đánh giá") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                var hasValidReview = false
                products.forEach { product ->
                    val rating = ratings[product.productId] ?: 0
                    val comment = comments[product.productId] ?: ""

                    if (rating > 0 && comment.isNotBlank()) {
                        hasValidReview = true
                        viewModel.addReview(
                            userId = userId,
                            productId = product.productId,
                            orderId = orderId,
                            avatar = avatar,
                            username = username,
                            ratingStar = rating,
                            commentDes = comment,
                            onSuccess = onReviewSuccess
                        )
                    }
                }

                if (!hasValidReview) {
                    viewModel.setErrorMessage("Vui lòng chọn số sao và nhập nội dung đánh giá ít nhất cho 1 sản phẩm")
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Gửi đánh giá")
            }
        }

        errorMessage?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}

