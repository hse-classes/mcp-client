package kmo.dev;

import java.io.IOException;
import java.time.Duration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Protects the two things worth abusing: password guessing on /login and the
 * Gemini key behind /api/chat. Limits are per client IP.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimiter loginLimiter = new RateLimiter(10, Duration.ofMinutes(5));
    private final RateLimiter chatLimiter = new RateLimiter(20, Duration.ofMinutes(1));

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !"POST".equals(request.getMethod());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        RateLimiter limiter = switch (request.getRequestURI()) {
            case "/login" -> loginLimiter;
            case "/api/chat" -> chatLimiter;
            default -> null;
        };
        // Behind Traefik, server.forward-headers-strategy makes this the real client IP.
        if (limiter != null && !limiter.tryAcquire(request.getRemoteAddr())) {
            // setStatus, not sendError: sendError forwards to /error, which is behind the
            // login and would turn the 429 into a redirect.
            response.setStatus(429);
            response.setContentType("text/plain;charset=UTF-8");
            response.getWriter().write("Zu viele Anfragen, bitte kurz warten.");
            return;
        }
        chain.doFilter(request, response);
    }
}
