package unical.enterpriceapplication.onlycards.application.config.security.filter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import unical.enterpriceapplication.onlycards.application.config.security.LoggedUserDetailsService;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomAuthenticationProvider implements AuthenticationProvider {
    private final LoggedUserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;
    @Override
    public Authentication authenticate(Authentication authentication)  {
        if (authentication == null) {
            return null;
        }

        String userName = String.valueOf(authentication.getName());
        String userPassword = String.valueOf(authentication.getCredentials());
        log.debug("User {} is trying to authenticate", userName);

        UserDetails userDetails;
        UsernamePasswordAuthenticationToken authToken = null;

        userDetails = userDetailsService.loadUserByUsername(userName);

        if (passwordEncoder.matches(userPassword, userDetails.getPassword()) && userDetails.isAccountNonLocked()) {
            log.debug("User {} authenticated with basic", userName);
            authToken= new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        } return authToken;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return authentication.equals(UsernamePasswordAuthenticationToken.class);
    }
}
