package com.example.datn

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ProductService {
    // ===== Product =====
    @GET("api/products")
    suspend fun getListProducts(): Response<List<ProductData>>

    @GET("api/products/{id}")
    suspend fun getProduct(@Path("id") id: String): Response<ProductData>

    // ===== User =====
    @GET("api/users")
    suspend fun getUsers(): Response<List<User>>

    @PUT("api/users/{id}")
    suspend fun updateUser(
        @Path("id") id: String,
        @Body user: User
    ): Response<Unit>

    @GET("api/users/{id}")
    suspend fun getUserById(@Path("id") id: String): User

    @POST("/api/users/register")
    suspend fun registerUser(@Body user: User): Response<RegisterResponse>

    // ===== Order =====
    @GET("api/orders")
    suspend fun getOrders(): Response<List<Order>>

    @POST("api/orders")
    suspend fun createOrder(@Body order: Order): Response<Order>

    @PUT("api/orders/{id}")
    suspend fun updateOrder(@Path("id") id: String, @Body order: Order): Response<Order>

    // ✅ Sửa đúng path có "api/"
    @GET("api/orders/user/{userId}")
    suspend fun getOrdersByUser(@Path("userId") userId: String): Response<List<Order>>



    // ===== Cart =====
    @DELETE("api/cart-items/{itemId}")
    suspend fun deleteCartItemById(
        @Path("itemId") itemId: String
    ): Response<Unit>

    // ===== Wishlist =====
    @GET("api/wishlists/user/{userId}")
    suspend fun getWishlistByUserId(@Path("userId") userId: String): Response<Wishlist>

    @POST("api/wishlists/toggle")
    suspend fun toggleWishlist(@Body request: ToggleWishlistRequest): Response<Wishlist>

    @DELETE("api/wishlists/{userId}/items/{productId}")
    suspend fun removeWishlistItem(
        @Path("userId") userId: String,
        @Path("productId") productId: String
    ): Response<Unit>
}
