package com.example.datn

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
//import com.example.datn.ui.theme.DATNTheme

class Home : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                HomeScreen()
            }
        }
    }
}

@Composable
fun HomeScreen(viewModel: ProductViewModel = viewModel()) {
    val context = LocalContext.current
    val productList by viewModel.products.observeAsState(emptyList())
    var selectedCategory by remember { mutableStateOf("Tất cả") }
    var searchText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        viewModel.getListProducts() // đúng tên hàm
    }

    Scaffold(
        bottomBar = { BottomNavigationBar() }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(8.dp)
        ) {
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Poly Sport", style = MaterialTheme.typography.h6, modifier = Modifier.weight(1f))
                IconButton(onClick = { }) {
                    Icon(Icons.Default.Face, contentDescription = "Filter")
                }
            }

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("Tìm kiếm") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            val categories = listOf("Tất cả", "Bóng đá", "Cầu lông", "Pickleball")
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                categories.forEach { category ->
                    val isSelected = selectedCategory == category
                    Text(
                        text = category,
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(if (isSelected) Color.Black else Color.LightGray)
                            .clickable { selectedCategory = category }
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                        color = if (isSelected) Color.White else Color.Black
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxHeight()
            ) {
                items(productList.filter {
                    it.name.contains(searchText, ignoreCase = true)
                }) { product ->
                    ProductItem(product = product) {
                        val intent = Intent(context, ProductDetail::class.java)
                        intent.putExtra("productId", product.id)
                        context.startActivity(intent)
                    }
                }
            }
        }
    }
}

@Composable
fun ProductItem(product: Product, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() }
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(product.image)
                .crossfade(true)
                .build(),
            contentDescription = product.name,
            modifier = Modifier
                .height(150.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(10.dp)),
            contentScale = ContentScale.Crop
        )
        Text(
            text = product.name,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(top = 4.dp)
        )
        Text(text = "${product.price} vnd", color = Color.Gray)
    }
}

@Composable
fun BottomNavigationBar() {
    val context = LocalContext.current

    BottomNavigation(
        backgroundColor = Color.White,
        contentColor = Color.Black
    ) {
        BottomNavigationItem(
            selected = true,
            onClick = {},
            icon = { Icon(Icons.Default.Home, contentDescription = null) },
            label = { Text("Home") }
        )
        BottomNavigationItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Default.Search, contentDescription = null) },
            label = { Text("Search") }
        )
        BottomNavigationItem(
            selected = false,
            onClick = {},
            icon = { Icon(Icons.Default.Favorite, contentDescription = null) },
            label = { Text("Saved") }
        )
        BottomNavigationItem(
            selected = false,
            onClick = {},
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


