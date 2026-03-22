package com.iotabuild.zippyhand_Admin.reviews

import com.iotabuild.zippyhand_Admin.products.repository.ProductRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import org.slf4j.LoggerFactory

data class ReviewResponse(
    val id: Long,
    val productId: Long,
    val orderId: Long?,
    val productName: String,
    val customerName: String,
    val rating: Int,
    val technicianRating: Int,
    val isRecommended: Boolean,
    val impressions: Set<String>,
    val comment: String,
    val createdAt: LocalDateTime
)

data class CreateReviewRequest(
    val productId: Long,
    val orderId: Long,
    val customerName: String,
    val rating: Int,
    val technicianRating: Int,
    val technicianId: Long,
    val isRecommended: Boolean,
    val impressions: Set<String>,
    val comment: String
)

@Service
class ReviewService(
    private val reviewRepository: ReviewRepository,
    private val productRepository: ProductRepository
) {
    private val log = LoggerFactory.getLogger(ReviewService::class.java)

    fun getAllReviews(): List<ReviewResponse> {
        return reviewRepository.findAll().map { review ->
            val product = productRepository.findById(review.productId).orElse(null)
            ReviewResponse(
                id = review.id,
                productId = review.productId,
                orderId = review.orderId,
                productName = product?.title ?: "Unknown Product",
                customerName = review.customerName,
                rating = review.rating,
                technicianRating = review.technicianRating,
                isRecommended = review.isRecommended,
                impressions = review.impressions,
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
                orderId = review.orderId,
                productName = productName,
                customerName = review.customerName,
                rating = review.rating,
                technicianRating = review.technicianRating,
                isRecommended = review.isRecommended,
                impressions = review.impressions,
                comment = review.comment,
                createdAt = review.createdAt
            )
        }
    }

    fun getReview(id: Long): ReviewResponse {
        val review = reviewRepository.findById(id).orElseThrow { RuntimeException("Review not found") }
        val product = productRepository.findById(review.productId).orElse(null)
        val productName = product?.title ?: "Unknown Product"

        return ReviewResponse(
            id = review.id,
            productId = review.productId,
            orderId = review.orderId,
            productName = productName,
            customerName = review.customerName,
            rating = review.rating,
            technicianRating = review.technicianRating,
            isRecommended = review.isRecommended,
            impressions = review.impressions,
            comment = review.comment,
            createdAt = review.createdAt
        )
    }

    @Transactional
    fun createReview(request: CreateReviewRequest): ReviewResponse {
        log.info("Creating review for product ${request.productId}, order ${request.orderId}")
        val review = Review(
            productId = request.productId,
            orderId = request.orderId,
            customerName = request.customerName,
            rating = request.rating,
            technicianRating = request.technicianRating,
            technicianId = request.technicianId,
            isRecommended = request.isRecommended,
            impressions = request.impressions,
            comment = request.comment
        )
        val savedReview = try {
            log.info("Attempting to save review to database")
            reviewRepository.save(review)
        } catch (e: Exception) {
            log.error("Failed to save review to database: ${e.message}", e)
            throw e
        }
        
        log.info("Review saved with ID ${savedReview.id}. Fetching product info.")
        val product = productRepository.findById(request.productId).orElse(null)
        if (product == null) {
            log.warn("Product with ID ${request.productId} not found for review enrichment")
        }
        
        return ReviewResponse(
            id = savedReview.id,
            productId = savedReview.productId,
            orderId = savedReview.orderId,
            productName = product?.title ?: "Unknown Product",
            customerName = savedReview.customerName,
            rating = savedReview.rating,
            technicianRating = savedReview.technicianRating,
            isRecommended = savedReview.isRecommended,
            impressions = savedReview.impressions,
            comment = savedReview.comment,
            createdAt = savedReview.createdAt
        )
    }

    @Transactional
    fun updateReview(id: Long, request: CreateReviewRequest): ReviewResponse {
        val existingReview = reviewRepository.findById(id).orElseThrow { RuntimeException("Review not found") }
        val updatedReview = existingReview.copy(
            productId = request.productId,
            orderId = request.orderId,
            customerName = request.customerName,
            rating = request.rating,
            technicianRating = request.technicianRating,
            isRecommended = request.isRecommended,
            impressions = request.impressions,
            comment = request.comment
        )
        val savedReview = reviewRepository.save(updatedReview)
        val product = productRepository.findById(request.productId).orElse(null)

        return ReviewResponse(
            id = savedReview.id,
            productId = savedReview.productId,
            orderId = savedReview.orderId,
            productName = product?.title ?: "Unknown Product",
            customerName = savedReview.customerName,
            rating = savedReview.rating,
            technicianRating = savedReview.technicianRating,
            isRecommended = savedReview.isRecommended,
            impressions = savedReview.impressions,
            comment = savedReview.comment,
            createdAt = savedReview.createdAt
        )
    }

    @Transactional
    fun deleteReview(id: Long) {
        reviewRepository.deleteById(id)
    }
}
