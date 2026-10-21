package com.iotabuild.zippyhand_Admin.servicearea.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern

data class ServiceAreaRequest(
    @field:NotBlank(message = "Pincode is required")
    @field:Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid 6-digit Indian pincode")
    val pincode: String,

    @field:NotBlank(message = "Locality/Area name is required")
    val locality: String,

    @field:NotBlank(message = "City name is required")
    val cityName: String,

    @field:NotBlank(message = "State name is required")
    val state: String,

    val cityId: Long? = null,
    val isActive: Boolean = true,
    val deliveryNote: String? = null
)

data class ServiceAreaResponse(
    val id: Long,
    val pincode: String,
    val locality: String,
    val cityName: String,
    val state: String,
    val cityId: Long?,
    val isActive: Boolean,
    val deliveryNote: String?,
    val createdAt: String?,
    val updatedAt: String?
)

data class BulkServiceAreaRequest(
    @field:NotBlank(message = "Pincode is required")
    @field:Pattern(regexp = "^[1-9][0-9]{5}$", message = "Invalid 6-digit Indian pincode")
    val pincode: String,

    @field:NotBlank(message = "City name is required")
    val cityName: String,

    @field:NotBlank(message = "State name is required")
    val state: String,

    val cityId: Long? = null,
    val localities: List<String>,
    val deliveryNote: String? = null,
    val isActive: Boolean = true
)

data class PincodeAvailabilityResponse(
    val isServiceable: Boolean,
    val pincode: String,
    val cityName: String?,
    val state: String?,
    val localities: List<String>,
    val deliveryNote: String?,
    val message: String
)

data class ServiceAreaStatsResponse(
    val totalPincodes: Long,
    val totalLocalities: Long,
    val activeLocalities: Long,
    val totalCities: Long
)
