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

    val iconType: String = "url", // "svg" or "url"
    
    @Column(length = 10000) // Increase length for large SVGs
    val iconValue: String = "",

    val themeColor: String = "#000000",

    val status: String = "active"
)
