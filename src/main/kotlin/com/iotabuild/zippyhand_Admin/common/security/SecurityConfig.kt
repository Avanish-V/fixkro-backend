package com.iotabuild.zippyhand_Admin.common.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource
import org.springframework.security.config.Customizer

@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val firebaseAuthenticationFilter: FirebaseAuthenticationFilter,
    private val supabaseAuthenticationFilter: SupabaseAuthenticationFilter
) {

    @Bean
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors(Customizer.withDefaults())
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    // 1. PUBLIC ENDPOINTS
                    .requestMatchers("/api/v1/payments/webhook").permitAll()
                    .requestMatchers("/api/v1/payments/health").permitAll()
                    .requestMatchers("/api/v1/auth/**").permitAll() // Supabase Login
                    .requestMatchers("/api/v1/cities/active").permitAll()
                    .requestMatchers("/api/v1/hello").permitAll()
                    
                    // Public GET access for catalogs (Mobile app needs these without auth or with optional auth)
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/categories").permitAll()
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/services/**").permitAll()
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/product/**").permitAll()
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/offers/**").permitAll()
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/coupons/**").permitAll()
                    
                    // 2. ADMIN ENDPOINTS (Supabase Auth -> ROLE_ADMIN)
                    .requestMatchers("/api/v1/dashboard/**").hasRole("ADMIN")
                    .requestMatchers("/api/v1/professionals/**").hasRole("ADMIN")
                    .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/coupons/**").hasRole("ADMIN")
                    .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/v1/coupons/**").hasRole("ADMIN")
                    .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/v1/coupons/**").hasRole("ADMIN")
                    .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/offers/**").hasRole("ADMIN")
                    .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/v1/offers/**").hasRole("ADMIN")
                    .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/v1/offers/**").hasRole("ADMIN")
                    .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/v1/offers/**").hasRole("ADMIN")
                    .requestMatchers("/api/v1/order/all").hasRole("ADMIN")
                    .requestMatchers("/api/v1/order/*/status").hasRole("ADMIN")
                    .requestMatchers("/api/v1/order/*/assign-technician").hasRole("ADMIN")
                    
                    // Write operations for products and cities are ADMIN only
                    .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/cities/**", "/api/v1/category/**", "/api/v1/service/**", "/api/v1/files/**").hasRole("ADMIN")
                    .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/v1/cities/**", "/api/v1/category/**", "/api/v1/product/**").hasRole("ADMIN")
                    .requestMatchers(org.springframework.http.HttpMethod.PATCH, "/api/v1/cities/**", "/api/v1/category/**", "/api/v1/product/**").hasRole("ADMIN")
                    .requestMatchers(org.springframework.http.HttpMethod.DELETE, "/api/v1/cities/**", "/api/v1/category/**", "/api/v1/product/**", "/api/v1/files/**").hasRole("ADMIN")
                    .requestMatchers(org.springframework.http.HttpMethod.GET, "/api/v1/files/**").hasRole("ADMIN")

                    // 3. MOBILE/CLIENT ENDPOINTS (Firebase Auth -> ROLE_USER)
                    .requestMatchers("/api/v1/users/register").hasRole("USER")
                    .requestMatchers("/api/v1/addresses/**").hasRole("USER")
                    .requestMatchers("/api/v1/pricing/**").hasRole("USER")
                    .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/v1/order").hasRole("USER")
                    .requestMatchers("/api/v1/order/my-orders").hasRole("USER")
                    .requestMatchers("/api/v1/order/*/cancel").hasRole("USER")
                    
                    // Individual order view can be accessed by either (Owner as USER or Admin)
                    .requestMatchers("/api/v1/order/*").hasAnyRole("ADMIN", "USER")
                    
                    // Default behavior
                    .anyRequest().authenticated()
            }
            .addFilterBefore(supabaseAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            .addFilterBefore(firebaseAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()
        configuration.allowedOrigins = listOf(
            "http://localhost:5173", 
            "http://localhost:3000",
            "https://homecare-hub-six.vercel.app",
            "https://admin.fixkro.in"
        )
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        configuration.allowedHeaders = listOf("*")
        configuration.allowCredentials = true
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }
}
