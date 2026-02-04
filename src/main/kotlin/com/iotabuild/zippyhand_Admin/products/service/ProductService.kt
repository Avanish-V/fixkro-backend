package com.iotabuild.zippyhand_Admin.products.service

import com.iotabuild.zippyhand_Admin.products.models.*
import com.iotabuild.zippyhand_Admin.products.repository.CategoryRepository
import com.iotabuild.zippyhand_Admin.products.repository.ProductRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

@Service
class ProductService(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository
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
            status = saved.status
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
                status = it.status
            ) 
        }
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

    fun getService(categoryId: Long): List<ProductResponse> {
        return productRepository.findAllByCategoryId(categoryId).map {
            entityToResponse(it)
        }
    }

    fun getProduct(productId: Long): ProductResponse {
        val product = productRepository.findById(productId)
            .orElseThrow { NoSuchElementException("Product not found with id: $productId") }
        return entityToResponse(product)
    }

    private fun entityToResponse(product: ProductEntity): ProductResponse = ProductResponse(
        categoryId = product.categoryId,
        productId = product.productId,
        title = product.title,
        imageUrl = product.image,
        serviceType = product.serviceType,
        status = product.status,
        price = product.price,
        descriptions = product.descriptions
    )

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
    fun deleteProduct(productId: Long) {
        if (!productRepository.existsById(productId)) {
            throw NoSuchElementException("Product not found with id: $productId")
        }
        productRepository.deleteById(productId)
    }
}
