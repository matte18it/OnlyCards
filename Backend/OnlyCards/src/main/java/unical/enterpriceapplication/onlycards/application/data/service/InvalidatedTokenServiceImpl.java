package unical.enterpriceapplication.onlycards.application.data.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import unical.enterpriceapplication.onlycards.application.data.entities.InvalidatedToken;
import unical.enterpriceapplication.onlycards.application.data.entities.accounts.User;
import unical.enterpriceapplication.onlycards.application.data.repository.InvalidatedTokenRepository;
import unical.enterpriceapplication.onlycards.application.data.repository.UserRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class InvalidatedTokenServiceImpl implements InvalidatedTokenService{
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final UserRepository userRepository;

    @Override
    public void invalidateToken(String token, LocalDateTime expiryDate, UUID id) {
        InvalidatedToken invalidatedToken = new InvalidatedToken();
        invalidatedToken.setToken(token);
        invalidatedToken.setExpiryDate(expiryDate);
        Optional<User> account = userRepository.findById(id);
        if(account.isEmpty())
            return;
        invalidatedToken.setUser(account.get());
        invalidatedTokenRepository.save(invalidatedToken);

    }

    @Override
    public boolean isTokenInvalidated(String token) {
        return invalidatedTokenRepository.existsByToken(token);
    }
}
