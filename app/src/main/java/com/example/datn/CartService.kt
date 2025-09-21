package com.example.datn

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface CartService {

    @GET("api/carts/user/{userId}")
    suspend fun getCartByUserId(
        @Path("userId") userId: String
    ): Response<CartResponse>

    @PATCH("api/carts/{cartId}")
    suspend fun updateCart(
        @Path("cartId") cartId: String,
        @Body updatedCart: CartResponse
    ): CartResponse

    @POST("api/carts")
    suspend fun createCart(
        @Body newCart: CartCreateRequest
    ): CartResponse

    @POST("api/carts/{cartId}/items")
    suspend fun addItemToCart(
        @Path("cartId") cartId: String,
        @Body newItem: CartItemDto
    ): CartResponse

    // ✅ Xoá 1 sản phẩm khỏi giỏ theo itemId
    @DELETE("api/carts/{cartId}/items/{itemId}")
    suspend fun deleteItemFromCart(
        @Path("cartId") cartId: String,
        @Path("itemId") itemId: String
    ): CartResponse


}

