package com.example.datn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.datn.ui.theme.DATNTheme

class ReviewListScreen : ComponentActivity() {

    private val reviewViewModel: ReviewViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // ✅ Nhận productId từ Intent
        val productId = intent.getStringExtra("productId") ?: ""

        // Gọi API lấy comments
        reviewViewModel.getCommentsByProduct(productId)

        setContent {
            DATNTheme {
                val comments by reviewViewModel.comments.collectAsState()
                val isLoading by reviewViewModel.isLoading.collectAsState()
                val errorMessage by reviewViewModel.errorMessage.collectAsState()

                ReviewList(
                    comments = comments,
                    isLoading = isLoading,
                    errorMessage = errorMessage,
                    onBack = { finish() }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReviewList(
    comments: List<Comment>,
    isLoading: Boolean,
    errorMessage: String?,
    onBack: () -> Unit
) {
    var selectedStar by remember { mutableStateOf(0) } // 0 = tất cả

    // Lọc comment theo rating
    val filteredComments = if (selectedStar == 0) comments else comments.filter { it.ratingStar == selectedStar }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Đánh giá sản phẩm") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // ⭐ Thanh lọc theo số sao
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(label = "Tất cả", isSelected = selectedStar == 0) {
                    selectedStar = 0
                }
                (1..5).forEach { star ->
                    FilterChip(label = "$star★", isSelected = selectedStar == star) {
                        selectedStar = star
                    }
                }
            }

            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    isLoading -> {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                    errorMessage != null -> {
                        Text(
                            text = errorMessage,
                            color = Color.Red,
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    filteredComments.isEmpty() -> {
                        Text(
                            text = "Chưa có đánh giá nào",
                            modifier = Modifier.align(Alignment.Center)
                        )
                    }
                    else -> {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(12.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(filteredComments) { comment ->
                                CommentCard(comment)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun FilterChip(label: String, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        color = if (isSelected) Color(0xFF1976D2) else Color.LightGray.copy(alpha = 0.3f),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .clickable { onClick() }
            .padding(horizontal = 4.dp)
    ) {
        Text(
            text = label,
            color = if (isSelected) Color.White else Color.Black,
            fontSize = 14.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
fun CommentCard(comment: Comment) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF9F9F9)),
        elevation = CardDefaults.cardElevation(6.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            // ⭐ Avatar từ base64
            val context = LocalContext.current
            val imageRequest = remember(comment.avatar) {
                if (comment.avatar?.startsWith("/9j/") == true) {
                    val decodedBytes = android.util.Base64.decode(comment.avatar, android.util.Base64.DEFAULT)
                    coil.request.ImageRequest.Builder(context)
                        .data(decodedBytes)
                        .crossfade(true)
                        .build()
                } else {
                    coil.request.ImageRequest.Builder(context)
                        .data(comment.avatar)
                        .crossfade(true)
                        .build()
                }
            }

            AsyncImage(
                model = imageRequest,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(Color.Gray),
                contentScale = ContentScale.Crop
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = comment.username,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    RatingBar(rating = comment.ratingStar)
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = comment.commentDes,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}

@Composable
fun RatingBar(rating: Int, maxRating: Int = 5) {
    Row {
        repeat(maxRating) { index ->
            val icon = if (index < rating) Icons.Default.Star else Icons.Default.StarBorder
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = Color(0xFFFFD700), // vàng
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

