package com.example.datn

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("_id") val _id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("email") val email: String? = null,
    @SerializedName("phone") val phone: String? = null,
    @SerializedName("password") val password: String? = null,
    @SerializedName("address") val address: String? = null,
    @SerializedName("avatar") val avatar: String? = null,
    @SerializedName("role") val role: String? = "user"
)

