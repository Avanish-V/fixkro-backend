package com.iotabuild.zippyhand_Admin.common.controller

import com.iotabuild.zippyhand_Admin.common.response.ApiResponse
import com.iotabuild.zippyhand_Admin.common.service.FirebaseStorageService
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/api/v1/files")
class FileUploadController(private val storageService: FirebaseStorageService) {

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
}
