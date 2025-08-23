package com.example.datn

import retrofit2.Response
import retrofit2.http.*

interface ZaloPayService {
    @POST("api/zalopay/create-order")
    suspend fun createOrder(
        @Body request: ZaloPayOrderRequest
    ): Response<OrderWrapperResponse>   // ✅ đổi sang wrapper

    @GET("api/zalopay/query/{app_trans_id}")
    suspend fun queryOrder(
        @Path("app_trans_id") appTransId: String
    ): Response<ZaloPayOrderResponse>
}
