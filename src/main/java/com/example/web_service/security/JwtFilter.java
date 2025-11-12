package com.example.web_service.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.web_service.dto.Response;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Collections;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    private final Key SECRET_KEY = Keys.hmacShaKeyFor("supersecretkeysupersecretkey1234".getBytes(StandardCharsets.UTF_8));
    private final ObjectMapper objectMapper = new ObjectMapper();  

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                   FilterChain filterChain) throws ServletException, IOException {

        String path = request.getRequestURI();
        if (path.startsWith("/api/auth/") || path.startsWith("/api/uploads/") || request.getMethod().equalsIgnoreCase("OPTIONS")) {
            filterChain.doFilter(request, response);
            return;
        }
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            // sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token tidak ditemukan");
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED,Response.failedResponse(HttpStatus.UNAUTHORIZED.value(), "Token tidak ditemukan"));
            return;
        }
        String token = header.substring(7);
        if (jwtUtil.isTokenRevoked(token)) {
            // sendError(response, HttpServletResponse.SC_FORBIDDEN, "Token sudah di-logout");
            sendError(response, HttpServletResponse.SC_FORBIDDEN,Response.failedResponse(HttpStatus.FORBIDDEN.value(), "Token sudah di-logout"));
            return;
        }
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(SECRET_KEY)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String username = claims.getSubject();
            if (username == null) {
                // sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token tidak valid");
                sendError(response, HttpServletResponse.SC_UNAUTHORIZED,Response.failedResponse(HttpStatus.UNAUTHORIZED.value(), "Token tidak valid"));
                
                return;
            }
            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(new User(username, "", Collections.emptyList()), null, Collections.emptyList());
            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception e) {
            // sendError(response, HttpServletResponse.SC_UNAUTHORIZED, "Token tidak valid atau sudah kedaluwarsa");
            sendError(response, HttpServletResponse.SC_UNAUTHORIZED,Response.failedResponse(HttpStatus.UNAUTHORIZED.value(), "Token tidak valid atau sudah kedaluwarsa"));
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void sendError(HttpServletResponse response, int status, Response message) throws IOException {
        response.setStatus(status);
        response.setContentType("application/json");
        String json = objectMapper.writeValueAsString(message);
        try (PrintWriter writer = response.getWriter()) {
            writer.write(json);
            writer.flush();
        }
        
    }
}
