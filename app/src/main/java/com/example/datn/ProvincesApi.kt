package com.example.datn

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface ProvincesApi {
    // depth: 1 provinces only, 2 include districts, 3 include wards
    @GET("api/v1/")
    suspend fun getProvinces(@Query("depth") depth: Int = 1): Response<List<Province>>

    @GET("api/v1/p/{code}")
    suspend fun getProvince(@Path("code") code: Int, @Query("depth") depth: Int = 2): Response<Province>

    @GET("api/v1/d/{code}")
    suspend fun getDistrict(@Path("code") code: Int, @Query("depth") depth: Int = 2): Response<District>

    @GET("api/v1/w/{code}")
    suspend fun getWard(@Path("code") code: Int): Response<Ward>
}


