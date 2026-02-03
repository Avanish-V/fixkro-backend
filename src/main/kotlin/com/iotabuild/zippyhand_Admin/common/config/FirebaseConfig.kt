package com.iotabuild.zippyhand_Admin.common.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import org.springframework.context.annotation.Configuration
import java.io.FileInputStream
import javax.annotation.PostConstruct

@Configuration
class FirebaseConfig {

    @PostConstruct
    fun initialize() {
        if (FirebaseApp.getApps().isEmpty()) {
            try {
                // Try from local file first (for development)
                val serviceAccount = FileInputStream("src/main/resources/account.json")
                val options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build()
                FirebaseApp.initializeApp(options)
            } catch (e: Exception) {
                // Fallback to default credentials (for production/cloud environments)
                try {
                    val options = FirebaseOptions.builder()
                        .setCredentials(GoogleCredentials.getApplicationDefault())
                        .build()
                    FirebaseApp.initializeApp(options)
                } catch (ex: Exception) {
                    println("Firebase could not be initialized. Make sure you have provided service-account.json or set GOOGLE_APPLICATION_CREDENTIALS: ${ex.message}")
                }
            }
        }
    }
}
