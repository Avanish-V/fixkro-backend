package com.iotabuild.zippyhand_Admin.products.controller

import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import com.iotabuild.zippyhand_Admin.products.dto.CategoryResponse
import com.iotabuild.zippyhand_Admin.products.dto.HomeResponse
import com.iotabuild.zippyhand_Admin.products.dto.ProductResponse
import com.iotabuild.zippyhand_Admin.products.service.CustomerProductService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1")
class CustomerProductController(
    private val customerProductService: CustomerProductService
) {
    @GetMapping("/home")
    suspend fun getHome(): ResponseEntity<HomeResponse>{
        val response = customerProductService.getHome()
        return ResponseEntity.ok(response)
    }

    @GetMapping("/categories")
    fun getAllCategories(): ApiResponse<List<CategoryResponse>> {
        return ApiResponse.success(customerProductService.getAllCategories())
    }

    @GetMapping("/category/{id}")
    fun getCategoryById(@PathVariable id: Long): ApiResponse<CategoryResponse> {
        return ApiResponse.success(customerProductService.getCategoryById(id))
    }

    @GetMapping("/services")
    fun getByCategory(@RequestParam categoryId: Long): ApiResponse<List<ProductResponse>> {
        return ApiResponse.success(customerProductService.getProductsByCategory(categoryId))
    }

    @GetMapping("/product/{id}")
    fun getProductById(@PathVariable id: Long): ApiResponse<ProductResponse> {
        return ApiResponse.success(customerProductService.getProductDetails(id))
    }
}
