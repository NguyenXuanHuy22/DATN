package com.example.datn

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.*
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Alignment
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import com.example.datn.utils.toDecimalString
import androidx.compose.material.icons.filled.Search
import com.google.accompanist.pager.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton


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
fun HomeScreen(
    productViewModel: ProductViewModel = viewModel(),
    bannerViewModel: BannerViewModel = viewModel()
) {
    val context = LocalContext.current
    val productList by productViewModel.products.observeAsState(emptyList())
    val banners = bannerViewModel.banners

    var searchText by remember { mutableStateOf("") }
    var selectedCategories by remember { mutableStateOf(setOf<String>()) }
    var priceSort by remember { mutableStateOf("none") }
    var showFilter by remember { mutableStateOf(false) }

    // ❌ Bị sai: remember trong LazyColumn
    // ✅ Đưa selectedCategory ra ngoài
    var selectedCategory by remember { mutableStateOf<String?>("Tất cả") }

    LaunchedEffect(Unit) {
        productViewModel.getListProducts()
        bannerViewModel.loadBanners()
    }

    val categories = remember(productList) {
        productList.map { it.category }.distinct().take(7)
    }

    // Lọc sản phẩm
    val filteredProducts = productList
        .filter { product ->
            (selectedCategories.isEmpty() || selectedCategories.contains(product.category)) &&
                    (selectedCategory == null || product.category == selectedCategory || selectedCategory == "Tất cả") &&
                    product.name.contains(searchText, ignoreCase = true)
        }
        .let {
            when (priceSort) {
                "lowToHigh" -> it.sortedBy { p -> p.price }
                "highToLow" -> it.sortedByDescending { p -> p.price }
                else -> it
            }
        }

    Scaffold(
        bottomBar = { BottomNavigationBar(currentScreen = "Home") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Thanh tìm kiếm + icon lọc
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    placeholder = { Text("Tìm kiếm sản phẩm...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .weight(1f)
                        .clickable {
                            val intent = Intent(context, SearchActivity::class.java)
                            context.startActivity(intent)
                        },
                    readOnly = true,
                    enabled = false
                )

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(onClick = { showFilter = true }) {
                    Icon(Icons.Default.FilterList, contentDescription = "Lọc")
                }
            }

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Banner
                if (banners.isNotEmpty()) {
                    item {
                        BannerSlider(banners = banners)
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Danh mục ngang
                if (categories.isNotEmpty()) {
                    item {
                        val categoriesWithAll = listOf("Tất cả") + categories

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            items(categoriesWithAll.size) { index ->
                                val category = categoriesWithAll[index]
                                val isSelected = selectedCategory == category ||
                                        (category == "Tất cả" && selectedCategory == null)

                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    modifier = Modifier
                                        .clickable {
                                            selectedCategory = if (category == "Tất cả") null else category
                                        }
                                ) {
                                    Text(
                                        text = category,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isSelected) Color.Black else Color.Gray
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (isSelected) {
                                        Box(
                                            modifier = Modifier
                                                .height(2.dp)
                                                .width(24.dp)
                                                .background(Color.Black, shape = RoundedCornerShape(1.dp))
                                        )
                                    } else {
                                        Spacer(modifier = Modifier.height(2.dp))
                                    }
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                    }
                }

                // Sản phẩm dạng lưới
                items(filteredProducts.chunked(2)) { rowItems ->
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        rowItems.forEach { product ->
                            Box(modifier = Modifier.weight(1f)) {
                                ProductItem(product = product) {
                                    val intent = Intent(context, ProductDetail::class.java)
                                    intent.putExtra("productId", product._id)
                                    context.startActivity(intent)
                                }
                            }
                        }
                        if (rowItems.size < 2) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
                item { Spacer(modifier = Modifier.height(16.dp)) }
            }
        }
    }

    if (showFilter) {
        AlertDialog(
            onDismissRequest = { showFilter = false },
            title = { Text("Bộ lọc") },
            text = {
                Column {
                    // --- Lọc theo danh mục ---
                    Text("Danh mục", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))

                    LazyVerticalGrid(
                        columns = GridCells.Fixed(3), // 3 ô mỗi hàng
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.heightIn(max = 200.dp)
                    ) {
                        // Chip "Tất cả"
                        item {
                            FilterChip(
                                selected = selectedCategories.isEmpty(),
                                onClick = { selectedCategories = emptySet() },
                                label = { Text("Tất cả") },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color.Black,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.Transparent,
                                    labelColor = Color.Black
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (selectedCategories.isEmpty()) Color.Black else Color.Gray
                                )
                            )
                        }

                        // Các chip danh mục khác
                        items(categories.size) { index ->
                            val category = categories[index]
                            val isSelected = selectedCategories.contains(category)

                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedCategories =
                                        if (isSelected) {
                                            emptySet() // bấm lần nữa -> bỏ chọn
                                        } else {
                                            setOf(category) // chọn mới
                                        }
                                },
                                label = { Text(category) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = Color.Black,
                                    selectedLabelColor = Color.White,
                                    containerColor = Color.Transparent,
                                    labelColor = Color.Black
                                ),
                                border = BorderStroke(
                                    1.dp,
                                    if (isSelected) Color.Black else Color.Gray
                                )
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // --- Lọc theo giá ---
                    Text("Sắp xếp theo giá", fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row {
                        val isLowSelected = priceSort == "lowToHigh"
                        val isHighSelected = priceSort == "highToLow"

                        FilterChip(
                            selected = isLowSelected,
                            onClick = {
                                priceSort = if (isLowSelected) "" else "lowToHigh"
                            },
                            label = { Text("Thấp → Cao") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.Black,
                                selectedLabelColor = Color.White,
                                containerColor = Color.Transparent,
                                labelColor = Color.Black
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isLowSelected) Color.Black else Color.Gray
                            )
                        )
                        Spacer(Modifier.width(8.dp))
                        FilterChip(
                            selected = isHighSelected,
                            onClick = {
                                priceSort = if (isHighSelected) "" else "highToLow"
                            },
                            label = { Text("Cao → Thấp") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Color.Black,
                                selectedLabelColor = Color.White,
                                containerColor = Color.Transparent,
                                labelColor = Color.Black
                            ),
                            border = BorderStroke(
                                1.dp,
                                if (isHighSelected) Color.Black else Color.Gray
                            )
                        )
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFilter = false }) {
                    Text("Áp dụng")
                }
            },
            dismissButton = {
                TextButton(onClick = { showFilter = false }) {
                    Text("Hủy")
                }
            }
        )
    }

}


@Composable
fun BannerSlider(banners: List<Banner>) {
    if (banners.isEmpty()) return

    val pagerState = rememberPagerState(initialPage = 0)
    val scope = rememberCoroutineScope()

    // Auto slide
    LaunchedEffect(pagerState.currentPage, banners.size) {
        delay(3000)
        val next = (pagerState.currentPage + 1) % banners.size
        scope.launch {
            pagerState.scrollToPage(next)
        }
    }

    val context = LocalContext.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color.LightGray)
    ) {
        HorizontalPager(
            count = banners.size,
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            val banner = banners[page]

            // Tạo ImageRequest giống ProductItem
            val imageRequest = remember(banner.image) {
                if (banner.image.startsWith("data:image")) {
                    val pureBase64 = banner.image.substringAfter("base64,")
                    val decodedBytes =
                        android.util.Base64.decode(pureBase64, android.util.Base64.DEFAULT)
                    coil.request.ImageRequest.Builder(context)
                        .data(decodedBytes)
                        .crossfade(true)
                        .build()
                } else {
                    coil.request.ImageRequest.Builder(context)
                        .data("http://192.168.1.13:5000${banner.image}")
                        .crossfade(true)
                        .build()
                }
            }

            AsyncImage(
                model = imageRequest,
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(id = R.drawable.logo),
                error = painterResource(id = R.drawable.logo)
            )
        }

        // Indicator
        Row(
            horizontalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(6.dp)
                .background(Color(0x55000000), shape = RoundedCornerShape(50))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            repeat(banners.size) { index ->
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(
                            if (pagerState.currentPage == index) Color.White else Color.Black
                        )
                )
            }
        }
    }
}

@Composable
fun ProductItem(
    product: Product,
    onClick: () -> Unit
) {
    val context = LocalContext.current

    val imageRequest = remember(product.image) {
        product.image?.let { base64String ->
            if (base64String.startsWith("data:image")) {
                val pureBase64 = base64String.substringAfter("base64,")
                val decodedBytes = android.util.Base64.decode(pureBase64, android.util.Base64.DEFAULT)
                coil.request.ImageRequest.Builder(context)
                    .data(decodedBytes)
                    .crossfade(true)
                    .build()
            } else null
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF5F5F5))
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
            if (imageRequest != null) {
                AsyncImage(
                    model = imageRequest,
                    contentDescription = product.name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    placeholder = painterResource(id = R.drawable.logo),
                    error = painterResource(id = R.drawable.logo)
                )
            } else {
                Image(
                    painter = painterResource(id = R.drawable.logo),
                    contentDescription = "Placeholder",
                    modifier = Modifier.fillMaxSize()
                )
            }
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


