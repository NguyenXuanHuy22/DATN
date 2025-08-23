package com.example.datn

data class OrderWrapperResponse(
    val order: Order?,             // Thông tin đơn hàng trong DB
    val zalopay: ZaloPayOrderResponse? // Thông tin từ ZaloPay
)
