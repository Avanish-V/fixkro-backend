package com.iotabuild.zippyhand_Admin.reviews

import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import org.springframework.web.bind.annotation.*
import org.slf4j.LoggerFactory

@RestController
@RequestMapping("/api/v1/reviews")
class ReviewController(private val reviewService: ReviewService) {

    private val log = LoggerFactory.getLogger(ReviewController::class.java)

    @GetMapping
    fun getAllReviews(): ApiResponse<List<ReviewResponse>> {
        return ApiResponse.success(reviewService.getAllReviews())
    }

    @GetMapping("/product/{productId}")
    fun getReviewsByProduct(@PathVariable productId: Long): ApiResponse<List<ReviewResponse>> {
        return ApiResponse.success(reviewService.getReviewsByProduct(productId))
    }

    /**
     * Customer-facing endpoint that returns reviews in the mobile client format.
     * Matches the client's Review data class: id, userName, userImage, rating (Double), date, comment.
     * Publicly accessible — no authentication required.
     */
    @GetMapping("/product/{productId}/client")
    fun getCustomerReviewsByProduct(@PathVariable productId: Long): ApiResponse<List<CustomerReviewResponse>> {
        return ApiResponse.success(reviewService.getCustomerReviewsByProduct(productId))
    }

    @PostMapping
    fun createReview(@RequestBody request: CreateReviewRequest): ApiResponse<ReviewResponse> {
        log.info("Received request to create review: {}", request)
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
