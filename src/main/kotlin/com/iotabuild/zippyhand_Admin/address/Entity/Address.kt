package com.iotabuild.zippyhand_Admin.address.entity

import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

enum class AddressType {
    HOME, OFFICE, OTHER
}

@Entity
@Table(name = "addresses")
data class Address(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    val userId: String,

    val fullName: String,
    val phone: String,

    val addressLine1: String,
    val addressLine2: String?,

    val city: String,
    val state: String,
    val postalCode: String,

    val addressType: AddressType = AddressType.OTHER,

    val isDefault: Boolean = false,

    val createdAt: Instant = Instant.now(),
    val updatedAt: Instant = Instant.now(),
    val deletedAt: Instant? = null
)
