package br.app.coeur.core.config.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RateLimitingFilter implements Filter {

    private final Map<String, TokenBucket> ipBuckets = new ConcurrentHashMap<>();
    private static final long MAX_TOKENS = 30;
    private static final long REFILL_PERIOD_SECONDS = 60;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest httpRequest && response instanceof HttpServletResponse httpResponse) {
            String ip = getClientIp(httpRequest);

            TokenBucket bucket = ipBuckets.computeIfAbsent(ip, k -> new TokenBucket(MAX_TOKENS, REFILL_PERIOD_SECONDS));

            if (!bucket.tryConsume()) {
                httpResponse.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
                httpResponse.setContentType("application/json");
                httpResponse.setCharacterEncoding("UTF-8");
                httpResponse.getWriter().write("{\"error\": \"Excesso de requisições. Rate limit excedido. Tente novamente em instantes.\"}");
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }

    private static class TokenBucket {
        private final long capacity;
        private final long refillPeriodSeconds;
        private double tokens;
        private Instant lastRefill;

        public TokenBucket(long capacity, long refillPeriodSeconds) {
            this.capacity = capacity;
            this.refillPeriodSeconds = refillPeriodSeconds;
            this.tokens = capacity;
            this.lastRefill = Instant.now();
        }

        public synchronized boolean tryConsume() {
            refill();
            if (tokens >= 1.0) {
                tokens -= 1.0;
                return true;
            }
            return false;
        }

        private void refill() {
            Instant now = Instant.now();
            double elapsedSeconds = Duration.between(lastRefill, now).toMillis() / 1000.0;
            double tokensToAdd = elapsedSeconds * ((double) capacity / refillPeriodSeconds);
            if (tokensToAdd > 0) {
                tokens = Math.min(capacity, tokens + tokensToAdd);
                lastRefill = now;
            }
        }
    }
}
