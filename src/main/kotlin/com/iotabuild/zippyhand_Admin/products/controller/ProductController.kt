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

    @PostMapping("/service")
    fun createProduct(@Valid @RequestBody request: CreateProductRequest): ApiResponse<ProductResponse> {
        return ApiResponse.success(productService.createProduct(request))
    }

    @GetMapping("/services")
    fun getByCategory(@RequestParam categoryId: Long): ApiResponse<List<ProductResponse>> {
        return ApiResponse.success(productService.getService(categoryId))
    }

    @GetMapping("/product/{id}")
    fun getProductById(@PathVariable id: Long): ApiResponse<ProductResponse> {
        return ApiResponse.success(productService.getProduct(id))
    }

}