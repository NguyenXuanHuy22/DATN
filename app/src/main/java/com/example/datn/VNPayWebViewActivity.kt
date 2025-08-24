package com.example.datn


import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.webkit.*
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.viewinterop.AndroidView
import com.example.datn.ui.theme.DATNTheme

class VNPayWebViewActivity : ComponentActivity() {

    companion object {
        const val BASE_URL = "http://192.168.1.13:5000/" // đổi thành IP LAN khi test trên device
        const val RETURN_URL = "$BASE_URL/api/orders/vnpay_return"

        const val EXTRA_PAYMENT_URL = "payment_url"
        const val EXTRA_ORDER_ID = "order_id"
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val paymentUrl = intent.getStringExtra(EXTRA_PAYMENT_URL)

        if (paymentUrl.isNullOrBlank()) {
            Toast.makeText(this, "Không có URL thanh toán", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setContent {
            DATNTheme {
                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true

                            webViewClient = object : WebViewClient() {
                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    request: WebResourceRequest?
                                ): Boolean {
                                    val url = request?.url?.toString() ?: return false
                                    return handleNavigation(url)
                                }

                                override fun shouldOverrideUrlLoading(
                                    view: WebView?,
                                    url: String?
                                ): Boolean {
                                    return url?.let { handleNavigation(it) } ?: false
                                }

                                private fun handleNavigation(url: String): Boolean {
                                    if (url.startsWith(RETURN_URL, ignoreCase = true)) {
                                        val uri = Uri.parse(url)
                                        val rsp = uri.getQueryParameter("vnp_ResponseCode")
                                        val txnRef = uri.getQueryParameter("vnp_TxnRef")

                                        if (rsp == "00") {
                                            Toast.makeText(
                                                this@VNPayWebViewActivity,
                                                "Thanh toán thành công",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            setResult(
                                                RESULT_OK,
                                                Intent().apply {
                                                    putExtra("order_id", txnRef)
                                                    putExtra("status", "success")
                                                }
                                            )
                                        } else {
                                            Toast.makeText(
                                                this@VNPayWebViewActivity,
                                                "Thanh toán thất bại (mã $rsp)",
                                                Toast.LENGTH_LONG
                                            ).show()
                                            setResult(
                                                RESULT_CANCELED,
                                                Intent().apply {
                                                    putExtra("order_id", txnRef)
                                                    putExtra("status", "failed")
                                                    putExtra("response_code", rsp)
                                                }
                                            )
                                        }
                                        finish()
                                        return true
                                    }
                                    return false
                                }
                            }

                            loadUrl(paymentUrl)
                        }
                    },
                    update = { webView ->
                        // có thể update config nếu cần
                    }
                )
            }
        }
    }
}