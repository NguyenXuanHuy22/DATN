package com.example.datn

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class ZaloPayWebViewActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val paymentUrl = intent.getStringExtra("payment_url") ?: run { finish(); return }
        val appTransId = intent.getStringExtra("app_trans_id") ?: ""

        setContent {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("Thanh toán ZaloPay") },
                        navigationIcon = {
                            IconButton(onClick = { finish() }) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                            }
                        }
                    )
                }
            ) { padding ->
                AndroidView(
                    modifier = Modifier.padding(padding),
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    url?.let {
                                        // Nếu redirect về payment-result
                                        if (it.contains("payment-result")) {
                                            // Gọi queryOrder để xác nhận
                                            lifecycleScope.launch {
                                                try {
                                                    val resp = RetrofitClient.zaloPayService.queryOrder(appTransId)
                                                    if (resp.isSuccessful && resp.body()?.return_code == 1) {
                                                        Toast.makeText(context, "Thanh toán thành công", Toast.LENGTH_SHORT).show()
                                                    } else {
                                                        Toast.makeText(context, "Thanh toán thất bại", Toast.LENGTH_SHORT).show()
                                                    }
                                                    finish()
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                    Toast.makeText(context, "Lỗi kiểm tra thanh toán", Toast.LENGTH_SHORT).show()
                                                    finish()
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            loadUrl(paymentUrl)
                        }
                    }
                )
            }
        }
    }
}

