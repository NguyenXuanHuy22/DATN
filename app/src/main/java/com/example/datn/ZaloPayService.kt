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

    @GET("api/payments/zalopay/return")
    suspend fun returnOrder(
        @Query("apptransid") appTransId: String
    ): Response<ReturnOrderResponse>
}