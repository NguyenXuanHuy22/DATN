package com.example.datn

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("_id") val _id: String? = null, // Cho phép null để backend tự tạo
    @SerializedName("name") val name: String,
    @SerializedName("email") val email: String,
    @SerializedName("phone") val phone: String,
    @SerializedName("password") val password: String,
    @SerializedName("address") val address: String,
    @SerializedName("avatar") val avatar: String,
    @SerializedName("role") val role: String = "user"
)
