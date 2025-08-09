package com.example.datn

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
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
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.datn.utils.toDecimalString

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
        viewModel.getListProducts()
    }

    val categories = remember(productList) {
        listOf("Tất cả") + productList.map { it.category }.distinct()
    }

    val filteredProducts = productList.filter {
        (selectedCategory == "Tất cả" || it.category == selectedCategory) &&
                it.name.contains(searchText, ignoreCase = true)
    }

    Scaffold(
        bottomBar = { BottomNavigationBar(currentScreen = "Home") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFFEEEEEE))  // nền xám nhẹ cả màn
                .padding(innerPadding)
                .padding(horizontal = 12.dp, vertical = 8.dp)  // padding đều 2 bên
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = searchText,
                onValueChange = { searchText = it },
                placeholder = { Text("Tìm kiếm sản phẩm...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val intent = Intent(context, SearchActivity::class.java)
                        context.startActivity(intent)
                    },
                readOnly = true,
                enabled = false
            )

            Spacer(modifier = Modifier.height(16.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                items(categories.size) { index ->
                    val category = categories[index]
                    val isSelected = selectedCategory == category
                    Surface(
                        color = if (isSelected) MaterialTheme.colorScheme.primary else Color.LightGray,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.clickable { selectedCategory = category }
                    ) {
                        Text(
                            text = category,
                            color = if (isSelected) Color.White else Color.Black,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(filteredProducts) { product ->
                    ProductItem(product = product) {
                        val intent = Intent(context, ProductDetail::class.java)
                        intent.putExtra("productId", product._id)
                        context.startActivity(intent)
                    }
                }
            }
        }
    }
}


@Composable
fun ProductItem(
    product: Product,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF5F5F5))  // nền sáng nhẹ cho item, giống FavoriteItem
            .clickable { onClick() }
            .padding(bottom = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp))
                .background(Color.LightGray)
        ) {
            AsyncImage(
                model = product.image ?: "",
                contentDescription = product.name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)),
                error = painterResource(id = R.drawable.logo),
                placeholder = painterResource(id = R.drawable.logo)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = product.name,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(horizontal = 7.dp),
            color = Color.Black
        )

        Text(
            text = "${product.price.toDecimalString()} VND",
            color = Color(0xFFD32F2F),
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 7.dp)
        )
    }
}


@Composable
fun BottomNavigationBar(currentScreen: String) {
    val context = LocalContext.current

    BottomNavigation(
        backgroundColor = Color.White,
        contentColor = Color.Black,
        elevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        val items = listOf(
            Triple("Home", Icons.Default.Home, Home::class.java),
            Triple("Search", Icons.Default.Search, SearchActivity::class.java),
            Triple("Saved", Icons.Default.Favorite, Favorite::class.java),
            Triple("Cart", Icons.Default.ShoppingCart, CartScreen::class.java),
            Triple("Account", Icons.Default.AccountCircle, Account::class.java)
        )

        items.forEach { (label, icon, activityClass) ->
            BottomNavigationItem(
                selected = currentScreen == label,
                onClick = {
                    // 👉 Khi click chuyển sang màn mới nếu chưa phải màn hiện tại
                    if (currentScreen != label) {
                        context.startActivity(Intent(context, activityClass))
                    }
                },
                icon = {
                    Icon(
                        imageVector = icon,
                        contentDescription = label,
                        modifier = Modifier.size(20.dp),
                        // 👉 Sửa tại đây: Nếu được chọn thì icon màu đen, không thì màu xám
                        tint = if (currentScreen == label) Color.Black else Color.Gray
                    )
                },
                label = {
                    Text(
                        text = label,
                        fontSize = 10.sp,
                        // 👉 Sửa tại đây: Nếu được chọn thì chữ màu đen, không thì màu xám
                        color = if (currentScreen == label) Color.Black else Color.Gray
                    )
                }
            )
        }
    }
}


