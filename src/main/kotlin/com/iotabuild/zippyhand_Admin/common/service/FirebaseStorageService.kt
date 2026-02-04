package com.iotabuild.zippyhand_Admin.common.service

import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.StorageOptions
import com.google.firebase.cloud.StorageClient
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.util.*

@Service
class FirebaseStorageService {

    fun uploadFile(file: MultipartFile, folder: String): String {
        val fileName = "${folder}/${UUID.randomUUID()}_${file.originalFilename}"
        val bucket = StorageClient.getInstance().bucket()
        
        val blob = bucket.create(fileName, file.inputStream, file.contentType)
        
        // Return the public URL
        // For Firebase Storage, the URL format is:
        // https://firebasestorage.googleapis.com/v0/b/[BUCKET_NAME]/o/[FILE_PATH]?alt=media
        val encodedPath = fileName.replace("/", "%2F")
        return "https://firebasestorage.googleapis.com/v0/b/${bucket.name}/o/${encodedPath}?alt=media"
    }
}
