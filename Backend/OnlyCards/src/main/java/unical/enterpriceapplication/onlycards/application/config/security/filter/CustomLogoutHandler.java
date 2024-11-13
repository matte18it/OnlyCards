package unical.enterpriceapplication.onlycards.application.config.security.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import unical.enterpriceapplication.onlycards.application.config.security.SecurityConstants;
import unical.enterpriceapplication.onlycards.application.config.security.TokenStore;
import unical.enterpriceapplication.onlycards.application.core.service.GoogleUserInfoService;
import unical.enterpriceapplication.onlycards.application.data.service.InvalidatedTokenService;

import java.util.Map;
import java.util.UUID;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;
@Slf4j
public class CustomLogoutHandler implements LogoutHandler {
    @Autowired
    private  InvalidatedTokenService invalidatedTokenService;
    @Autowired
    private GoogleUserInfoService googleUserInfoService;
    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        if(authentication instanceof OAuth2AuthenticationToken) {
            log.debug("OAuth2AuthenticationToken found in logout request");
            log.trace("OAuth2AuthenticationToken : {}", authentication);
            return;
        }
        String token;
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        String refreshToken = request.getHeader(SecurityConstants.HEADER_REFRESH_TOKEN);
        // se non c'è header Authorization
        if ( authorizationHeader == null || (!authorizationHeader.startsWith(SecurityConstants.BASIC_TOKEN_PREFIX) && !authorizationHeader.startsWith(SecurityConstants.BEARER_TOKEN_PREFIX))) {
                log.debug("Authorization not found in logout request: {}", request.getRequestURI());
                throw new InsufficientAuthenticationException("User is not authenticated");

        }
        if(refreshToken!=null){
            Map<String, String> refreshTokenClaims = TokenStore.getInstance().getClaims(refreshToken);
            if(refreshTokenClaims != null) {
                log.debug("Invalidating refresh token: {}", refreshToken);
                invalidatedTokenService.invalidateToken(refreshToken, TokenStore.getInstance().getLocalDateFromClaim(refreshTokenClaims.get("exp")), UUID.fromString(refreshTokenClaims.get("sub")));
            }
        }
        if(authorizationHeader.startsWith(SecurityConstants.BEARER_TOKEN_PREFIX) ){
            token = authorizationHeader.substring(SecurityConstants.BEARER_TOKEN_PREFIX.length());
            if(googleUserInfoService.isGoogleAccessToken(token)){
                log.debug("Google access token found in logout request");
                log.trace("Google access token: {}", token);
                googleUserInfoService.invalidateAccessToken(token);
                return;
            }else{
            Map<String, String> claims = TokenStore.getInstance().getClaims(token);
                if(claims != null) {
                    log.debug("Invalidating token: {}", token);
                    invalidatedTokenService.invalidateToken(token, TokenStore.getInstance().getLocalDateFromClaim(claims.get("exp")), UUID.fromString(claims.get("sub")));
                }}

            
        }else
            throw new BadCredentialsException("User is not authenticated");

    }
}
