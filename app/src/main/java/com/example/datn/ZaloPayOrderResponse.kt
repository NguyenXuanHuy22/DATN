package com.example.datn

data class ZaloPayOrderResponse(
    val return_code: Int,
    val return_message: String?,
    val order_url: String?,       // URL thanh toán
    val zp_trans_token: String?,  // token giao dịch
    val app_trans_id: String?     // mã giao dịch app
)
