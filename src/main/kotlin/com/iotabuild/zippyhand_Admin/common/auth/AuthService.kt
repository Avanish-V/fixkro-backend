package com.iotabuild.zippyhand_Admin.common.auth

import com.fasterxml.jackson.databind.ObjectMapper
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class AuthService(
    @Value("\${supabase.url:https://pifgeycbjxluaubdhmhh.supabase.co}") private val supabaseUrl: String,
    @Value("\${supabase.anon-key:}") private val anonKey: String,
    private val objectMapper: ObjectMapper
) {
    private val client = OkHttpClient()

    fun login(request: LoginRequest): LoginResponse {
        val url = "$supabaseUrl/auth/v1/token?grant_type=password"
        println("AuthService: Attempting login at $url")
        println("AuthService: Using API Key starting with: ${anonKey.take(10)}...")
        
        val bodyMap = mapOf(
            "email" to request.email,
            "password" to request.password
        )
        val jsonBody = objectMapper.writeValueAsString(bodyMap)
        
        val httpRequest = Request.Builder()
            .url(url)
            .addHeader("apikey", anonKey)
            .addHeader("Authorization", "Bearer $anonKey")
            .post(jsonBody.toRequestBody("application/json".toMediaType()))
            .build()

        client.newCall(httpRequest).execute().use { response ->
            if (!response.isSuccessful) {
                throw RuntimeException("Login failed: ${response.body?.string()}")
            }

            val responseBody = response.body?.string() ?: throw RuntimeException("Empty response from Supabase")
            val jsonNode = objectMapper.readTree(responseBody)
            
            return LoginResponse(
                accessToken = jsonNode["access_token"].asText(),
                expiresIn = jsonNode["expires_in"].asLong(),
                user = UserInfo(
                    id = jsonNode["user"]["id"].asText(),
                    email = jsonNode["user"]["email"].asText()
                )
            )
        }
    }
}
