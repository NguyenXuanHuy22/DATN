package com.example.datn

import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.PartMap
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

    @GET("api/users/{id}")
    suspend fun getUserById(@Path("id") id: String): User

    @POST("api/users/register")
    suspend fun registerUser(@Body user: User): Response<RegisterResponse>

    @POST("api/users/{id}/change-password")
    suspend fun changePassword(
        @Path("id") id: String,
        @Body request: ChangePasswordRequest
    ): Response<ChangePasswordResponse>

    @PUT("api/users/{id}")
    suspend fun updateUserJson(
        @Path("id") id: String,
        @Body body: UpdateUserRequest
    ): Response<User>

    // ===== Order =====
    @POST("api/orders")
    suspend fun createOrder(@Body order: CreateOrderRequest): Response<Order>

    @GET("api/orders/user/{userId}")
    suspend fun getOrdersByUser(@Path("userId") userId: String): Response<List<Order>>

    @GET("api/orders/{id}/detail")
    suspend fun getOrderDetail(@Path("id") orderId: String): Response<Order>

    // Gửi PATCH request kèm note huỷ
    @PATCH("api/orders/{id}/cancel")
    suspend fun cancelOrder(
        @Path("id") orderId: String,
        @Body request: CancelOrderRequest
    ): Response<CancelOrderResponse>



    // ===== Cart =====
    @DELETE("api/cart-items/{itemId}")
    suspend fun deleteCartItemById(@Path("itemId") itemId: String): Response<Unit>

    // ===== Wishlist =====
    @GET("api/wishlists/user/{userId}")
    suspend fun getWishlistByUserId(@Path("userId") userId: String): Response<Wishlist>

    @POST("api/wishlists/toggle")
    suspend fun toggleWishlist(@Body request: ToggleWishlistRequest): Response<Wishlist>

    // ===== Banner =====
    @GET("api/banners")
    suspend fun getBanners(): List<Banner>
}

