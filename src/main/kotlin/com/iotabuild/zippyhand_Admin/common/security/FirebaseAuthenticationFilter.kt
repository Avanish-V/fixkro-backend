package com.iotabuild.zippyhand_Admin.common.security

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseToken
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter

@Component
class FirebaseAuthenticationFilter(private val firebaseAuth: FirebaseAuth) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7).trim()
            
            // Optimization: If already authenticated (e.g. by Supabase), skip
            if (SecurityContextHolder.getContext().authentication != null) {
                filterChain.doFilter(request, response)
                return
            }

            try {
                // Peek at the header to see if it's potentially a Firebase token (RS256)
                val parts = token.split(".")
                if (parts.size != 3) {
                    filterChain.doFilter(request, response)
                    return
                }

                val header = String(java.util.Base64.getDecoder().decode(parts[0]))
                if (!header.contains("RS256")) {
                    // Not a Firebase token, skip
                    filterChain.doFilter(request, response)
                    return
                }

                val decodedToken: FirebaseToken = firebaseAuth.verifyIdToken(token)
                
                val authorities = listOf(SimpleGrantedAuthority("ROLE_USER"))
                // Set the FirebaseToken object as the principal so it can be injected via @AuthenticationPrincipal
                val authentication = UsernamePasswordAuthenticationToken(decodedToken, null, authorities)
                SecurityContextHolder.getContext().authentication = authentication
            } catch (e: Exception) {
                // Only log if it really looked like a Firebase token but failed
                println("Firebase Auth Failed: ${e.message}")
            }
        }
        
        filterChain.doFilter(request, response)
    }
}
