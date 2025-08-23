package com.example.datn

data class ChangePasswordRequest (
    val oldPassword: String,
    val newPassword: String
)