package com.iotabuild.zippyhand_Admin.common.service

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest
import software.amazon.awssdk.services.s3.model.GetObjectRequest
import software.amazon.awssdk.services.s3.model.GetUrlRequest
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import software.amazon.awssdk.services.s3.presigner.S3Presigner
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest
import java.time.Duration
import java.util.*

@Service
class S3StorageService(
    private val s3Client: S3Client,
    private val s3Presigner: S3Presigner,
    @Value("\${aws.s3.bucket-name}") private val bucketName: String,
    @Value("\${aws.s3.region}") private val region: String,
    @Value("\${aws.s3.public-url-prefix:}") private val publicUrlPrefix: String
) : StorageService {

    private val logger = LoggerFactory.getLogger(S3StorageService::class.java)

    companion object {
        const val FOLDER_CATEGORIES = "categories"
        const val FOLDER_PRODUCTS = "products"
        const val FOLDER_PROFESSIONALS = "professionals"
        const val FOLDER_DOCUMENTS = "documents"
    }

    override fun uploadFile(file: MultipartFile, folder: String): String {
        validateFile(file)
        
        val originalFilename = file.originalFilename ?: "unnamed"
        val extension = getFileExtension(originalFilename).lowercase()
        val contentType = file.contentType ?: ""
        
        // Robust SVG detection
        val isSvg = extension == "svg" || contentType == "image/svg+xml" || contentType == "text/xml"
        
        val targetExtension = if (isSvg && extension != "svg") "svg" else extension
        val fileName = "${folder}/${UUID.randomUUID()}${if (targetExtension.isNotEmpty()) ".$targetExtension" else ""}"
        val targetContentType = if (isSvg) "image/svg+xml" else contentType.ifBlank { "application/octet-stream" }
        
        logger.info("Uploading file: $originalFilename as $fileName, Detected Content-Type: $targetContentType, Size: ${file.size} bytes")
        
        return try {
            val putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .contentType(targetContentType)
                .build()

            logger.info("Sending PutObjectRequest to bucket: $bucketName, key: $fileName, region: $region")
            
            // Using fromBytes is safer for small files and SVGs to ensure the content is fully read
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.bytes))

            val url = if (publicUrlPrefix.isNotBlank()) {
                "${publicUrlPrefix.removeSuffix("/")}/$fileName"
            } else {
                s3Client.utilities().getUrl(GetUrlRequest.builder().bucket(bucketName).key(fileName).build()).toExternalForm()
            }
            
            logger.info("Successfully uploaded $originalFilename. URL: $url")
            url
        } catch (e: Exception) {
            logger.error("Failed to upload file $originalFilename to S3 (Bucket: $bucketName, Region: $region): ${e.message}", e)
            throw RuntimeException("Failed to upload file to storage: ${e.message}")
        }
    }

    override fun deleteFile(fileUrl: String) {
        val key = extractKeyFromUrl(fileUrl)
        logger.info("Deleting file with key: $key from bucket: $bucketName")
        
        try {
            val deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .build()
            s3Client.deleteObject(deleteObjectRequest)
        } catch (e: Exception) {
            logger.error("Failed to delete file from S3: ${e.message}", e)
            // We usually don't throw on delete failure to avoid breaking transactions
        }
    }

    override fun generatePresignedUrl(fileName: String, expirationMinutes: Long): String {
        val getObjectRequest = GetObjectRequest.builder()
            .bucket(bucketName)
            .key(fileName)
            .build()

        val presignRequest = GetObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(expirationMinutes))
            .getObjectRequest(getObjectRequest)
            .build()

        val presignedRequest = s3Presigner.presignGetObject(presignRequest)
        return presignedRequest.url().toExternalForm()
    }

    override fun generatePresignedPutUrl(folder: String, extension: String, contentType: String): Map<String, String> {
        val targetExtension = if (extension.startsWith(".")) extension else ".$extension"
        val fileName = "${folder}/${UUID.randomUUID()}$targetExtension"
        
        val putObjectRequest = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(fileName)
            .contentType(contentType)
            .build()
            
        val presignRequest = software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest.builder()
            .signatureDuration(Duration.ofMinutes(15))
            .putObjectRequest(putObjectRequest)
            .build()
            
        val presignedRequest = s3Presigner.presignPutObject(presignRequest)
        
        val publicUrl = if (publicUrlPrefix.isNotBlank()) {
            "${publicUrlPrefix.removeSuffix("/")}/$fileName"
        } else {
            s3Client.utilities().getUrl(GetUrlRequest.builder().bucket(bucketName).key(fileName).build()).toExternalForm()
        }
        
        return mapOf(
            "uploadUrl" to presignedRequest.url().toExternalForm(),
            "publicUrl" to publicUrl
        )
    }

    private fun validateFile(file: MultipartFile) {
        if (file.isEmpty) {
            throw IllegalArgumentException("Cannot upload empty file")
        }
        // Max 10MB limit (example)
        if (file.size > 10 * 1024 * 1024) {
            throw IllegalArgumentException("File size exceeds limit of 10MB")
        }
    }

    private fun getFileExtension(fileName: String): String {
        return fileName.substringAfterLast('.', "")
    }

    private fun extractKeyFromUrl(url: String): String {
        return try {
            val keyCandidate = if (publicUrlPrefix.isNotBlank() && url.contains(publicUrlPrefix)) {
                url.substringAfter(publicUrlPrefix.removeSuffix("/"))
            } else if (url.contains(".amazonaws.com/")) {
                url.substringAfter(".amazonaws.com/")
            } else {
                // Last resort: assume the last part of the URL contains the rest of the path
                // This is risky but handles some custom setups.
                url.substringAfterLast(".com/")
            }
            keyCandidate.removePrefix("/")
        } catch (e: Exception) {
            logger.warn("Could not extract key from URL: $url. Using URL as key as fallback.")
            url
        }
    }
}
