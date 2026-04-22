package com.iotabuild.zippyhand_Admin.products.repository

import com.iotabuild.zippyhand_Admin.products.entity.CategoryEntity
import com.iotabuild.zippyhand_Admin.products.entity.ProductEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository : JpaRepository<CategoryEntity, Long>

@Repository
interface ProductRepository : JpaRepository<ProductEntity, Long> {
    fun findAllByCategoryId(categoryId: Long): List<ProductEntity>
    fun findAllByCategoryIdAndStatus(categoryId: Long, status: Boolean): List<ProductEntity>
    fun findAllByStatus(status: Boolean): List<ProductEntity>
}