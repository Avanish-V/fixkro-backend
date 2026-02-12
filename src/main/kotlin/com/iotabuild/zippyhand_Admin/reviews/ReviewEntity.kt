package com.iotabuild.zippyhand_Admin.reviews

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "reviews")
data class Review(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "product_id", nullable = false)
    val productId: Long,

    @Column(name = "customer_name", nullable = false)
    val customerName: String,

    @Column(nullable = false)
    val rating: Int,

    @Column(columnDefinition = "text")
    val comment: String,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
