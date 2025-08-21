package com.example.datn

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    const val BASE_URL = "http://192.168.1.13:5000/"

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ProductService by lazy {
        retrofit.create(ProductService::class.java)
    }

    val cartService: CartService by lazy {
        retrofit.create(CartService::class.java)
    }
    val addressService: AddressService by lazy {
        retrofit.create(AddressService::class.java)
    }
    val commentService: CommentService = retrofit.create(CommentService::class.java)
}

