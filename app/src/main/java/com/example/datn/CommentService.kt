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

    @PUT("api/orders/{id}/review/{productId}")
    suspend fun markProductAsReviewed(
        @Path("id") orderId: String,
        @Path("productId") productId: String
    ): Response<Order>
}