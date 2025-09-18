package com.example.datn // chỉnh theo package project của bạn nếu khác

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast

@SuppressLint("SetJavaScriptEnabled")
class ZaloPayWebViewActivity : Activity() {

    companion object {
        const val EXTRA_PAYMENT_URL = "payment_url"
        const val EXTRA_APP_TRANS_ID = "app_trans_id" // ✅ Thêm constant
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val webView = WebView(this)
        setContentView(webView)

        val url = intent.getStringExtra(EXTRA_PAYMENT_URL)
        val savedTransId = intent.getStringExtra(EXTRA_APP_TRANS_ID) // ✅ Lấy từ extra
        if (url == null) {
            Log.e("ZaloPayWebView", "No payment URL provided")
            Toast.makeText(this, "Không có URL thanh toán", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        webView.settings.javaScriptEnabled = true
        webView.settings.domStorageEnabled = true

        webView.webViewClient = object : WebViewClient() {

            override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                val redirectUrl = url ?: return false
                Log.d("ZaloPayWebView", "shouldOverrideUrlLoading(url): $redirectUrl")

                // ✅ FIX: Parse apptransid từ URL redirect
                val parsedTransId = try {
                    val uri = Uri.parse(redirectUrl)
                    uri.getQueryParameter("apptransid")
                } catch (e: Exception) { null }

                if (isPaymentSuccessUrl(redirectUrl)) {
                    setResultAndFinish(success = true, transId = parsedTransId ?: savedTransId)
                    return true
                }
                return false
            }

            override fun onPageFinished(view: WebView?, currentUrl: String?) {
                super.onPageFinished(view, currentUrl)
                Log.d("ZaloPayWebView", "Page finished: $currentUrl")

                // Fallback: kiểm tra nội dung HTML
                view?.evaluateJavascript("(function() { return document.body.innerText; })();") { html ->
                    try {
                        if (html != null &&
                            (html.contains("Đã xác nhận", ignoreCase = true) ||
                                    html.contains("thanh toán thành công", ignoreCase = true))
                        ) {
                            Log.d("ZaloPayWebView", "HTML contains success message")
                            setResultAndFinish(success = true, transId = savedTransId)
                        }
                    } catch (e: Exception) {
                        Log.e("ZaloPayWebView", "evaluateJavascript error: ${e.message}")
                    }
                }
            }
        }

        Log.d("ZaloPayWebView", "Loading URL: $url")
        webView.loadUrl(url)
    }

    private fun isPaymentSuccessUrl(urlStr: String): Boolean {
        return try {
            val uri = Uri.parse(urlStr)
            val status = uri.getQueryParameter("status")
            val returnCode = uri.getQueryParameter("return_code")
            val subReturnCode = uri.getQueryParameter("sub_return_code")
            (status != null && status.equals("success", ignoreCase = true))
                    || returnCode == "1"
                    || subReturnCode == "1"
        } catch (e: Exception) {
            false
        }
    }

    private fun setResultAndFinish(success: Boolean, transId: String? = null) { // ✅ Thêm transId param
        val resultIntent = Intent().apply {
            putExtra("status", if (success) "success" else "failed")
            putExtra("response_code", if (success) "1" else "0")
            if (transId != null) putExtra("appTransId", transId) // ✅ Trả về appTransId
        }
        setResult(RESULT_OK, resultIntent)
        finish()
    }
}
