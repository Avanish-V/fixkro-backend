package com.iotabuild.zippyhand_Admin.city.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "cities")
data class City(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    val name: String = "",

    @Column(nullable = false)
    val state: String = "",

    @Column(nullable = false)
    val pincode: String = "",

    @Column(nullable = false)
    val symbol: String = "🏙️",

    @Column(nullable = false)
    val isActive: Boolean = true,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now()
)
