package com.example.datn

import com.example.datn.Address
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface AddressService {
    //  Lấy danh sách địa chỉ của user
    @GET("api/addresses/{userId}")
    suspend fun getAddresses(
        @Path("userId") userId: String
    ): Response<List<Address>>

    //Lấy địa chỉ mặc định của user
    @GET("api/addresses/default/{userId}")
    suspend fun getDefaultAddress(
        @Path("userId") userId: String
    ): Response<Address>

    //  Thêm địa chỉ mới
    @POST("api/addresses")
    suspend fun addAddress(
        @Body address: Address
    ): Response<Address>

    //  Cập nhật địa chỉ
    @PUT("api/addresses/{id}")
    suspend fun updateAddress(
        @Path("id") id: String,
        @Body address: Address
    ): Response<Address>

    //  Xóa địa chỉ
    @DELETE("api/addresses/{id}")
    suspend fun deleteAddress(
        @Path("id") id: String
    ): Response<MessageResponse>
}