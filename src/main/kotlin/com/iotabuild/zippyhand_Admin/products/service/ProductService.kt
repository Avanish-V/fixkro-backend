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
            image = request.image
        )
        val saved = categoryRepository.save(entity)
        return CategoryResponse(saved.categoryId, saved.title, saved.image)
    }

    fun getCategories(): List<CategoryResponse> {
        return categoryRepository.findAll().map { 
            CategoryResponse(it.categoryId, it.title, it.image) 
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
}
