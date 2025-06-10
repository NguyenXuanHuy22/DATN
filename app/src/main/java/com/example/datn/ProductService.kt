package com.example.datn

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ProductService {
    @GET("products")
    suspend fun getListProducts(): Response<List<Product>>

    @GET("users")
    suspend fun getUsers(): List<User>

    @POST("users")
    suspend fun createUser(@Body user: User): Response<User>

    @GET("carts")
    suspend fun getCarts(): List<Cart>

    @GET("wishlists")
    suspend fun getWishlists(): List<Wishlist>

    @GET("orders")
    suspend fun getOrders(): List<Order>

    @POST("users")
    suspend fun registerUser(@Body user: User): User

    @POST("carts")
    suspend fun createCart(@Body cart: Cart): Cart

    @POST("wishlists")
    suspend fun createWishlist(@Body wishlist: Wishlist): Wishlist

    @POST("orders")
    suspend fun createOrder(@Body order: Order): Order

    @PUT("carts/{id}")
    suspend fun updateCart(@Path("id") id: String, @Body cart: Cart): Cart

    @PUT("wishlists/{id}")
    suspend fun updateWishlist(@Path("id") id: String, @Body wishlist: Wishlist): Wishlist

    @PUT("orders/{id}")
    suspend fun updateOrder(@Path("id") id: String, @Body order: Order): Order

    @GET("products/{id}")
    suspend fun getProductDetail(@Path("id") id: String): Response<Product>

}