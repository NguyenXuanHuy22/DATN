package com.example.datn

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ProductService {
    @GET("products")
    suspend fun getListProducts(): Response<List<ProductData>>

    @GET("products/{id}")
    suspend fun getProduct(@Path("id") id: String): Response<ProductData>

    @GET("users")
    suspend fun getUsers(): Response<List<User>>

    @POST("users")
    suspend fun createUser(@Body user: User): Response<User>

    @GET("carts/{userId}")
    suspend fun getCart(@Path("userId") userId: String): Response<Cart?>

    @GET("carts")
    suspend fun getCarts(): Response<List<Cart>>

    @POST("carts")
    suspend fun createCart(@Body cart: Cart): Response<Cart>

    @PUT("carts/{id}")
    suspend fun updateCart(@Path("id") id: String, @Body cart: Cart): Response<Cart>

    @GET("wishlists")
    suspend fun getWishlists(): Response<List<Wishlist>>

    @POST("wishlists")
    suspend fun createWishlist(@Body wishlist: Wishlist): Response<Wishlist>

    @PUT("wishlists/{id}")
    suspend fun updateWishlist(@Path("id") id: String, @Body wishlist: Wishlist): Response<Wishlist>

    @GET("orders")
    suspend fun getOrders(): Response<List<Order>>

    @POST("orders")
    suspend fun createOrder(@Body order: Order): Response<Order>

    @PUT("orders/{id}")
    suspend fun updateOrder(@Path("id") id: String, @Body order: Order): Response<Order>


}