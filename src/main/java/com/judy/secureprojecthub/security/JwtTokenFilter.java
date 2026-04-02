package com.judy.secureprojecthub.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class JwtTokenFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenFilter.class);
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    
    private final SecretKey secretKey;

    public JwtTokenFilter(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        System.out.println("🔵 JwtTokenFilter.doFilterInternal() called");
        System.out.println("   Path: " + request.getRequestURI());
        System.out.println("   Authorization header: " + (authHeader != null ? "PRESENT" : "ABSENT"));
        
        if (authHeader != null && authHeader.startsWith(BEARER_PREFIX)) {
            String token = authHeader.substring(BEARER_PREFIX.length());
            System.out.println("   Token found. Length: " + token.length());

            try {
                System.out.println("   Attempting to parse JWT token...");
                Claims claims = Jwts.parser()
                        .verifyWith(secretKey)
                        .build()
                        .parseSignedClaims(token)
                        .getPayload();

                String username = claims.getSubject();
                System.out.println("   ✅ Token parsed successfully! Username: " + username);
                
                @SuppressWarnings("unchecked")
                List<Map<String, String>> rolesList = (List<Map<String, String>>) claims.get("roles");

                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                if (rolesList != null) {
                    for (Map<String, String> role : rolesList) {
                        String authority = role.get("authority");
                        if (authority != null) {
                            authorities.add(new SimpleGrantedAuthority(authority));
                            System.out.println("   Added authority: " + authority);
                        }
                    }
                }

                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(authentication);
                System.out.println("   ✅ Authentication set in SecurityContext");

            } catch (Exception e) {
                System.out.println("   ❌ JWT token validation FAILED: " + e.getMessage());
                e.printStackTrace();
                logger.debug("JWT token validation failed: " + e.getMessage());
                SecurityContextHolder.clearContext();
            }
        } else {
            System.out.println("   No Bearer token found");
        }

        filterChain.doFilter(request, response);
    }
}