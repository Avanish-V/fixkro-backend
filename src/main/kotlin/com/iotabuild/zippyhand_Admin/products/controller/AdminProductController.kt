package com.iotabuild.zippyhand_Admin.products.controller

import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import com.iotabuild.zippyhand_Admin.products.dto.*
import com.iotabuild.zippyhand_Admin.products.service.AdminProductService
import com.iotabuild.zippyhand_Admin.products.service.ProductQueryService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin")
class AdminProductController(
    private val adminProductService: AdminProductService,
    private val queryService: ProductQueryService
) {

    @PostMapping("/category")
    fun createCategory(@Valid @RequestBody request: CreateCategoryRequest): ApiResponse<CategoryResponse> {
        return ApiResponse.success(adminProductService.createCategory(request))
    }

    @PutMapping("/category/{id}")
    fun updateCategory(@PathVariable id: Long, @Valid @RequestBody request: UpdateCategoryRequest): ApiResponse<CategoryResponse> {
        return ApiResponse.success(adminProductService.updateCategory(id, request))
    }

    @DeleteMapping("/category/{id}")
    fun deleteCategory(@PathVariable id: Long): ApiResponse<String> {
        adminProductService.deleteCategory(id)
        return ApiResponse.success("Category deleted successfully")
    }

    @PostMapping("/service")
    fun createProduct(@Valid @RequestBody request: CreateProductRequest): ApiResponse<ProductResponse> {
        return ApiResponse.success(adminProductService.createProduct(request))
    }

    @PutMapping("/product/{id}")
    fun updateProduct(@PathVariable id: Long, @Valid @RequestBody request: UpdateProductRequest): ApiResponse<ProductResponse> {
        return ApiResponse.success(adminProductService.updateProduct(id, request))
    }

    @PatchMapping("/product/{id}/status")
    fun toggleProductStatus(@PathVariable id: Long, @RequestParam status: Boolean): ApiResponse<ProductResponse> {
        return ApiResponse.success(adminProductService.toggleProductStatus(id, status))
    }

    @DeleteMapping("/product/{id}")
    fun deleteProduct(@PathVariable id: Long): ApiResponse<String> {
        adminProductService.deleteProduct(id)
        return ApiResponse.success("Product deleted successfully")
    }
    
    @GetMapping("/services/all")
    fun getAllServices(@RequestParam(required = false) status: Boolean?): ApiResponse<List<ProductResponse>> {
        return ApiResponse.success(queryService.getAllProducts(status))
    }
}
