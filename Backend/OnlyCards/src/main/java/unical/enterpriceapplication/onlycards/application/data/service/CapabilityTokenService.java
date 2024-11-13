package unical.enterpriceapplication.onlycards.application.data.service;

import java.util.UUID;

public interface CapabilityTokenService {
    String generateToken(UUID wishlistId) ;
    void deleteToken(UUID wishlistId, String token);
    UUID getWishlistIdFromToken(String token) ;
}
