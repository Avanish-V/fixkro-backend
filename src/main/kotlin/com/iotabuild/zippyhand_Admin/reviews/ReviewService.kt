package com.iotabuild.zippyhand_Admin.reviews

import com.iotabuild.zippyhand_Admin.products.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

data class ReviewResponse(
    val id: Long,
    val productId: Long,
    val productName: String,
    val customerName: String,
    val rating: Int,
    val comment: String,
    val createdAt: LocalDateTime
)

data class CreateReviewRequest(
    val productId: Long,
    val customerName: String,
    val rating: Int,
    val comment: String
)

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val productRepository: ProductRepository
) {

    fun getAllReviews(): List<ReviewResponse> {
        val reviews = reviewRepository.findAll()
        val products = productRepository.findAll().associateBy { it.productId }
        
        return reviews.map { review ->
            ReviewResponse(
                id = review.id,
                productId = review.productId,
                productName = products[review.productId]?.title ?: "Unknown Product",
                customerName = review.customerName,
                rating = review.rating,
                comment = review.comment,
                createdAt = review.createdAt
            )
        }
    }

    fun getReviewsByProduct(productId: Long): List<ReviewResponse> {
        val reviews = reviewRepository.findByProductId(productId)
        val product = productRepository.findById(productId).orElse(null)
        val productName = product?.title ?: "Unknown Product"

        return reviews.map { review ->
            ReviewResponse(
                id = review.id,
                productId = review.productId,
                productName = productName,
                customerName = review.customerName,
                rating = review.rating,
                comment = review.comment,
                createdAt = review.createdAt
            )
        }
    }

    @Transactional
    fun createReview(request: CreateReviewRequest): ReviewResponse {
        val review = Review(
            productId = request.productId,
            customerName = request.customerName,
            rating = request.rating,
            comment = request.comment
        )
        val savedReview = reviewRepository.save(review)
        val product = productRepository.findById(request.productId).orElse(null)
        
        return ReviewResponse(
            id = savedReview.id,
            productId = savedReview.productId,
            productName = product?.title ?: "Unknown Product",
            customerName = savedReview.customerName,
            rating = savedReview.rating,
            comment = savedReview.comment,
            createdAt = savedReview.createdAt
        )
    }

    @Transactional
    fun updateReview(id: Long, request: CreateReviewRequest): ReviewResponse {
        val existingReview = reviewRepository.findById(id).orElseThrow { RuntimeException("Review not found") }
        val updatedReview = existingReview.copy(
            productId = request.productId,
            customerName = request.customerName,
            rating = request.rating,
            comment = request.comment
        )
        val savedReview = reviewRepository.save(updatedReview)
        val product = productRepository.findById(request.productId).orElse(null)

        return ReviewResponse(
            id = savedReview.id,
            productId = savedReview.productId,
            productName = product?.title ?: "Unknown Product",
            customerName = savedReview.customerName,
            rating = savedReview.rating,
            comment = savedReview.comment,
            createdAt = savedReview.createdAt
        )
    }

    @Transactional
    fun deleteReview(id: Long) {
        reviewRepository.deleteById(id)
    }
}
