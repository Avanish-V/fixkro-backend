package com.iotabuild.zippyhand_Admin.products.controller

import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import com.iotabuild.zippyhand_Admin.products.models.*
import com.iotabuild.zippyhand_Admin.products.service.ProductService
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class ProductController(private val productService: ProductService) {
    private val logger = LoggerFactory.getLogger(ProductController::class.java)

    @PostMapping("/category")
    fun createCategory(@Valid @RequestBody request: CreateCategoryRequest): ApiResponse<CategoryResponse> {
        return try {
            ApiResponse.success(productService.createCategory(request))
        } catch (e: Exception) {
            logger.error("Error creating category: ${e.message}", e)
            ApiResponse.error("Failed to create category: ${e.message}")
        }
    }

    @GetMapping("/categories")
    fun getAllCategories(): ApiResponse<List<CategoryResponse>> {
        return ApiResponse.success(productService.getCategories())
    }

    @GetMapping("/category/{id}")
    fun getCategoryById(@PathVariable id: Long): ApiResponse<CategoryResponse> {
        return ApiResponse.success(productService.getCategory(id))
    }

    @PostMapping("/service")
    fun createProduct(@Valid @RequestBody request: CreateProductRequest): ApiResponse<ProductResponse> {
        return try {
            ApiResponse.success(productService.createProduct(request))
        } catch (e: Exception) {
            logger.error("Error creating product: ${e.message}", e)
            ApiResponse.error("Failed to create product: ${e.message}")
        }
    }

    @GetMapping("/services")
    fun getByCategory(@RequestParam categoryId: Long, @RequestParam(required = false) status: String?): ApiResponse<List<ProductResponse>> {
        val finalStatus = when (status?.lowercase()) {
            "all" -> null
            "inactive" -> false
            "active", "true" -> true
            else -> true // Default for mobile and unauthenticated requests
        }
        return ApiResponse.success(productService.getService(categoryId, finalStatus))
    }

    @GetMapping("/services/all")
    fun getAllServices(@RequestParam(required = false) status: String?): ApiResponse<List<ProductResponse>> {
        val finalStatus = when (status?.lowercase()) {
            "all" -> null
            "inactive" -> false
            "active", "true" -> true
            else -> true // Default to active
        }
        return ApiResponse.success(productService.getAllServices(finalStatus))
    }

    @GetMapping("/product/{id}")
    fun getProductById(@PathVariable id: Long): ApiResponse<ProductResponse> {
        return ApiResponse.success(productService.getProduct(id))
    }

    @PutMapping("/product/{id}")
    fun updateProduct(@PathVariable id: Long, @Valid @RequestBody request: UpdateProductRequest): ApiResponse<ProductResponse> {
        return ApiResponse.success(productService.updateProduct(id, request))
    }

    @PatchMapping("/product/{id}/status")
    fun toggleProductStatus(@PathVariable id: Long, @RequestParam status: Boolean): ApiResponse<ProductResponse> {
        return try {
            ApiResponse.success(productService.toggleProductStatus(id, status))
        } catch (e: Exception) {
            logger.error("Error toggling product status: ${e.message}", e)
            ApiResponse.error("Failed to toggle status: ${e.message}")
        }
    }

    @DeleteMapping("/product/{id}")
    fun deleteProduct(@PathVariable id: Long): ApiResponse<String> {
        productService.deleteProduct(id)
        return ApiResponse.success("Product deleted successfully")
    }

    @PutMapping("/category/{id}")
    fun updateCategory(@PathVariable id: Long, @Valid @RequestBody request: UpdateCategoryRequest): ApiResponse<CategoryResponse> {
        return ApiResponse.success(productService.updateCategory(id, request))
    }

    @DeleteMapping("/category/{id}")
    fun deleteCategory(@PathVariable id: Long): ApiResponse<String> {
        productService.deleteCategory(id)
        return ApiResponse.success("Category deleted successfully")
    }

}
