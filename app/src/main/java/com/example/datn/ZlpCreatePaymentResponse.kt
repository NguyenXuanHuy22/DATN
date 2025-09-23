package com.example.datn

import com.google.gson.annotations.SerializedName

data class ZlpCreatePaymentResponse(
    @SerializedName("return_code")
    val returnCode: Int? = null,
    @SerializedName("return_message")
    val returnMessage: String? = null,
    @SerializedName("sub_return_code")
    val subReturnCode: Int? = null,
    @SerializedName("sub_return_message")
    val subReturnMessage: String? = null,
    @SerializedName("zp_trans_token")
    val zpTransToken: String? = null,
    @SerializedName("order_url")
    val orderUrl: String? = null,
    @SerializedName("order_token")
    val orderToken: String? = null
)

data class ZlpBackendCreateResponse(
    val message: String? = null,
    val orderId: String? = null,
    @SerializedName("app_trans_id")
    val appTransId: String? = null,
    val amount: Long? = null,
    val paymentUrl: String? = null,
    val zpTransToken: String? = null,
    @SerializedName("rawZalo")
    val rawZalo: ZlpCreatePaymentResponse? = null
)

data class ReturnOrderResponse(
    val status: String, // "success" hoặc "failed"
    val orderId: String?,
    @SerializedName("app_trans_id")
    val appTransId: String?,
    val total: Long?,
    @SerializedName("order_status")
    val orderStatus: String?
)

data class ZlpQueryRequest(
    @SerializedName("app_trans_id")
    val appTransId: String
)

data class ZlpQueryResponse(
    @SerializedName("return_code")
    val returnCode: Int? = null,
    @SerializedName("return_message")
    val returnMessage: String? = null,
    @SerializedName("sub_return_code")
    val subReturnCode: Int? = null,
    @SerializedName("sub_return_message")
    val subReturnMessage: String? = null,
    @SerializedName("app_trans_id")
    val appTransId: String? = null,
    @SerializedName("amount")
    val amount: Long? = null,
    @SerializedName("zp_trans_id")
    val zpTransId: String? = null,
    @SerializedName("status")
    val status: Int? = null, // 1: success, 0: fail, theo docs ZaloPay
    @SerializedName("order_status")
    val orderStatus: String? = null, // ✅ Thêm field order_status từ backend
    @SerializedName("orderId")
    val orderId: String? = null // ✅ Thêm field orderId từ backend
)

// ✅ Thêm data class cho cancel order request
data class ZlpCancelOrderRequest(
    @SerializedName("app_trans_id")
    val appTransId: String
)

// ✅ Thêm data class cho cancel order response
data class ZlpCancelOrderResponse(
    @SerializedName("return_code")
    val returnCode: Int? = null,
    @SerializedName("return_message")
    val returnMessage: String? = null,
    @SerializedName("order_status")
    val orderStatus: String? = null
)