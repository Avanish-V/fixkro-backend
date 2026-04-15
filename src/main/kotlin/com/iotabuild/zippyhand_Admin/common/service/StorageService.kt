package com.iotabuild.zippyhand_Admin.common.service

import org.springframework.web.multipart.MultipartFile

interface StorageService {
    /**
     * Uploads a file to the storage provider.
     * @param file The file to upload
     * @param folder The folder path within the storage
     * @return The public URL of the uploaded file
     */
    fun uploadFile(file: MultipartFile, folder: String): String

    /**
     * Deletes a file from storage provider.
     * @param fileUrl The full URL of the file to delete
     */
    fun deleteFile(fileUrl: String)

    /**
     * Generates a pre-signed URL for temporary access to a private file.
     * @param fileName The key/path of the file
     * @param expirationMinutes Minutes until the link expires
     * @return The pre-signed URL
     */
    fun generatePresignedUrl(fileName: String, expirationMinutes: Long = 15): String
    
    /**
     * Generates a pre-signed URL for client-side direct upload to S3 (Bypassing Lambda binary limits).
     * @param folder The target folder
     * @param extension The file extension (e.g. .jpg)
     * @return A map containing 'uploadUrl' and 'publicUrl'
     */
    fun generatePresignedPutUrl(folder: String, extension: String, contentType: String): Map<String, String>
}
