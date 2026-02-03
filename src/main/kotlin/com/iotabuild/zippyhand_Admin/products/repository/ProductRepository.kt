package com.iotabuild.zippyhand_Admin.products.repository

import com.iotabuild.zippyhand_Admin.products.models.CategoryEntity
import com.iotabuild.zippyhand_Admin.products.models.ProductEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface CategoryRepository : JpaRepository<CategoryEntity, Long>

@Repository
interface ProductRepository : JpaRepository<ProductEntity, Long> {
    fun findAllByCategoryId(categoryId: Long): List<ProductEntity>
}