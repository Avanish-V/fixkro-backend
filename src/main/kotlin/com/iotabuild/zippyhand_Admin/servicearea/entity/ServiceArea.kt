package com.iotabuild.zippyhand_Admin.servicearea.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(
    name = "service_areas",
    indexes = [
        Index(name = "idx_service_pincode", columnList = "pincode"),
        Index(name = "idx_service_city", columnList = "cityName"),
        Index(name = "idx_service_active", columnList = "isActive")
    ]
)
data class ServiceArea(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, length = 10)
    var pincode: String = "",

    @Column(nullable = false, length = 150)
    var locality: String = "",

    @Column(nullable = false, length = 100)
    var cityName: String = "",

    @Column(nullable = false, length = 100)
    var state: String = "",

    var cityId: Long? = null,

    @Column(nullable = false)
    var isActive: Boolean = true,

    @Column(length = 255)
    var deliveryNote: String? = null,

    @Column(nullable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
