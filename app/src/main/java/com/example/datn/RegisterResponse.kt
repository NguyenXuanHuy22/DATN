package com.example.datn

import com.google.gson.annotations.SerializedName

data class RegisterResponse(
    @SerializedName("message") val message: String,
    @SerializedName("user") val user: User
)
