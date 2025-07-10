package com.example.datn

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface CartService {
    @GET("carts") // <-- sửa "cart" thành "carts"
    suspend fun getCartByUserId(@Query("userId") userId: String): List<CartResponse>

    @PATCH("carts/{cartId}")
    suspend fun updateCart(
        @Path("cartId") cartId: String,
        @Body updatedCart: CartResponse
    )

    @POST("carts")
    suspend fun createCart(@Body newCart: CartResponse)

}
