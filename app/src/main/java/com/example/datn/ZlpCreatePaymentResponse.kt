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
    val app_trans_id: String? = null,
    val amount: Long? = null,
    val paymentUrl: String? = null,
    val zpTransToken: String? = null,
    val rawZalo: ZlpCreatePaymentResponse? = null
)

data class ReturnOrderResponse(
    val status: String, // "success" hoặc "failed"
    val orderId: String?,
    val app_trans_id: String?,
    val total: Long?,
    val order_status: String?
)