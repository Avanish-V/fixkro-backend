package com.iotabuild.zippyhand_Admin.city.dto

data class CityRequest(
    val name: String,
    val state: String,
    val pincode: String,
    val symbol: String = "🏙️",
    val isActive: Boolean = true
)
