package com.example.datn

data class Province(
    val code: Int,
    val name: String,
    val codename: String?,
    val division_type: String?,
    val phone_code: Int?,
    val districts: List<District>?
)

data class District(
    val code: Int,
    val name: String,
    val codename: String?,
    val division_type: String?,
    val province_code: Int?,
    val wards: List<Ward>?
)

data class Ward(
    val code: Int,
    val name: String,
    val codename: String?,
    val division_type: String?,
    val district_code: Int?
)


