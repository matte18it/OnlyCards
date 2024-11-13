package unical.enterpriceapplication.onlycards.application.config.security.filter;

import java.io.IOException;
import java.util.Date;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import unical.enterpriceapplication.onlycards.application.config.security.SecurityConstants;
import unical.enterpriceapplication.onlycards.application.config.security.TokenStore;
import unical.enterpriceapplication.onlycards.application.dto.ServiceError;
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    private final AuthenticationManager authenticationManager;
    @Value("${app.back-end}")
    private String backendUrl;
    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult)  {
        UserDetails user = (UserDetails)authResult.getPrincipal();
        String accessToken = TokenStore.getInstance().createAccessToken(user.getUsername(),backendUrl,
                user.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        String refreshToken = TokenStore.getInstance().createRefreshToken(user.getUsername(), backendUrl);
        log.debug("User {} authenticated successfully, generating JWT", user.getUsername());
        response.addHeader(AUTHORIZATION, SecurityConstants.BEARER_TOKEN_PREFIX + accessToken);
        response.addHeader(SecurityConstants.HEADER_REFRESH_TOKEN, refreshToken);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        ServiceError error = new ServiceError();
        error.setTimestamp(new Date());
        error.setMessage("Wrong credentials");
        error.setUrl(request.getRequestURI());

        response.getWriter().write(error.toJsonObject().toString());
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)  {
        String username;
        String password;
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if(authorizationHeader == null || !authorizationHeader.startsWith(SecurityConstants.BASIC_TOKEN_PREFIX)){
            log.debug("Authorization header not found in request: {}", request.getRequestURI());
            response.setContentType("application/json");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            ServiceError error = new ServiceError();
            error.setTimestamp(new Date());
            error.setMessage("Missing credentials");
            error.setUrl(request.getRequestURI());

            try {
                response.getWriter().write(error.toJsonObject().toString());
                return null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        String headerToken = StringUtils.delete(authorizationHeader, SecurityConstants.BASIC_TOKEN_PREFIX).trim();
        String[] credential = TokenStore.decodedBase64(headerToken);
        username = credential[0];
        password = credential[1];
        log.debug("User {} is trying to authenticate", username);
        return authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));

    }
    @Override
    @Autowired
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        super.setAuthenticationManager(authenticationManager);
    }
}
