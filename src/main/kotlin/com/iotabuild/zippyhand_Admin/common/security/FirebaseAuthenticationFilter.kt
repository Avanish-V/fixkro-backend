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
class FirebaseAuthenticationFilter : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        val header = request.getHeader("Authorization")

        if (header != null && header.startsWith("Bearer ")) {
            val idToken = header.substring(7)
            try {
                val decodedToken: FirebaseToken = FirebaseAuth.getInstance().verifyIdToken(idToken)
                
                val authentication = UsernamePasswordAuthenticationToken(
                    decodedToken, // Principal is the FirebaseToken
                    null,
                    listOf(SimpleGrantedAuthority("ROLE_USER"))
                )
                
                SecurityContextHolder.getContext().authentication = authentication
            } catch (e: Exception) {
                // Token invalid or expired
                SecurityContextHolder.clearContext()
            }
        }
        
        filterChain.doFilter(request, response)
    }
}
