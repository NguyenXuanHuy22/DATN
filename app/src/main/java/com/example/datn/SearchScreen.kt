package com.example.datn

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest

@Composable
fun SearchScreen(viewModel: ProductViewModel = androidx.lifecycle.viewmodel.compose.viewModel()) {
    val context = LocalContext.current
    val allProducts by viewModel.products.observeAsState(emptyList())
    var query by remember { mutableStateOf("") }
    LaunchedEffect(Unit) {
        viewModel.getListProducts() //  THÊM DÒNG NÀY
    }
    val suggestions = listOf(
        "Áo đá bóng", "Áo cầu lông", "kamito", "Nike shoes black", "V-neck t-shirt"
    )

    val filteredProducts = allProducts.filter {
        it.name.contains(query, ignoreCase = true)
    }

    Scaffold(
        bottomBar = { BottomNavigationBarSearch() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp)
                .padding(innerPadding)
        ) {
            // Header
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Tìm kiếm", style = MaterialTheme.typography.titleLarge)
            }

            // Search Input
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                placeholder = { Text("Search for clothes...") },
                leadingIcon = { Icon(Icons.Default.Search, null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            when {
                query.isEmpty() -> {
                    Text(
                        "Suggestions",
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                    LazyColumn {
                        items(suggestions) { item ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable { query = item }
                                    .padding(12.dp)
                            ) {
                                Text(item)
                            }
                        }
                    }
                }

                filteredProducts.isNotEmpty() -> {
                    LazyColumn {
                        items(filteredProducts) { product ->
                            Row(
                                Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        val intent = Intent(context, ProductDetail::class.java)
                                        intent.putExtra("productId", product.id)
                                        context.startActivity(intent)
                                    }
                                    .padding(8.dp)
                            ) {
                                AsyncImage(
                                    model = ImageRequest.Builder(LocalContext.current)
                                        .data(product.image)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = product.name,
                                    modifier = Modifier
                                        .size(80.dp)
                                        .clip(RoundedCornerShape(10.dp)),
                                    contentScale = ContentScale.Crop
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(product.name, fontWeight = FontWeight.Bold)
                                    Text("${product.price} vnd", color = Color.Gray)
                                }
                            }
                        }
                    }
                }

                else -> {
                    Column(
                        Modifier
                            .fillMaxSize()
                            .padding(top = 40.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_empty),
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Text("No Results Found!", fontWeight = FontWeight.Bold)
                        Text("Try a similar word or something more general.", color = Color.Gray)
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBarSearch() {
    val context = LocalContext.current

    BottomNavigation(
        backgroundColor = Color.White,
        contentColor = Color.Black
    ) {
        BottomNavigationItem(
            selected = false,
            onClick = {
                val intent = Intent(context, Home::class.java)
                context.startActivity(intent)
            },
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Home") }
        )
        BottomNavigationItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.Default.Search, contentDescription = null) },
            label = { Text("Search") }
        )
        BottomNavigationItem(
            selected = false,
            onClick = {
                val intent = Intent(context, Favorite::class.java)
                context.startActivity(intent)
            },
            icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
            label = { Text("Saved") }
        )
        BottomNavigationItem(
            selected = false,
            onClick = {
            },
            icon = { Icon(Icons.Default.ShoppingCart, contentDescription = null) },
            label = { Text("Cart") }
        )
        BottomNavigationItem(
            selected = false,
            onClick = {
                val intent = Intent(context, Account::class.java)
                context.startActivity(intent)
            },
            icon = { Icon(Icons.Default.AccountCircle, contentDescription = null) },
            label = { Text("Account") }
        )
    }
}
