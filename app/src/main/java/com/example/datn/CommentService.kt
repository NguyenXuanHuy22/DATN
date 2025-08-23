package com.example.datn

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CommentService {
    @POST("api/comments")
    suspend fun addComment(@Body comment: Comment): Response<Comment>

    @GET("api/comments/{productId}")
    suspend fun getComments(@Path("productId") productId: String): Response<List<Comment>>

    // Đánh dấu 1 sản phẩm trong đơn hàng đã được đánh giá
    @PUT("api/orders/{id}/review/{productId}")
    suspend fun markProductAsReviewed(
        @Path("id") orderId: String,
        @Path("productId") productId: String
    ): Response<Order>

    // 🔥 Thêm hàm đánh dấu toàn bộ đơn hàng đã được đánh giá
    @PUT("api/orders/{id}/review")
    suspend fun markOrderAsReviewed(
        @Path("id") orderId: String
    ): Response<Order>
}
