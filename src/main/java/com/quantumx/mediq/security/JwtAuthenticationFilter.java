package com.quantumx.mediq.security;

import com.quantumx.mediq.service.CustomUserDetailsService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtils jwtUtils;
    private final CustomUserDetailsService userDetailsService; // Use CustomUserDetailsService

    // ✅ Define public endpoints that do NOT require authentication
    private static final List<String> PUBLIC_ENDPOINTS = List.of(
            "/api/auth/register",
            "/api/auth/login",
            "/api/education-levels",
            "/api/timezones",
            "/api/exam-categories"
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String requestPath = request.getRequestURI();
        System.out.println("🔍 JWT Authentication Filter triggered for: " + requestPath);

        // ✅ Skip authentication for public endpoints
        if (PUBLIC_ENDPOINTS.contains(requestPath)) {
            System.out.println("✅ Skipping authentication for: " + requestPath);
            filterChain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.err.println("🚨 Missing or Invalid Authorization header");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Missing or Invalid Authorization header.");
            return;
        }

        String token = authHeader.substring(7);
        String username = null;

        try {
            username = jwtUtils.extractUsername(token);
        } catch (ExpiredJwtException e) {
            System.err.println("❌ JWT Expired: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "JWT Token has expired. Please login again.");
            return;
        } catch (Exception e) {
            System.err.println("❌ Error parsing JWT Token: " + e.getMessage());
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid Token.");
            return;
        }

        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            if (jwtUtils.validateToken(token, userDetails.getUsername())) {
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        filterChain.doFilter(request, response);
    }
}
