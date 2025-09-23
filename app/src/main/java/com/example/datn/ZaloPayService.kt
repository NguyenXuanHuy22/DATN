package com.example.datn

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface ZaloPayService {
    @POST("api/payments/zalopay/create")
    suspend fun createZalopayPayment(
        @Body request: ZlpCreatePaymentRequest
    ): Response<ZlpBackendCreateResponse>

    // ✅ Query status (POST /query với body {app_trans_id})
    @POST("api/payments/zalopay/query")
    suspend fun queryStatus(
        @Body request: ZlpQueryRequest
    ): Response<ZlpQueryResponse>

    // ✅ Return endpoint (GET /return) - để check status từ redirect
    @GET("api/payments/zalopay/return")
    suspend fun checkOrderStatus(
        @Query("apptransid") appTransId: String,
        @Query("status") status: String? = null,
        @Query("return_code") returnCode: String? = null
    ): Response<ReturnOrderResponse>
    
    // ✅ Cancel order endpoint - để hủy đơn hàng khi thanh toán thất bại
    @POST("api/payments/zalopay/cancel")
    suspend fun cancelOrder(
        @Body request: ZlpCancelOrderRequest
    ): Response<ZlpCancelOrderResponse>
}
