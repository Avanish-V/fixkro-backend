package com.iotabuild.zippyhand_Admin.products.service

import com.iotabuild.zippyhand_Admin.products.models.*
import com.iotabuild.zippyhand_Admin.products.repository.CategoryRepository
import com.iotabuild.zippyhand_Admin.products.repository.ProductRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val reviewRepository: com.iotabuild.zippyhand_Admin.reviews.ReviewRepository
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
        return CategoryResponse(
            categoryId = saved.categoryId,
            title = saved.title,
            iconType = saved.iconType,
            iconValue = saved.iconValue,
            themeColor = saved.themeColor,
            status = saved.status,
            image = saved.iconValue
        )
    }

    fun getCategories(): List<CategoryResponse> {
        return categoryRepository.findAll().map { 
            CategoryResponse(
                categoryId = it.categoryId,
                title = it.title,
                iconType = it.iconType,
                iconValue = it.iconValue,
                themeColor = it.themeColor,
                status = it.status,
                image = it.iconValue
            ) 
        }
    }

    fun getCategory(categoryId: Long): CategoryResponse {
        val category = categoryRepository.findById(categoryId)
            .orElseThrow { NoSuchElementException("Category not found with id: $categoryId") }
        return CategoryResponse(
            categoryId = category.categoryId,
            title = category.title,
            iconType = category.iconType,
            iconValue = category.iconValue,
            themeColor = category.themeColor,
            status = category.status,
            image = category.iconValue
        )
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
                descriptions = request.descriptions
            )
        )
        return entityToResponse(product)
    }

    fun getService(categoryId: Long, status: Boolean? = null): List<ProductResponse> {
        return if (status != null) {
            productRepository.findAllByCategoryIdAndStatus(categoryId, status).map { entityToResponse(it) }
        } else {
            productRepository.findAllByCategoryId(categoryId).map { entityToResponse(it) }
        }
    }

    fun getAllServices(status: Boolean? = null): List<ProductResponse> {
        return if (status != null) {
            productRepository.findAllByStatus(status).map { entityToResponse(it) }
        } else {
            productRepository.findAll().map { entityToResponse(it) }
        }
    }

    fun getProduct(productId: Long): ProductResponse {
        val product = productRepository.findById(productId)
            .orElseThrow { NoSuchElementException("Product not found with id: $productId") }
        return entityToResponse(product)
    }

    private fun entityToResponse(product: ProductEntity): ProductResponse {
        val reviews = reviewRepository.findByProductId(product.productId)
        val avgRating = if (reviews.isEmpty()) 0.0 else reviews.map { it.rating }.average()
        
        val category = categoryRepository.findById(product.categoryId).orElse(null)
        val categoryImage = category?.iconValue ?: ""
        
        return ProductResponse(
            categoryId = product.categoryId,
            productId = product.productId,
            title = product.title,
            imageUrl = product.image,
            serviceType = product.serviceType,
            status = product.status,
            price = product.price,
            descriptions = product.descriptions,
            rating = avgRating,
            reviewCount = reviews.size,
            categoryImage = categoryImage
        )
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
            descriptions = request.descriptions
        )
        val saved = productRepository.save(updated)
        return entityToResponse(saved)
    }

    @Transactional
    fun toggleProductStatus(productId: Long, status: Boolean): ProductResponse {
        val existing = productRepository.findById(productId)
            .orElseThrow { NoSuchElementException("Product not found with id: $productId") }
        
        val updated = existing.copy(status = status)
        val saved = productRepository.save(updated)
        return entityToResponse(saved)
    }

    @Transactional
    fun deleteProduct(productId: Long) {
        if (!productRepository.existsById(productId)) {
            throw NoSuchElementException("Product not found with id: $productId")
        }
        productRepository.deleteById(productId)
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
        return CategoryResponse(
            categoryId = saved.categoryId,
            title = saved.title,
            iconType = saved.iconType,
            iconValue = saved.iconValue,
            themeColor = saved.themeColor,
            status = saved.status,
            image = saved.iconValue
        )
    }

    @Transactional
    fun deleteCategory(categoryId: Long) {
        if (!categoryRepository.existsById(categoryId)) {
            throw NoSuchElementException("Category not found with id: $categoryId")
        }
        // Optional: Check if products exist in this category before deleting
        categoryRepository.deleteById(categoryId)
    }
}
