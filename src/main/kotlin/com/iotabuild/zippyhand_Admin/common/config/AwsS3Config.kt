package com.iotabuild.zippyhand_Admin.common.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.presigner.S3Presigner

@Configuration
class AwsS3Config(
    @Value("\${aws.s3.access-key}") private val accessKey: String,
    @Value("\${aws.s3.secret-key}") private val secretKey: String,
    @Value("\${aws.s3.region}") private val region: String
) {

    @Bean
    fun s3Client(): S3Client {
        val regionObj = Region.of(region)
        
        // Use DefaultCredentialsProvider for production (IAM Roles)
        // Fallback to StaticCredentialsProvider for local development
        val credentialsProvider = try {
            if (accessKey.isNotBlank() && secretKey.isNotBlank()) {
                StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey))
            } else {
                DefaultCredentialsProvider.create()
            }
        } catch (e: Exception) {
            DefaultCredentialsProvider.create()
        }

        return S3Client.builder()
            .region(regionObj)
            .credentialsProvider(credentialsProvider)
            .build()
    }

    @Bean
    fun s3Presigner(): S3Presigner {
        val regionObj = Region.of(region)
        val credentialsProvider = try {
            if (accessKey.isNotBlank() && secretKey.isNotBlank()) {
                StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey, secretKey))
            } else {
                DefaultCredentialsProvider.create()
            }
        } catch (e: Exception) {
            DefaultCredentialsProvider.create()
        }

        return S3Presigner.builder()
            .region(regionObj)
            .credentialsProvider(credentialsProvider)
            .build()
    }
}
