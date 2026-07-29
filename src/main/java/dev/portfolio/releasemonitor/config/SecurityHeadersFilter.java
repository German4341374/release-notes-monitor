package dev.portfolio.releasemonitor.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("X-Frame-Options", "DENY");
        response.setHeader("Referrer-Policy", "no-referrer");
        String contentSecurityPolicy = request.getRequestURI().startsWith("/swagger-ui")
                ? "default-src 'self'; style-src 'self' 'unsafe-inline'; "
                        + "script-src 'self' 'unsafe-inline'; img-src 'self' data:"
                : "default-src 'self'; style-src 'self'; script-src 'self'; img-src 'self' data:";
        response.setHeader("Content-Security-Policy", contentSecurityPolicy);
        filterChain.doFilter(request, response);
    }
}
