package com.iotabuild.zippyhand_Admin.products.service

import com.iotabuild.zippyhand_Admin.products.dto.CategoryResponse
import com.iotabuild.zippyhand_Admin.products.dto.ProductResponse
import com.iotabuild.zippyhand_Admin.products.entity.ProductEntity
import com.iotabuild.zippyhand_Admin.products.mapper.ProductMapper
import com.iotabuild.zippyhand_Admin.products.repository.CategoryRepository
import com.iotabuild.zippyhand_Admin.products.repository.ProductRepository
import com.iotabuild.zippyhand_Admin.reviews.ReviewRepository
import org.springframework.stereotype.Service

@Service
class ProductQueryService(
    private val productRepository: ProductRepository,
    private val categoryRepository: CategoryRepository,
    private val reviewRepository: ReviewRepository,
    private val productMapper: ProductMapper
) {

    fun getCategory(categoryId: Long): CategoryResponse {
        val category = categoryRepository.findById(categoryId)
            .orElseThrow { NoSuchElementException("Category not found with id: $categoryId") }
        return productMapper.toCategoryResponse(category)
    }

    fun getAllCategories(): List<CategoryResponse> {
        return categoryRepository.findAll().map { productMapper.toCategoryResponse(it) }
    }

    fun getProduct(productId: Long): ProductResponse {
        val product = productRepository.findById(productId)
            .orElseThrow { NoSuchElementException("Product not found with id: $productId") }
        return entityToResponse(product)
    }

    fun getProductsByCategory(categoryId: Long, status: Boolean? = null): List<ProductResponse> {
        val products = if (status != null) {
            productRepository.findAllByCategoryIdAndStatus(categoryId, status)
        } else {
            productRepository.findAllByCategoryId(categoryId)
        }
        return products.map { entityToResponse(it) }
    }

    fun getAllProducts(status: Boolean? = null): List<ProductResponse> {
        val products = if (status != null) {
            productRepository.findAllByStatus(status)
        } else {
            productRepository.findAll()
        }
        return products.map { entityToResponse(it) }
    }

    private fun entityToResponse(product: ProductEntity): ProductResponse {
        val reviews = reviewRepository.findByProductId(product.productId)
        val avgRating = if (reviews.isEmpty()) 0.0 else reviews.map { it.rating }.average()
        val category = categoryRepository.findById(product.categoryId).orElse(null)
        
        return productMapper.toProductResponse(product, category, avgRating, reviews.size)
    }
}
