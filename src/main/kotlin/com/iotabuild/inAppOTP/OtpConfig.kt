package com.iotabuild.inAppOTP

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.security.crypto.password.PasswordEncoder
import javax.crypto.spec.SecretKeySpec
import javax.crypto.SecretKey
import org.springframework.beans.factory.annotation.Value

@Configuration
class OtpConfig {

    @Bean
    fun passwordEncoder(): PasswordEncoder {
        return BCryptPasswordEncoder()
    }

    @Bean
    fun encryptionKey(
        @Value("\${otp.encryption.key:default_otp_encryption_key_32bytes_!!}") keyString: String
    ): SecretKey {
        val decodedKey = keyString.toByteArray()
        // Ensure it's exactly 32 bytes for AES-256
        val keyBytes = ByteArray(32)
        val copySize = minOf(decodedKey.size, 32)
        System.arraycopy(decodedKey, 0, keyBytes, 0, copySize)
        
        // If the key was shorter than 32 bytes, fill the rest with a deterministic pattern
        if (copySize < 32) {
            for (i in copySize until 32) {
                keyBytes[i] = (i % 256).toByte()
            }
        }
        
        return SecretKeySpec(keyBytes, "AES")
    }
}
