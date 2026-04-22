package com.iotabuild.zippyhand_Admin.common.security

import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.nio.charset.StandardCharsets

@Component
class SupabaseAuthenticationFilter(
    @Value("\${supabase.jwt-secret}") private val jwtSecret: String
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val authHeader = request.getHeader("Authorization")

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            val token = authHeader.substring(7).trim()
            
            // Optimization: If already authenticated, skip
            if (SecurityContextHolder.getContext().authentication != null) {
                filterChain.doFilter(request, response)
                return
            }

            try {
                // Peek at the header to see if it's potentially a Supabase token (HS256)
                // Firebase tokens use RS256
                val parts = token.split(".")
                if (parts.size != 3) {
                    filterChain.doFilter(request, response)
                    return
                }

                val header = String(java.util.Base64.getUrlDecoder().decode(parts[0]))
                if (!header.contains("HS256")) {
                    // Not a Supabase token, skip to next filter
                    filterChain.doFilter(request, response)
                    return
                }

                // Supabase uses HS256 with the JWT secret
                val key = Keys.hmacShaKeyFor(jwtSecret.toByteArray(StandardCharsets.UTF_8))
                
                val claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .payload
                
                val email = claims["email"] as? String
                val role = claims["role"] as? String 

                if (email != null) {
                    val authorities = mutableListOf(SimpleGrantedAuthority("ROLE_AUTHENTICATED"))
                    
                    // Supabase tokens from admin panel are typically 'authenticated'
                    if (role == "authenticated") {
                        authorities.add(SimpleGrantedAuthority("ROLE_ADMIN"))
                    }

                    val authentication = UsernamePasswordAuthenticationToken(email, null, authorities)
                    SecurityContextHolder.getContext().authentication = authentication
                }
            } catch (e: Exception) {
                // Only log if it really looked like a Supabase token but failed
                println("Supabase Auth Failed: ${e.message}")
            }
        }

        filterChain.doFilter(request, response)
    }
}
