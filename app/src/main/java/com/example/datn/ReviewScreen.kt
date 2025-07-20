package com.example.datn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.lazy.items
import androidx.compose.material.IconButton
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.filled.ArrowBack





class ReviewScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ReviewScreen(reviews = sampleReviews)
        }
    }
}

data class Review(
    val id: String,
    val userId: String,
    val productId: String,
    val username: String,
    val ratingStar: Int,
    val commentDes: String
)

val sampleReviews = listOf(
    Review("2", "QZsU2tCy", "2", "huyhuy", 5, "Rất hài lòng với áo này!"),
    Review("3", "UYT29LKj", "2", "linhlinh", 4, "Áo đẹp, chất vải ổn nhưng giao hơi chậm."),
    Review("4", "NXT89UJk", "2", "anhphuc", 3, "Áo tạm ổn, không như kỳ vọng."),
    Review("5", "MKP23ZcX", "2", "trangtrang", 5, "Áo siêu xinh, đóng gói cẩn thận."),
    Review("6", "TYU12GHk", "2", "minhtri", 2, "Không giống hình.")
)

@Composable
fun ReviewScreen(reviews: List<Review>, onBackClick: () -> Unit = {}) {
    Scaffold(
        topBar = {
            TopAppBar(
                backgroundColor = Color.White,
                elevation = 4.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp), // chiều cao mặc định AppBar
                    contentAlignment = Alignment.Center
                ) {
                    // Title căn giữa
                    Text(
                        text = "Reviews",
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.Black
                    )

                    // Nút back ở trái
                    IconButton(
                        onClick = onBackClick,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            }
        },
        content = { paddingValues ->
            LazyColumn(
                modifier = Modifier
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(reviews) { review ->
                    ReviewItem(review)
                    Divider(modifier = Modifier.padding(vertical = 12.dp))
                }
            }
        }
    )
}

@Composable
fun ReviewItem(review: Review) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 4.dp)
        ) {
            Text(
                text = review.username,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
            )
            Spacer(modifier = Modifier.width(8.dp))
            RatingBar(rating = review.ratingStar)
        }
        Text(
            text = review.commentDes,
            style = MaterialTheme.typography.bodyMedium,
            color = Color.DarkGray
        )
    }
}

@Composable
fun RatingBar(rating: Int) {
    Row {
        repeat(5) { index ->
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = null,
                tint = if (index < rating) Color(0xFFFFC107) else Color.LightGray,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}