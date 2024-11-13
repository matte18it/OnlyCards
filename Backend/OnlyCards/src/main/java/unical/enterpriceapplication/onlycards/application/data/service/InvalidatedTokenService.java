package unical.enterpriceapplication.onlycards.application.data.service;

import java.time.LocalDateTime;
import java.util.UUID;

public interface InvalidatedTokenService {
    void invalidateToken(String token, LocalDateTime expiryDate, UUID userId);
    boolean isTokenInvalidated(String token);
}
