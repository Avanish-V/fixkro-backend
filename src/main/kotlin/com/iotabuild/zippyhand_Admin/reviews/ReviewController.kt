package com.iotabuild.zippyhand_Admin.reviews

import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/reviews")
class ReviewController(private val reviewService: ReviewService) {

    @GetMapping
    fun getAllReviews(): ApiResponse<List<ReviewResponse>> {
        return ApiResponse.success(reviewService.getAllReviews())
    }

    @GetMapping("/product/{productId}")
    fun getReviewsByProduct(@PathVariable productId: Long): ApiResponse<List<ReviewResponse>> {
        return ApiResponse.success(reviewService.getReviewsByProduct(productId))
    }

    @PostMapping
    fun createReview(@RequestBody request: CreateReviewRequest): ApiResponse<ReviewResponse> {
        return ApiResponse.success(reviewService.createReview(request))
    }

    @PutMapping("/{id}")
    fun updateReview(@PathVariable id: Long, @RequestBody request: CreateReviewRequest): ApiResponse<ReviewResponse> {
        return ApiResponse.success(reviewService.updateReview(id, request))
    }

    @DeleteMapping("/{id}")
    fun deleteReview(@PathVariable id: Long): ApiResponse<String> {
        reviewService.deleteReview(id)
        return ApiResponse.success("Review deleted successfully")
    }
}
