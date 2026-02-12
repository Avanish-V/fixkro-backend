package com.iotabuild.zippyhand_Admin.products.controller

import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import com.iotabuild.zippyhand_Admin.products.models.*
import com.iotabuild.zippyhand_Admin.products.service.ProductService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class ProductController(private val productService: ProductService) {

    @PostMapping("/category")
    fun createCategory(@Valid @RequestBody request: CreateCategoryRequest): ApiResponse<CategoryResponse> {
        return ApiResponse.success(productService.createCategory(request))
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
        return ApiResponse.success(productService.createProduct(request))
    }

    @GetMapping("/services")
    fun getByCategory(@RequestParam categoryId: Long): ApiResponse<List<ProductResponse>> {
        return ApiResponse.success(productService.getService(categoryId))
    }

    @GetMapping("/services/all")
    fun getAllServices(): ApiResponse<List<ProductResponse>> {
        return ApiResponse.success(productService.getAllServices())
    }

    @GetMapping("/product/{id}")
    fun getProductById(@PathVariable id: Long): ApiResponse<ProductResponse> {
        return ApiResponse.success(productService.getProduct(id))
    }

    @PutMapping("/product/{id}")
    fun updateProduct(@PathVariable id: Long, @Valid @RequestBody request: UpdateProductRequest): ApiResponse<ProductResponse> {
        return ApiResponse.success(productService.updateProduct(id, request))
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
