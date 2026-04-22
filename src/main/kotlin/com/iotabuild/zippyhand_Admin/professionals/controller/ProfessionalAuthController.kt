package com.iotabuild.zippyhand_Admin.professionals.controller

import com.google.firebase.auth.FirebaseAuth
import com.iotabuild.zippyhand_Admin.professionals.dto.AuthRequest
import com.iotabuild.zippyhand_Admin.professionals.dto.AuthResponse
import com.iotabuild.zippyhand_Admin.professionals.service.MobileProfessionalService
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/auth") // Standardizing to v1
class ProfessionalAuthController(
    private val mobileProfessionalService: MobileProfessionalService,
    private val firebaseAuth: FirebaseAuth
) {
    private val logger = LoggerFactory.getLogger(ProfessionalAuthController::class.java)

    @PostMapping("/check")
    fun checkUser(@RequestBody request: AuthRequest): ResponseEntity<AuthResponse> {
        if (request.idToken.isBlank()) {
            logger.warn("ProfessionalAuthController: Received blank ID Token")
            return ResponseEntity.status(401).build()
        }

        return try {
            val decodedToken = firebaseAuth.verifyIdToken(request.idToken)
            val uid = decodedToken.uid
            val email = decodedToken.email
            val name = (decodedToken.claims["name"] as? String) ?: "Professional"
            
            val professional = mobileProfessionalService.checkAndRegister(uid, email, name)
            
            ResponseEntity.ok(AuthResponse(
                uid = professional.firebaseUid,
                status = professional.status ?: "INCOMPLETE",
                isNewUser = professional.status == "INCOMPLETE"
            ))
        } catch (e: Exception) {
            logger.error("ProfessionalAuthController: Auth Error", e)
            ResponseEntity.status(401).build()
        }
    }
}
