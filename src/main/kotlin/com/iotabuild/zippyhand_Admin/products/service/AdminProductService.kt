package com.iotabuild.zippyhand_Admin.products.service

import com.iotabuild.zippyhand_Admin.products.dto.*
import com.iotabuild.zippyhand_Admin.products.entity.*
import com.iotabuild.zippyhand_Admin.products.repository.CategoryRepository
import com.iotabuild.zippyhand_Admin.products.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AdminProductService(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val queryService: ProductQueryService
) {

    @Transactional
    fun createCategory(request: CreateCategoryRequest): CategoryResponse {
        val entity = CategoryEntity(
            title = request.title,
            iconType = request.iconType,
            iconValue = request.iconValue,
            themeColor = request.themeColor,
            status = request.status
        )
        val saved = categoryRepository.save(entity)
        return queryService.getCategory(saved.categoryId)
    }

    @Transactional
    fun updateCategory(categoryId: Long, request: UpdateCategoryRequest): CategoryResponse {
        val existing = categoryRepository.findById(categoryId)
            .orElseThrow { NoSuchElementException("Category not found with id: $categoryId") }
        
        val updated = existing.copy(
            title = request.title,
            iconType = request.iconType,
            iconValue = request.iconValue,
            themeColor = request.themeColor,
            status = request.status
        )
        val saved = categoryRepository.save(updated)
        return queryService.getCategory(saved.categoryId)
    }

    @Transactional
    fun deleteCategory(categoryId: Long) {
        if (!categoryRepository.existsById(categoryId)) {
            throw NoSuchElementException("Category not found with id: $categoryId")
        }
        categoryRepository.deleteById(categoryId)
    }

    @Transactional
    fun createProduct(request: CreateProductRequest): ProductResponse {
        val product = productRepository.save(
            ProductEntity(
                categoryId = request.categoryId,
                image = request.imageUrl,
                title = request.title,
                serviceType = request.serviceType,
                status = request.status,
                price = request.price,
                descriptions = request.descriptions.map { ProductDescriptionEntity(it.title, it.shortDescription) }
            )
        )
        return queryService.getProduct(product.productId)
    }

    @Transactional
    fun updateProduct(productId: Long, request: UpdateProductRequest): ProductResponse {
        val existing = productRepository.findById(productId)
            .orElseThrow { NoSuchElementException("Product not found with id: $productId") }
        
        val updated = existing.copy(
            categoryId = request.categoryId,
            title = request.title,
            image = request.imageUrl,
            serviceType = request.serviceType,
            status = request.status,
            price = request.price,
            descriptions = request.descriptions.map { ProductDescriptionEntity(it.title, it.shortDescription) }
        )
        val saved = productRepository.save(updated)
        return queryService.getProduct(saved.productId)
    }

    @Transactional
    fun toggleProductStatus(productId: Long, status: Boolean): ProductResponse {
        val existing = productRepository.findById(productId)
            .orElseThrow { NoSuchElementException("Product not found with id: $productId") }
        
        val updated = existing.copy(status = status)
        val saved = productRepository.save(updated)
        return queryService.getProduct(saved.productId)
    }

    @Transactional
    fun deleteProduct(productId: Long) {
        if (!productRepository.existsById(productId)) {
            throw NoSuchElementException("Product not found with id: $productId")
        }
        productRepository.deleteById(productId)
    }
}
