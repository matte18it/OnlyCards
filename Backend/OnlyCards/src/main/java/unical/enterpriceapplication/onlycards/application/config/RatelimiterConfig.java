package unical.enterpriceapplication.onlycards.application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.google.common.util.concurrent.RateLimiter;

@Configuration
public class RatelimiterConfig {
    @Bean
    public RateLimiter rateLimiter() {
        // Limita a 10 richieste al secondo
        return RateLimiter.create(10);
    }
}
