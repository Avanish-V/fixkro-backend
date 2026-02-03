package com.iotabuild.zippyhand_Admin.products.models

import jakarta.persistence.*

@Entity
@Table(name = "categories")
data class CategoryEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val categoryId: Long = 0,

    @Column(nullable = false)
    val title: String = "",

    val image: String = ""
)
