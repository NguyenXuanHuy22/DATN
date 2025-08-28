package com.example.datn

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

class ZaloPayWebViewActivity : Activity() {
    companion object {
        const val EXTRA_PAYMENT_URL = "payment_url"
        const val EXTRA_ORDER_ID = "order_id"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val webView = WebView(this)
        setContentView(webView)

        val url = intent.getStringExtra(EXTRA_PAYMENT_URL)
        if (url != null) {
            webView.settings.javaScriptEnabled = true
            webView.settings.domStorageEnabled = true
            webView.webViewClient = object : WebViewClient() {
                override fun onPageFinished(view: WebView?, currentUrl: String?) {
                    super.onPageFinished(view, currentUrl)
                    Log.d("ZaloPayWebView", "Redirect URL: $currentUrl")

                    currentUrl?.let { urlStr ->
                        try {
                            val uri = Uri.parse(urlStr)
                            val queryParams = uri.queryParameterNames
                            Log.d("ZaloPayWebView", "Query params: $queryParams")

                            if (queryParams.isNotEmpty()) {
                                // Lấy status từ query params
                                val statusParam = uri.getQueryParameter("status")
                                    ?: uri.getQueryParameter("return_code")
                                    ?: uri.getQueryParameter("returnMessage")
                                    ?: uri.getQueryParameter("return_message")

                                val responseCode = uri.getQueryParameter("response_code")
                                    ?: uri.getQueryParameter("sub_return_code")
                                    ?: uri.getQueryParameter("return_code")
                                    ?: "unknown"

                                Log.d("ZaloPayWebView", "statusParam=$statusParam, responseCode=$responseCode")

                                val isSuccess = statusParam != null && (
                                        statusParam == "1" ||
                                                statusParam.equals("success", ignoreCase = true) ||
                                                statusParam == "SUCCESS"
                                        )

                                val resultIntent = Intent().apply {
                                    putExtra("status", if (isSuccess) "success" else "failed")
                                    putExtra("response_code", responseCode)
                                }
                                setResult(RESULT_OK, resultIntent)
                                finish()
                            } else {
                                // Trường hợp trả về HTML từ /return endpoint
                                Log.w("ZaloPayWebView", "No query params, checking HTML content")
                                webView.evaluateJavascript(
                                    "(function() { return document.body.innerText; })();"
                                ) { result ->
                                    Log.d("ZaloPayWebView", "HTML content: $result")
                                    val resultIntent = Intent()
                                    val isSuccess = result?.contains("Đã xác nhận", true) == true
                                    val responseCode = if (isSuccess) "1" else "unknown"

                                    resultIntent.apply {
                                        putExtra("status", if (isSuccess) "success" else "failed")
                                        putExtra("response_code", responseCode)
                                    }
                                    setResult(RESULT_OK, resultIntent)
                                    finish()
                                }
                            }
                        } catch (e: Exception) {
                            Log.e("ZaloPayWebView", "Error parsing URL: ${e.message}")
                            val resultIntent = Intent().apply {
                                putExtra("status", "failed")
                                putExtra("response_code", "parse_error")
                            }
                            setResult(RESULT_OK, resultIntent)
                            finish()
                        }
                    } ?: run {
                        Log.e("ZaloPayWebView", "Redirect URL is null")
                        Toast.makeText(this@ZaloPayWebViewActivity, "Lỗi: URL redirect null", Toast.LENGTH_LONG).show()
                        val resultIntent = Intent().apply {
                            putExtra("status", "failed")
                            putExtra("response_code", "url_null")
                        }
                        setResult(RESULT_OK, resultIntent)
                        finish()
                    }
                }
            }
            Log.d("ZaloPayWebView", "Loading URL: $url")
            webView.loadUrl(url)
        } else {
            Log.e("ZaloPayWebView", "No payment URL provided")
            Toast.makeText(this, "Không có URL thanh toán", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}

