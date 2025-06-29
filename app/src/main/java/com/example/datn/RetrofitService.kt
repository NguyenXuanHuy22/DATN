package com.example.datn

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.56.103:3000/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ProductService by lazy {
        retrofit.create(ProductService::class.java)
    }
}