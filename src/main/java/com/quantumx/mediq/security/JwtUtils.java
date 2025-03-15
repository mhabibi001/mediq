package com.quantumx.mediq.security;

import com.quantumx.mediq.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Date;
import java.util.function.Function;

@Component
public class JwtUtils {

    private final String secretKey;
    private final long expirationMs;

    public JwtUtils(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.expirationMs}") long expirationMs) {
        this.secretKey = secretKey;
        this.expirationMs = expirationMs;
    }

    public String generateToken(User user) {
        Date issuedAt = new Date();
        Date expiryDate = new Date(issuedAt.getTime() + expirationMs);

        System.out.println("🔑 Generating JWT for: " + user.getUsername());
        System.out.println("🕒 Expiry Time: " + expiryDate);

        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("role", user.getRole())
                .claim("firstName", user.getFirstName())  // ✅ Include first name
                .claim("lastName", user.getLastName())    // ✅ Include last name
                .claim("forcePasswordChange", user.isForcePasswordChange())  // ✅ Include this
                .setIssuedAt(issuedAt)
                .setExpiration(expiryDate)
                .signWith(SignatureAlgorithm.HS256, secretKey.getBytes())
                .compact();
    }


    public boolean validateToken(String token, String username) {
        return username.equals(extractUsername(token)) && !isTokenExpired(token);
    }

    public String extractUserRole(String token) {
        String role = extractClaim(token, claims -> claims.get("role", String.class));
        System.out.println("Extracted Role from Token: " + role);
        return role;
    }


    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public boolean isTokenExpired(String token) {
        try {
            Date expiryDate = extractExpiration(token);
            boolean isExpired = expiryDate.before(new Date());

            if (isExpired) {
                System.err.println("🚨 JWT Token has expired! Expired at: " + expiryDate);
            }
            return isExpired;
        } catch (Exception e) {
            System.err.println("❌ Error while checking JWT expiry: " + e.getMessage());
            return true; // Assume expired if there's an error
        }
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(secretKey.getBytes())
                .setAllowedClockSkewSeconds(300)  // Allow 60 seconds of clock skew
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }
}
