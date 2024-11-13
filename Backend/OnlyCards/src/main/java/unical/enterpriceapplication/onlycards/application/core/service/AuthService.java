package unical.enterpriceapplication.onlycards.application.core.service;

import java.util.UUID;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import unical.enterpriceapplication.onlycards.application.config.security.LoggedUserDetails;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    public UUID getCurrentUserUUID() {
        UUID id = null;
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        log.trace("Principal: {}", principal);
        log.debug("Principal class: {}", principal.getClass());

        switch (principal) {
            case LoggedUserDetails userDetails -> id = UUID.fromString(userDetails.getUsername());
            
            default -> {
            }
        }

        log.debug("User: {}", id);
        return id;
    }

    public boolean getUserCorrespondsToPrincipal(UUID userId) {
        UUID currentUserId = getCurrentUserUUID();
        log.debug("Checking if user corresponds to principal, principal: {}, user: {}", currentUserId, userId);
        return currentUserId.equals(userId);
    }
}
