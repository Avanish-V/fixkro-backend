package com.iotabuild.zippyhand_Admin.common.controller

import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import com.iotabuild.zippyhand_Admin.common.service.S3StorageService
import com.iotabuild.zippyhand_Admin.common.service.StorageService
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/files")
class FileUploadController(private val storageService: StorageService) {

    @PostMapping("/upload")
    fun uploadFile(
        @RequestParam("file") file: MultipartFile,
        @RequestParam("folder", defaultValue = "general") folder: String
    ): ApiResponse<String> {
        return try {
            val url = storageService.uploadFile(file, folder)
            ApiResponse.success(url)
        } catch (e: Exception) {
            ApiResponse.error("Failed to upload file: ${e.message}")
        }
    }

    @DeleteMapping("/delete")
    fun deleteFile(@RequestParam("url") url: String): ApiResponse<String> {
        return try {
            storageService.deleteFile(url)
            ApiResponse.success("File deleted successfully")
        } catch (e: Exception) {
            ApiResponse.error("Failed to delete file: ${e.message}")
        }
    }

    @GetMapping("/presigned-url")
    fun getPresignedUrl(
        @RequestParam("fileName") fileName: String,
        @RequestParam("expiration", defaultValue = "15") expiration: Long
    ): ApiResponse<String> {
        return try {
            val url = storageService.generatePresignedUrl(fileName, expiration)
            ApiResponse.success(url)
        } catch (e: Exception) {
            ApiResponse.error("Failed to generate presigned URL: ${e.message}")
        }
    }
}
