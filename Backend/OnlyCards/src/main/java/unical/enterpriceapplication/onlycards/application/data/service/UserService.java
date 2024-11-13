package unical.enterpriceapplication.onlycards.application.data.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import unical.enterpriceapplication.onlycards.application.data.entities.accounts.User;
import unical.enterpriceapplication.onlycards.application.dto.AccountInfoDto;
import unical.enterpriceapplication.onlycards.application.dto.RoleDto;
import unical.enterpriceapplication.onlycards.application.dto.UserDTO;
import unical.enterpriceapplication.onlycards.application.dto.UserLoginDto;
import unical.enterpriceapplication.onlycards.application.dto.UserPublicProfileDto;
import unical.enterpriceapplication.onlycards.application.dto.UserRegistrationDto;

public interface UserService {
    Optional<UserLoginDto> findByIdLogin(UUID id);
    UserDTO updateUser(UUID userId, UserDTO userDTO);
    Optional<UserDTO> findByEmail(String email);
    Page<UserDTO> searchUsersByUsernameOrEmail(String username, String email, Pageable pageable);
    Optional<UserDTO> findById(UUID id);
    Optional<UserDTO> findByUsername(String username);
    UserDTO saveUser(UserRegistrationDto userDto, boolean isOauthUser);
    void deleteUserById(UUID id);
    Optional<UserDTO> getUserById(UUID id);
    Optional<UserLoginDto> findByEmailLogin(String username);
    Optional<UserLoginDto> findByUsernameLogin(String username);
    Map<String, String> refreshToken(String refreshToken);
    Optional<UserDTO> findById(String subject);
    boolean existsById(UUID id);
    Set<RoleDto> findRolesByUserId(UUID sub);
    Set<RoleDto> findRolesByUserEmail(String email);
    List<User> getAllUsersAdmin();
    boolean isBlocked(UUID userId);
    Optional<UserDTO> getUserByUsername(String username);
    Page<UserDTO> getAllUsers(Pageable pageable);
    AccountInfoDto getAccountInfoDto(UUID id);
    String getEmail(UUID id);
    boolean isOauthUser(UUID fromString);
    String generateAUniqueUsername(String string);
    Optional<UserPublicProfileDto> findByUsernamePublicProfile(String username);
    boolean isOnlyAdmin(String username);
    List<RoleDto> getUserRoles(UUID userId);

}
