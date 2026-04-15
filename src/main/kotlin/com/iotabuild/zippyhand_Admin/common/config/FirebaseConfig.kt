package com.iotabuild.zippyhand_Admin.common.config

import com.google.auth.oauth2.GoogleCredentials
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessaging
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.InputStream

@Configuration
class FirebaseConfig {

    @Bean
    fun firebaseApp(): FirebaseApp {
        println("FirebaseConfig: Initializing Firebase App Bean...")
        val apps = FirebaseApp.getApps()
        if (apps.isNotEmpty()) {
            println("FirebaseConfig: Firebase App already exists, returning existing instance")
            return apps[0]
        }

        return try {
            val serviceAccount = this::class.java.classLoader.getResourceAsStream("account.json")
            
            if (serviceAccount == null) {
                println("FirebaseConfig: account.json not found in classpath!")
                throw RuntimeException("account.json not found in classpath")
            }
            
            val options = FirebaseOptions.builder()
                .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                .setProjectId("fixkro-237ec")
                .setStorageBucket("fixkro-237ec.firebasestorage.app")
                .build()
            
            val app = FirebaseApp.initializeApp(options)
            println("FirebaseConfig: Firebase App [DEFAULT] initialized successfully with project fixkro-237ec")
            app
        } catch (e: Exception) {
            println("FirebaseConfig: Failed to initialize with account.json: ${e.message}")
            // Fallback to application default
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
