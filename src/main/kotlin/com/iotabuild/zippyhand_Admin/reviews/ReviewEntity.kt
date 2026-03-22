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
    val productId: Long = 0,

    @Column(name = "order_id", nullable = true)
    val orderId: Long? = null,

    @Column(name = "customer_name", nullable = false)
    val customerName: String = "",

    @Column(nullable = false)
    val rating: Int = 0, // Service Rating

    @Column(name = "technician_rating", nullable = false)
    val technicianRating: Int = 0,

    @Column(name = "is_recommended", nullable = false)
    val isRecommended: Boolean = true,

    @ElementCollection
    @CollectionTable(name = "review_impressions", joinColumns = [JoinColumn(name = "review_id")])
    @Column(name = "impression")
    val impressions: Set<String> = emptySet(),

    @Column(columnDefinition = "text")
    val comment: String = "",

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
)
