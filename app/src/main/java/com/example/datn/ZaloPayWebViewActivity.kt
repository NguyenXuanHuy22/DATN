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
    
    // ✅ Biến để track xem đã có kết quả chưa
    private var hasResult = false
    private var webView: WebView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        webView = WebView(this)
        setContentView(webView)

        val url = intent.getStringExtra(EXTRA_PAYMENT_URL)
        val savedTransId = intent.getStringExtra(EXTRA_APP_TRANS_ID) // ✅ Lấy từ extra
        if (url == null) {
            Log.e("ZaloPayWebView", "No payment URL provided")
            Toast.makeText(this, "Không có URL thanh toán", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        webView?.settings?.javaScriptEnabled = true
        webView?.settings?.domStorageEnabled = true

        webView?.webViewClient = object : WebViewClient() {

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

                // Kiểm tra URL hiện tại có phải success URL không
                if (currentUrl != null && isPaymentSuccessUrl(currentUrl)) {
                    Log.d("ZaloPayWebView", "Current URL indicates success")
                    val parsedTransId = try {
                        val uri = Uri.parse(currentUrl)
                        uri.getQueryParameter("apptransid")
                    } catch (e: Exception) { null }
                    setResultAndFinish(success = true, transId = parsedTransId ?: savedTransId)
                    return
                }

                // ✅ Kiểm tra nội dung HTML để phát hiện success
                view?.evaluateJavascript("(function() { return document.body.innerText; })();") { html ->
                    try {
                        Log.d("ZaloPayWebView", "Page content: $html")
                        
                        if (html != null) {
                            // ✅ Chỉ phát hiện success khi có dấu hiệu rõ ràng
                            val successKeywords = listOf(
                                "đã xác nhận", "thanh toán thành công", "payment successful", 
                                "xác nhận thanh toán", "đã thanh toán", "payment completed",
                                "thanh toán hoàn tất", "payment success", "giao dịch thành công"
                            )
                            
                            val hasSuccessKeyword = successKeywords.any { keyword ->
                                html.contains(keyword, ignoreCase = true)
                            }
                            
                            if (hasSuccessKeyword) {
                                Log.d("ZaloPayWebView", "HTML contains success message: $html - showing success")
                                setResultAndFinish(success = true, transId = savedTransId)
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("ZaloPayWebView", "evaluateJavascript error: ${e.message}")
                    }
                }
            }
        }

        Log.d("ZaloPayWebView", "Loading URL: $url")
        webView?.loadUrl(url)
    }

    // ✅ Xử lý khi user nhấn back
    override fun onBackPressed() {
        Log.d("ZaloPayWebView", "User pressed back - checking payment status first")
        val savedTransId = intent.getStringExtra(EXTRA_APP_TRANS_ID)
        
        // ✅ Kiểm tra URL hiện tại trước khi trả về kết quả
        webView?.url?.let { currentUrl ->
            Log.d("ZaloPayWebView", "Current URL when back pressed: $currentUrl")
            if (isPaymentSuccessUrl(currentUrl)) {
                Log.d("ZaloPayWebView", "Payment was successful - returning success")
                setResultAndFinish(success = true, transId = savedTransId)
                return
            }
        }
        
        // ✅ Nếu không phải success URL, kiểm tra xem có phải user đã thanh toán thành công nhưng chưa redirect không
        if (savedTransId != null) {
            Log.d("ZaloPayWebView", "Checking payment status with server for transId: $savedTransId")
            // Có thể thêm logic query server ở đây nếu cần
            // Nhưng tạm thời trả về failed để tránh false positive
        }
        
        // ✅ Nếu không phải success URL thì mới trả về failed
        Log.d("ZaloPayWebView", "Payment not successful - returning failed")
        setResultAndFinish(success = false, transId = savedTransId)
    }
    
    // ✅ Xử lý khi activity bị destroy
    override fun onDestroy() {
        super.onDestroy()
        // Kiểm tra nếu chưa có kết quả thì kiểm tra trạng thái thanh toán
        if (!hasResult) {
            Log.d("ZaloPayWebView", "Activity destroyed without result - checking payment status")
            val savedTransId = intent.getStringExtra(EXTRA_APP_TRANS_ID)
            
            // ✅ Kiểm tra URL hiện tại trước khi trả về kết quả
            webView?.url?.let { currentUrl ->
                Log.d("ZaloPayWebView", "Current URL when destroyed: $currentUrl")
                if (isPaymentSuccessUrl(currentUrl)) {
                    Log.d("ZaloPayWebView", "Payment was successful - returning success")
                    setResultAndFinish(success = true, transId = savedTransId)
                    return
                }
            }
            
            // ✅ Nếu không phải success URL thì mới trả về failed
            Log.d("ZaloPayWebView", "Payment not successful - returning failed")
            setResultAndFinish(success = false, transId = savedTransId)
        }
    }

    private fun isPaymentSuccessUrl(urlStr: String): Boolean {
        return try {
            val uri = Uri.parse(urlStr)
            val status = uri.getQueryParameter("status")
            val returnCode = uri.getQueryParameter("return_code")
            val returncode = uri.getQueryParameter("returncode") // ✅ Thêm returncode (không có dấu gạch dưới)
            val subReturnCode = uri.getQueryParameter("sub_return_code")
            
            // ✅ Debug: Log tất cả query parameters
            val queryNames = uri.queryParameterNames
            Log.d("ZaloPayWebView", "🔍 All query parameter names: $queryNames")
            queryNames.forEach { paramName ->
                val paramValue = uri.getQueryParameter(paramName)
                Log.d("ZaloPayWebView", "🔍 Parameter: $paramName = $paramValue")
            }
            
            Log.d("ZaloPayWebView", "🔍 Checking URL: $urlStr")
            Log.d("ZaloPayWebView", "🔍 Parsed params - status: $status, returnCode: $returnCode, returncode: $returncode, subReturnCode: $subReturnCode")
            
            // ✅ Chỉ phát hiện success khi có dấu hiệu rõ ràng
            val isSuccess = (status != null && status.equals("success", ignoreCase = true))
                    || (status != null && status == "1") // ✅ Thêm kiểm tra status=1
                    || returnCode == "1"
                    || returncode == "1" // ✅ Thêm kiểm tra returncode=1
                    || subReturnCode == "1"
                    || urlStr.contains("return_code=1") // Direct success code
                    || urlStr.contains("returncode=1") // ✅ Thêm kiểm tra returncode=1 trong URL
                    || urlStr.contains("status=success") // Direct success status
                    || urlStr.contains("status=1") // ✅ Thêm kiểm tra status=1 trong URL
                    || urlStr.contains("myapp://payment/result?status=success") // New redirect URL from backend
                    || urlStr.contains("Giao%20d%E1%BB%8Bch%20th%C3%A0nh%20c%C3%B4ng") // ✅ Thêm kiểm tra message thành công
                    || urlStr.contains("Giao dịch thành công") // ✅ Thêm kiểm tra message thành công (không encode)
                    || urlStr.contains("&returncode=1") // ✅ Thêm kiểm tra returncode=1 với dấu &
                    || urlStr.contains("?returncode=1") // ✅ Thêm kiểm tra returncode=1 với dấu ?
                    || urlStr.contains("&status=1") // ✅ Thêm kiểm tra status=1 với dấu &
                    || urlStr.contains("?status=1") // ✅ Thêm kiểm tra status=1 với dấu ?
            
            Log.d("ZaloPayWebView", "🔍 URL check result: $isSuccess")
            if (isSuccess) {
                Log.d("ZaloPayWebView", "✅ SUCCESS URL DETECTED!")
            } else {
                Log.d("ZaloPayWebView", "❌ Not a success URL")
            }
            isSuccess
        } catch (e: Exception) {
            Log.e("ZaloPayWebView", "❌ Error parsing URL: ${e.message}")
            false
        }
    }

    private fun setResultAndFinish(success: Boolean, transId: String? = null) { // ✅ Thêm transId param
        hasResult = true // ✅ Đánh dấu đã có kết quả
        
        val resultIntent = Intent().apply {
            putExtra("status", if (success) "success" else "failed")
            putExtra("response_code", if (success) "1" else "0")
            if (transId != null) putExtra("appTransId", transId) // ✅ Trả về appTransId
        }
        
        Log.d("ZaloPayWebView", "setResultAndFinish - success: $success, transId: $transId")
        Log.d("ZaloPayWebView", "Returning status: ${if (success) "success" else "failed"}")
        Log.d("ZaloPayWebView", "Returning response_code: ${if (success) "1" else "0"}")
        
        // ✅ Nếu user hủy hoặc có lỗi, trả về failed để cập nhật status "ĐÃ HỦY"
        if (!success) {
            Log.d("ZaloPayWebView", "Payment failed - order will be updated to 'ĐÃ HỦY'")
        } else {
            Log.d("ZaloPayWebView", "Payment success - order remains 'CHỜ XÁC NHẬN'")
        }
        
        setResult(RESULT_OK, resultIntent)
        finish()
    }

}
