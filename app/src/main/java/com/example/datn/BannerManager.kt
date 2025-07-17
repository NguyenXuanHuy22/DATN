package com.example.datn

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight

data class BannerItem(val id: String, val imageUrl: String)

class BannerManager : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                BannerManagerActivity()
            }
        }
    }
}

@Composable
fun BannerManagerActivity() {
    val context = LocalContext.current

    var banners by remember {
        mutableStateOf(
            listOf(
                BannerItem("1", "https://cdn.pixabay.com/photo/2017/02/20/18/03/cat-2083492_1280.jpg"),
                BannerItem("2", "https://cdn.pixabay.com/photo/2020/05/17/20/26/flower-5182726_1280.jpg"),
                BannerItem("3", "https://cdn.pixabay.com/photo/2015/04/23/22/00/tree-736885_1280.jpg")
            )
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quản lý Banner") },
                navigationIcon = {
                    IconButton(onClick = {
                        // 👉 Quay lại màn trước
                        if (context is ComponentActivity) {
                            context.finish()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Quay lại")
                    }
                },
                backgroundColor = MaterialTheme.colors.primarySurface,
                contentColor = Color.White,
                elevation = 4.dp
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {

                },
                backgroundColor = MaterialTheme.colors.primary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Thêm banner")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(banners, key = { it.id }) { banner ->
                    BannerItemRow(banner) {
                        banners = banners.filterNot { it.id == banner.id }
                    }
                }
            }
        }
    }
}


@Composable
fun BannerItemRow(banner: BannerItem, onDelete: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFFF5F5F5), RoundedCornerShape(8.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(banner.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Banner",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(100.dp)
                .clip(RoundedCornerShape(8.dp))
        )

        Spacer(modifier = Modifier.width(16.dp))

        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(text = "ID: ${banner.id}", fontWeight = FontWeight.Bold)
            Text(text = banner.imageUrl, maxLines = 1)
        }

        IconButton(onClick = onDelete) {
            Icon(Icons.Default.Delete, contentDescription = "Xoá", tint = Color.Red)
        }
    }
}