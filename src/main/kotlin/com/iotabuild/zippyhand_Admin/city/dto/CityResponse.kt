package com.iotabuild.zippyhand_Admin.city.dto

data class CityResponse(
    val id: Long,
    val name: String,
    val state: String,
    val pincode: String,
    val symbol: String,
    val isActive: Boolean
)
