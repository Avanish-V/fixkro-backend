package com.iotabuild.inAppOTP

import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import java.security.SecureRandom
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.Base64
import javax.crypto.Cipher
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class EncryptedOtp(
    var otpHash         : String?  = null,
    var otpEncrypted    : String?  = null,
    var otpExpiresAt    : Instant? = null,
)


@Service
class OtpService(
    private val passwordEncoder: PasswordEncoder,
    private val encryptionKey: SecretKey,
    private val otpRepository: OtpRepository
) {

    fun generateAndSaveOtp(orderId: String): EncryptedOtp {
        // First delete any existing OTP for this order to\ ensure fresh state
        otpRepository.deleteByOrderId(orderId)

        val plainOtp = (100000..999999).random().toString()
        val otpHash = passwordEncoder.encode(plainOtp)
        val otpEncrypted = encrypt(plainOtp)
        val otpExpireAt = Instant.now().plus(36500, ChronoUnit.DAYS) // Effectively no expiry (100 years)

        val otpRecord = OtpRecord(
            orderId = orderId,
            otpHash = otpHash,
            otpEncrypted = otpEncrypted,
            expiresAt = otpExpireAt
        )
        
        otpRepository.save(otpRecord)

        return EncryptedOtp(
            otpHash = otpHash,
            otpEncrypted = otpEncrypted,
            otpExpiresAt = otpExpireAt
        )
    }

    fun getOtpRecord(orderId: String): OtpRecord? {
        return otpRepository.findByOrderId(orderId)
    }

    fun deleteOtp(orderId: String) {
        otpRepository.deleteByOrderId(orderId)
    }

    fun decryptOtpForCustomer(orderId: String): String? {
        val record = otpRepository.findByOrderId(orderId) ?: return null
        return decrypt(record.otpEncrypted)
    }

//    fun decryptForCustomer(assignment: JobAssignment): String {
//        val encrypted = assignment.otpEncrypted
//            ?: throw OtpExpiredException("OTP already used")
//        return decrypt(encrypted)
//    }

    private fun encrypt(plain: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv     = ByteArray(12).also { SecureRandom().nextBytes(it) }
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey, GCMParameterSpec(128, iv))
        val encrypted = cipher.doFinal(plain.toByteArray())
        // Store iv + ciphertext together, base64 encoded
        return Base64.getEncoder().encodeToString(iv + encrypted)
    }

    private fun decrypt(encoded: String): String {
        val bytes     = Base64.getDecoder().decode(encoded)
        val iv        = bytes.sliceArray(0..11)
        val encrypted = bytes.sliceArray(12..bytes.lastIndex)
        val cipher    = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, encryptionKey, GCMParameterSpec(128, iv))
        return String(cipher.doFinal(encrypted))
    }
}