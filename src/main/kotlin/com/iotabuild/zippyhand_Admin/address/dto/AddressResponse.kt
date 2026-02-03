package com.iotabuild.zippyhand_Admin.address.dto

import com.iotabuild.zippyhand_Admin.address.Entity.Address

data class AddressResponse(
    val id: Long,
    val fullName: String,
    val phone: String?,
    val addressLine1: String,
    val addressLine2: String?,
    val city: String,
    val state: String?,
    val postalCode: String,
    val isDefault: Boolean
)
fun Address.toResponse() = AddressResponse(
    id = id,
    fullName = fullName,
    phone = phone,
    addressLine1 = addressLine1,
    addressLine2 = addressLine2,
    city = city,
    state = state,
    postalCode = postalCode,
    isDefault = false
)
