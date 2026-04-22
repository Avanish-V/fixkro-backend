package com.iotabuild.zippyhand_Admin.address.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateAddressRequest(
    @field:NotBlank val fullName: String,

    val phone: String,

    @field:NotBlank(message = "AddressLine1 cant be empty")
    val addressLine1: String,

    val addressLine2: String?,

    @field:NotBlank val city: String,

    val state: String,

    @field:NotBlank val postalCode: String,
    
    val type: String = "OTHER",

    val isDefault: Boolean = false
)
