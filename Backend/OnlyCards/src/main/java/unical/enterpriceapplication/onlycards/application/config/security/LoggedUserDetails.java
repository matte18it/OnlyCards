package unical.enterpriceapplication.onlycards.application.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import unical.enterpriceapplication.onlycards.application.dto.UserLoginDto;

import java.io.Serial;
import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;
@Slf4j
public class LoggedUserDetails implements UserDetails {
    @Serial
    private static final long serialVersionUID = 1;
    private final String id;
    private final String password;
    private final Boolean blocked;
    private final Set<GrantedAuthority> auths;
    public LoggedUserDetails(UserLoginDto userLoginDto) {
        this.id = userLoginDto.getId().toString();
        this.password = userLoginDto.getPassword();
        this.auths = userLoginDto.getRoles().stream().map(roleDto -> new SimpleGrantedAuthority(roleDto.getName())).collect(Collectors.toSet());
        log.debug("user role: {}", this.auths);
        this.blocked = userLoginDto.getBlocked();
    }
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return auths;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return id;
    }

    @Override
    public boolean isAccountNonExpired() {
        return false;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !blocked;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return false;
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
