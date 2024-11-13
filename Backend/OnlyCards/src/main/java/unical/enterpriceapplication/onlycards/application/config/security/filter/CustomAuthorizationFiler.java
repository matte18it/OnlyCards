package unical.enterpriceapplication.onlycards.application.config.security.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import unical.enterpriceapplication.onlycards.application.config.security.LoggedUserDetailsService;
import unical.enterpriceapplication.onlycards.application.config.security.SecurityConstants;
import unical.enterpriceapplication.onlycards.application.config.security.TokenStore;
import unical.enterpriceapplication.onlycards.application.core.service.GoogleUserInfoService;
import unical.enterpriceapplication.onlycards.application.data.service.InvalidatedTokenService;
import unical.enterpriceapplication.onlycards.application.data.service.UserService;
import unical.enterpriceapplication.onlycards.application.dto.RoleDto;
import unical.enterpriceapplication.onlycards.application.dto.ServiceError;
import unical.enterpriceapplication.onlycards.application.dto.UserDTO;
import unical.enterpriceapplication.onlycards.application.dto.UserRegistrationDto;

@Slf4j
@Component
public class CustomAuthorizationFiler extends OncePerRequestFilter {
    @Autowired
    private LoggedUserDetailsService loggedUserDetailsService;
    @Autowired
    private InvalidatedTokenService invalidatedTokenService;
    @Autowired
    private UserService userService;
    @Autowired
    private GoogleUserInfoService googleUserInfoService;
    @Value("${app.back-end}")
    private String backEndUrl;



    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain) throws ServletException, IOException, InsufficientAuthenticationException, AccessDeniedException {
        String uri = request.getRequestURI();
        String token;
        String authorizationHeader = request.getHeader(AUTHORIZATION);

        // se non c'è header Authorization
        if (authorizationHeader == null || (!authorizationHeader.startsWith(SecurityConstants.BASIC_TOKEN_PREFIX) && !authorizationHeader.startsWith(SecurityConstants.BEARER_TOKEN_PREFIX))) {
            if(SecurityContextHolder.getContext().getAuthentication() == null) {
                filterChain.doFilter(request, response);
                return;
            }
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if(principal instanceof OAuth2User oAuth2User) {
            String email = oAuth2User.getAttribute("email");
            log.debug("User {} is authenticated",email);
            List<SimpleGrantedAuthority> authorities = new ArrayList<>();
            Set<RoleDto> roles = userService.findRolesByUserEmail(email);
            if(roles.isEmpty()) {
                log.debug("User has no roles");
                filterChain.doFilter(request, response);
                return;
            }
            roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(role.getName())));
            SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(loggedUserDetailsService.loadUserByUsername(email), null, authorities));

            }
            
            filterChain.doFilter(request, response);
            return;
        }

        if ((authorizationHeader.startsWith(SecurityConstants.BASIC_TOKEN_PREFIX) && uri.endsWith(SecurityConstants.LOGIN_URI_ENDING))) {
            log.debug("Request for login or refresh token: {}", uri);
            filterChain.doFilter(request, response);
            return;
        }

        // se header Authorization inizia con il prefisso Bearer e la richiesta non è per il login
        if(authorizationHeader.startsWith(SecurityConstants.BEARER_TOKEN_PREFIX) && !uri.endsWith(SecurityConstants.LOGIN_URI_ENDING)) {
            try {
            log.debug("Request for resource with bearer token: {}", uri);
             token = authorizationHeader.substring(SecurityConstants.BEARER_TOKEN_PREFIX.length());
            if (googleUserInfoService.isGoogleAccessToken(token)) {
                log.debug("Google access token");
                Map<String, Object> userInfo = googleUserInfoService.fetchGoogleUserInfo(authorizationHeader.substring(SecurityConstants.BEARER_TOKEN_PREFIX.length()));
                if (userInfo == null) {
                    log.debug("Google access token is invalid");
                    throw new InsufficientAuthenticationException("Google access token is invalid");
                }
                log.debug("all user info: {}", userInfo);
                Optional<UserDTO> user = userService.findByEmail((String) userInfo.get("email"));
                log.trace("User: {}", user);
                if (user.isEmpty()) { //save the new user
                    UserRegistrationDto userRegistrationDto = new UserRegistrationDto();
                    userRegistrationDto.setEmail((String) userInfo.get("email"));
                    userRegistrationDto.setUsername(userService.generateAUniqueUsername((String) userInfo.get("email")));
                    userRegistrationDto.setPassword("passwordOauth2!");
                    userService.saveUser(userRegistrationDto, true);
                    user = userService.findByEmail((String) userInfo.get("email"));
                    log.debug("New user saved: {}", user.get().getEmail());

                }
                    if(loggedUserDetailsService.isBlocked(user.get().getId())) {
                        log.info("User is blocked");
                        throw new AccessDeniedException("User is blocked");
                    }
                    if(!loggedUserDetailsService.isOauthUser(user.get().getId())) {
                        log.info("User must login with username and password");
                        throw new AccessDeniedException("User must login with username and password");
                    }
               
                log.debug("User {} is authenticated", user.get().getEmail());
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                Set<RoleDto> roles = userService.findRolesByUserId(user.get().getId());
                roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(role.getName())));
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(loggedUserDetailsService.loadUserByUsername(user.get().getUsername()), null, authorities));

                filterChain.doFilter(request, response);
                return;
            } 

            
                token = authorizationHeader.substring(SecurityConstants.BEARER_TOKEN_PREFIX.length());
                Map<String, String> tokenMap = TokenStore.getInstance().getClaims(token);
                if(TokenStore.getInstance().isTokenInvalid(token)) {
                    log.debug("Token is invalid");
                    throw new InsufficientAuthenticationException("Token is invalid");
                }
                //check the issuer
                if(!tokenMap.get("iss").equals(backEndUrl)) {
                    log.debug("Token issuer is invalid");
                    throw new InsufficientAuthenticationException("Token issuer is invalid");
                }
              
                if(loggedUserDetailsService.isBlocked(UUID.fromString(tokenMap.get("sub")))) {
                    log.info("User is blocked");
                    throw new AccessDeniedException("User is blocked");
                }
                if(loggedUserDetailsService.isOauthUser(UUID.fromString(tokenMap.get("sub")))) {
                    log.info("User must login with oauth");
                    throw new AccessDeniedException("User must login with oauth");
                }
                //check if the roles correspond to the user
                if(loggedUserDetailsService.findRolesByUserId(UUID.fromString(tokenMap.get("sub"))).stream().map(RoleDto::getName).noneMatch(tokenMap.get("roles")::contains)) {
                    throw new InsufficientAuthenticationException("Token is invalid");
                }
                //check if the token is invalidated
                if(invalidatedTokenService.isTokenInvalidated(token)) {
                    throw new InsufficientAuthenticationException("Token is already invalidated");
                }

                log.debug("User {} is authenticated", tokenMap.get("sub"));
                Optional <UserDTO> user = userService.findById(UUID.fromString(tokenMap.get("sub")));
                if(user.isEmpty()) {
                    throw new InsufficientAuthenticationException("Token sub is invalid");
                }
                List<SimpleGrantedAuthority> authorities = new ArrayList<>();
                Set<RoleDto> roles = userService.findRolesByUserId(user.get().getId());
                roles.forEach(role -> authorities.add(new SimpleGrantedAuthority(role.getName())));
                SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(loggedUserDetailsService.loadUserByUsername(user.get().getUsername()), null, authorities));

            } catch (Exception e) {
                response.setContentType("application/json");
                if(e instanceof AccessDeniedException)
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                else
                    response.setStatus(HttpStatus.UNAUTHORIZED.value());
                ServiceError error = new ServiceError();
                error.setTimestamp(new Date());
                error.setMessage("Invalid authentication");
                error.setUrl(request.getRequestURI());
                response.getWriter().write(error.toJsonObject().toString());
                log.trace("Error in authentication", e);
                return;
            }
            filterChain.doFilter(request, response);

        } else {
            filterChain.doFilter(request, response);
        }


    }

}

