package com.example.datn

import com.example.datn.Address
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface AddressService {
    @GET("api/addresses/{userId}")
    suspend fun getAddresses(@Path("userId") userId: String): Response<List<Address>>

    @GET("api/addresses/default/{userId}")
    suspend fun getDefaultAddress(@Path("userId") userId: String): Response<Address>
}