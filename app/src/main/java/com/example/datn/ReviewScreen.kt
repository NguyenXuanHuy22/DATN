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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel

class ReviewActivity : ComponentActivity() {
    private val reviewViewModel: ReviewViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val productId = intent.getStringExtra("productId").orEmpty()
        val orderId = intent.getStringExtra("orderId").orEmpty()
        val userId = intent.getStringExtra("userId").orEmpty()
        val username = intent.getStringExtra("username").orEmpty()
        val avatar = intent.getStringExtra("avatar").orEmpty()

        setContent {
            MaterialTheme {
                ReviewScreen(
                    userId = userId,
                    productId = productId,
                    orderId = orderId,
                    avatar = avatar,
                    username = username,
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
fun ReviewScreen(
    userId: String,
    productId: String,
    orderId: String,
    avatar: String,
    username: String,
    viewModel: ReviewViewModel,
    onReviewSuccess: () -> Unit
) {
    var rating by remember { mutableStateOf(0) }
    var comment by remember { mutableStateOf("") }

    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Viết đánh giá sản phẩm", style = MaterialTheme.typography.titleLarge)

        Spacer(Modifier.height(16.dp))

        // ⭐ Rating
        Row {
            for (i in 1..5) {
                IconButton(onClick = { rating = i }) {
                    Icon(
                        imageVector = if (i <= rating) Icons.Default.Star else Icons.Default.StarBorder,
                        contentDescription = null,
                        tint = if (i <= rating) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        // 📝 Nội dung
        OutlinedTextField(
            value = comment,
            onValueChange = { comment = it },
            label = { Text("Nội dung đánh giá") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                viewModel.addReview(
                    userId,
                    productId,
                    orderId,
                    avatar,
                    username,
                    rating,
                    comment,
                    onSuccess = onReviewSuccess
                )
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


