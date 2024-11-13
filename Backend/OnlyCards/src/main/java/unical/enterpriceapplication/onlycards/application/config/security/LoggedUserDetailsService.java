package unical.enterpriceapplication.onlycards.application.config.security;

import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import unical.enterpriceapplication.onlycards.application.data.service.UserService;
import unical.enterpriceapplication.onlycards.application.dto.RoleDto;
import unical.enterpriceapplication.onlycards.application.dto.UserLoginDto;

@Service
@Slf4j
@RequiredArgsConstructor
public class LoggedUserDetailsService implements UserDetailsService {
    private UserService userService;
    @Override
    @SneakyThrows
    public UserDetails loadUserByUsername(String username)  {
        UserLoginDto userLoginDto;
        if(username.contains("@"))
            userLoginDto = userService.findByEmailLogin(username).orElseThrow(() -> new UsernameNotFoundException("user not found"));
        else
            userLoginDto = userService.findByUsernameLogin(username).orElseThrow(() -> new UsernameNotFoundException("user not found"));
        log.debug("user role: {}", userLoginDto.getRoles());
        return new LoggedUserDetails(userLoginDto);
    }
    @Autowired
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

    public boolean existsById(UUID userId) {
        return userService.existsById(userId);
    }

    public Set<RoleDto> findRolesByUserId(UUID userId) {
        return userService.findRolesByUserId(userId);
    }

    public boolean isBlocked(UUID userId) {
        return userService.isBlocked(userId);
    }
    public boolean isOauthUser(UUID fromString) {
        return userService.isOauthUser(fromString);
    }
}
