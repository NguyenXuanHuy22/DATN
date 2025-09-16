package com.example.datn

import com.google.gson.annotations.SerializedName

data class CancelOrderRequest(
    @SerializedName("note") val note: String
)
