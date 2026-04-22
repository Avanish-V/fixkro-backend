package com.iotabuild.zippyhand_Admin.common.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ClassPathResource
import java.io.InputStream

@Configuration
class FirebaseConfig {

    @Bean
    fun firebaseApp(): FirebaseApp {
        println("FirebaseConfig: Initializing Firebase App Bean...")
        val apps = FirebaseApp.getApps()
        if (apps.isNotEmpty()) {
            val existingApp = apps[0]
            println("FirebaseConfig: Firebase App already exists with project: ${existingApp.options.projectId}")
            if (existingApp.options.projectId != "fixkro-237ec") {
                println("FirebaseConfig: CRITICAL - Multi-project conflict! Existing project ID is ${existingApp.options.projectId}")
            }
            return existingApp
        }

        return try {
            // Use Spring's ClassPathResource which is more robust in AWS Lambda/JAR environments
            val resource = ClassPathResource("account.json")
            if (!resource.exists()) {
                println("FirebaseConfig: account.json NOT found via ClassPathResource")
                throw java.io.FileNotFoundException("account.json not found in classpath")
            }

            // Fail-safe: Set project ID as system property for the SDK
            System.setProperty("GOOGLE_CLOUD_PROJECT", "fixkro-237ec")
            
            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(resource.inputStream))
                .setProjectId("fixkro-237ec")
                .setStorageBucket("fixkro-237ec.firebasestorage.app")
                .build()
            
            val app = FirebaseApp.initializeApp(options)
            println("FirebaseConfig: Firebase App initialized successfully for project: ${app.options.projectId}")
            app
        } catch (e: Exception) {
            println("FirebaseConfig: Initialization failed: ${e.message}. Falling back...")
            System.setProperty("GOOGLE_CLOUD_PROJECT", "fixkro-237ec")
            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.getApplicationDefault())
                .setProjectId("fixkro-237ec")
                .setStorageBucket("fixkro-237ec.firebasestorage.app")
                .build()
            FirebaseApp.initializeApp(options)
        }
    }

    @Bean
    fun firebaseAuth(firebaseApp: FirebaseApp): FirebaseAuth {
        return FirebaseAuth.getInstance(firebaseApp)
    }

    @Bean
    fun firebaseMessaging(firebaseApp: FirebaseApp): FirebaseMessaging {
        return FirebaseMessaging.getInstance(firebaseApp)
    }
}
