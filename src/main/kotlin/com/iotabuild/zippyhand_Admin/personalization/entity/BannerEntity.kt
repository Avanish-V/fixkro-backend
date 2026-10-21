package com.iotabuild.zippyhand_Admin.personalization.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "banners")
data class BannerEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, length = 2048)
    var imageUrl: String = "",

    var title: String? = null,

    var linkUrl: String? = null,

    var isActive: Boolean = true,

    var displayOrder: Int = 0,

    var createdAt: LocalDateTime = LocalDateTime.now(),

    var updatedAt: LocalDateTime = LocalDateTime.now()
)
