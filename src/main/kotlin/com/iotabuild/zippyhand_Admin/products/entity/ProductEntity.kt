package com.iotabuild.zippyhand_Admin.products.entity

import jakarta.persistence.*
import org.hibernate.annotations.JdbcTypeCode
import org.hibernate.type.SqlTypes

@Entity
@Table(name = "products")
data class ProductEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val productId: Long = 0,
    @Column(name = "category_id", nullable = false)
    val categoryId: Long = 0,
    val title: String = "",
    val image: String = "",
    val serviceType: String = "",
    val status: Boolean = false,
    val price: Int = 0,
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    val descriptions: List<ProductDescriptionEntity> = emptyList()
)

data class ProductDescriptionEntity(
    val title: String = "",
    val shortDescription: String = ""
)
