package com.mathGame.app.security

import com.auth0.jwt.exceptions.JWTVerificationException
import com.auth0.jwt.interfaces.DecodedJWT
import com.mathGame.app.service.SupabaseJwtService
import jakarta.servlet.FilterChain
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import org.slf4j.LoggerFactory

@Component
class JwtAuthFilter(
    private val supabaseJwtService: SupabaseJwtService
) : OncePerRequestFilter() {
    private val logger = LoggerFactory.getLogger(JwtAuthFilter::class.java)

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            val authHeader = request.getHeader("Authorization")
            logger.debug("Processing request to: ${request.requestURI}")
            logger.debug("Authorization header: $authHeader")

            if ((authHeader != null && authHeader.startsWith("Bearer "))) {
                val token = authHeader.removePrefix("Bearer ").trim()
                try {
                    val decodedJWT = supabaseJwtService.verifyToken(token)
                    logger.debug("JWT verified successfully")

                    // Extract user info from claims
                    val userId = decodedJWT.getClaim("sub").asString()
                    val email = decodedJWT.getClaim("email").asString()
                    val role = decodedJWT.getClaim("role").asString() ?: "authenticated"
                    
                    logger.debug("User authenticated - ID: $userId, Email: $email, Role: $role")

                    val authorities = listOf(SimpleGrantedAuthority("ROLE_${role.uppercase()}"))
                    val authentication = UsernamePasswordAuthenticationToken(
                        userId,
                        null,
                        authorities
                    )
                    SecurityContextHolder.getContext().authentication = authentication
                    logger.debug("Security context updated with authorities: $authorities")

                } catch (e: JWTVerificationException) {
                    logger.error("JWT verification failed", e)
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid JWT token: ${e.message}")
                    return
                }
            } else {
                logger.debug("No Bearer token found in request")
            }

            filterChain.doFilter(request, response)
        } catch (e: Exception) {
            logger.error("Error processing request", e)
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error processing request")
            return
        }
    }
} 