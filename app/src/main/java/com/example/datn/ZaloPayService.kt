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

    // ✅ THÊM: Method query status (POST /query với body {app_trans_id})
    @POST("api/payments/zalopay/query")
    suspend fun queryStatus(
        @Body request: ZlpQueryRequest // Giả sử data class: data class ZlpQueryRequest(val app_trans_id: String)
    ): Response<ZlpQueryResponse> // Giả sử response có return_code, return_message

    // Giữ /return nếu cần, nhưng không dùng cho query
    @GET("api/payments/zalopay/return")
    suspend fun checkOrderStatus(
        @Query("apptransid") appTransId: String
    ): Response<ReturnOrderResponse>
}
