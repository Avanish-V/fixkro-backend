package com.iotabuild.zippyhand_Admin.address.dto

import com.iotabuild.zippyhand_Admin.address.entity.Address

data class AddressResponse(
    val id: Long,
    val fullName: String,
    val userId: String,
    val phone: String,
    val addressLine1: String,
    val city: String,
    val state: String,
    val postalCode: String,
    val type: String,
    val isDefault: Boolean
)
fun Address.toResponse() = AddressResponse(
    id = id,
    fullName = fullName,
    userId = userId,
    phone = phone,
    addressLine1 = addressLine1,
    city = city,
    state = state,
    postalCode = postalCode,
    type = addressType.name,
    isDefault = isDefault
)
