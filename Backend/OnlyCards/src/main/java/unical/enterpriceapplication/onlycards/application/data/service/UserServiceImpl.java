package unical.enterpriceapplication.onlycards.application.data.service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import unical.enterpriceapplication.onlycards.application.config.security.LoggedUserDetails;
import unical.enterpriceapplication.onlycards.application.config.security.TokenStore;
import unical.enterpriceapplication.onlycards.application.core.service.FirebaseFolders;
import unical.enterpriceapplication.onlycards.application.core.service.FirebaseStorageService;
import unical.enterpriceapplication.onlycards.application.data.entities.accounts.Role;
import unical.enterpriceapplication.onlycards.application.data.entities.accounts.User;
import unical.enterpriceapplication.onlycards.application.data.entities.accounts.Wallet;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Money;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Product;
import unical.enterpriceapplication.onlycards.application.data.entities.orders.Orders;
import unical.enterpriceapplication.onlycards.application.data.entities.orders.Status;
import unical.enterpriceapplication.onlycards.application.data.entities.wishlist.UserWishlist;
import unical.enterpriceapplication.onlycards.application.data.entities.wishlist.Wishlist;
import unical.enterpriceapplication.onlycards.application.data.entities.wishlist.WishlistOwnership;
import unical.enterpriceapplication.onlycards.application.data.repository.CartRepository;
import unical.enterpriceapplication.onlycards.application.data.repository.OrdersRepository;
import unical.enterpriceapplication.onlycards.application.data.repository.ProductRepository;
import unical.enterpriceapplication.onlycards.application.data.repository.RoleRepository;
import unical.enterpriceapplication.onlycards.application.data.repository.UserRepository;
import unical.enterpriceapplication.onlycards.application.data.repository.WishlistRepository;
import unical.enterpriceapplication.onlycards.application.dto.AccountInfoDto;
import unical.enterpriceapplication.onlycards.application.dto.RoleDto;
import unical.enterpriceapplication.onlycards.application.dto.UserDTO;
import unical.enterpriceapplication.onlycards.application.dto.UserLoginDto;
import unical.enterpriceapplication.onlycards.application.dto.UserPublicProfileDto;
import unical.enterpriceapplication.onlycards.application.dto.UserRegistrationDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserServiceImpl implements UserService {
     @Value("${app.back-end}")
    private String backendUrl;
     // Regular expression to match valid usernames
     private static final Pattern USERNAME_PATTERN = Pattern.compile("^[a-zA-Z0-9_]*$");
    
     // Constants for username length
     private static final int MIN_USERNAME_LENGTH = 3;
     private static final int MAX_USERNAME_LENGTH = 20;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final ProductRepository productRepository;
    private final WishlistRepository wishlistRepository;
    private final CartRepository cartRepository;
    private final OrdersRepository ordersRepository;
    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleService roleService;
    private final WalletService walletService;
    private final OrdersService ordersService;
    private final FirebaseStorageService firebaseStorageService;





    @Override
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        Page<User> users = userRepository.findAll(pageable);
        return users.map(user -> modelMapper.map(user, UserDTO.class));
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> findByEmail(String email){
        Optional<User> user = userRepository.findByEmail(email);
        return user.map(u -> modelMapper.map(u, UserDTO.class));
    }


    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> findById(UUID id){
        Optional<User> user = userRepository.findById(id);
        return user.map(u -> modelMapper.map(u, UserDTO.class));
    }

    @Override
    public Optional<UserDTO> findByUsername(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.map(u -> modelMapper.map(u, UserDTO.class));
    }

    @Override
    public Page<UserDTO> searchUsersByUsernameOrEmail(String username, String email, Pageable pageable) {
        Page<User> users;

        if (username != null && !username.isEmpty() && email != null && !email.isEmpty()) {
            // Cerca per entrambi i criteri contemporaneamente
            users = userRepository.findByUsernameContainingIgnoreCaseAndEmailContainingIgnoreCase(username, email, pageable);
        } else if (username != null && !username.isEmpty()) {
            users = userRepository.findByUsernameContainingIgnoreCase(username, pageable);
        } else {
            users = userRepository.findByEmailContainingIgnoreCase(email, pageable);
        }

        // Mappa i risultati trovati a UserDTO
        return users.map(user -> modelMapper.map(user, UserDTO.class));
    }


    @Override
    public UserDTO saveUser(@Valid UserRegistrationDto userDto, boolean isOauthUser) {
        User userEntity = modelMapper.map(userDto, User.class);
        userEntity.setPassword(passwordEncoder.encode(userDto.getPassword()));
        userEntity.setOauthUser(isOauthUser);
        Optional<Role> role = roleService.findByName("ROLE_BUYER");
        Optional<Role> role2 = roleService.findByName("ROLE_SELLER");
        if(role.isPresent() && role2.isPresent()){
            userEntity.setRoles(new HashSet<>(Arrays.asList(role.get(), role2.get())));
        }
        User user = userRepository.save(userEntity);

        // Creo il wallet
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(new Money());
        walletService.save(wallet);

        return modelMapper.map(user, UserDTO.class);
    }

    @Transactional
    @Override
    public UserDTO updateUser(UUID userId, UserDTO userDTO) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (userDTO.getUsername() != null) {
            user.setUsername(userDTO.getUsername());
        }
        if (userDTO.getEmail() != null) {
            user.setEmail(userDTO.getEmail());
        }
        if (userDTO.getCellphoneNumber() != null) {
            user.setCellphoneNumber(userDTO.getCellphoneNumber());
        }
        user.setBlocked(userDTO.isBlocked());

        if (userDTO.getRoles() != null) {
            updateRoles(user, userDTO.getRoles());
        }


        userRepository.save(user);
        return modelMapper.map(user, UserDTO.class);
    }

    private void updateRoles(User user, Set<RoleDto> roleDTOs) {
        Set<Role> roles = new HashSet<>();
        boolean isAdminRolePresent = roleDTOs.stream()
                .anyMatch(roleDto -> "ROLE_ADMIN".equals(roleDto.getName()));

        if (isAdminRolePresent) {
            // Se è presente il ruolo Admin, assegna solo questo ruolo e ignora gli altri
            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("Role not found: ROLE_ADMIN"));
            roles.add(adminRole);
        } else {
            // Altrimenti, aggiungi tutti i ruoli senza restrizioni
            for (RoleDto roleDto : roleDTOs) {
                Role role = roleRepository.findByName(roleDto.getName())
                        .orElseThrow(() -> new RuntimeException("Role not found: " + roleDto.getName()));
                roles.add(role);
            }
        }

        user.setRoles(roles);
    }


    @Transactional
    @Override
    public void deleteUserById(UUID userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        deleteUnsoldProducts(user);         // Elimina i prodotti non venduti
        deleteUserWishlists(user);          // Gestisce la rimozione delle wishlist
        deleteUserCart(user);               // Elimina il carrello dell'utente
        handleUserOrders(user);             // Gestisce gli ordini dell'utente
        firebaseStorageService.deleteFile(user.getId().toString(), FirebaseFolders.USERS_PROFILE);  // Elimina l'immagine del profilo

        userRepository.delete(user);
    }

    private void deleteUnsoldProducts(User user) {
        List<Product> productsToRemove = user.getProducts().stream()
                .filter(product -> !product.isSold())
                .collect(Collectors.toList());
        productRepository.deleteAll(productsToRemove);
    }

    private void deleteUserWishlists(User user) {
        for (UserWishlist userWishlist : user.getUserWishlists()) {
            Wishlist wishlist = userWishlist.getWishlist();
            if (wishlist != null && userWishlist.getOwnership()==WishlistOwnership.OWNER) {
                wishlistRepository.delete(wishlist);  // Elimina la wishlist se l'utente è l'owner
            }
        }
    }

    private void deleteUserCart(User user) {
        if (user.getCart() != null) {
            cartRepository.delete(user.getCart());  // Elimina direttamente il carrello
        }
    }

    private void handleUserOrders(User user) {
        List<Orders> orders = ordersRepository.findByUserId(user.getId());

        for (Orders order : orders) {
            if (order.getStatus() == Status.PENDING) {
                ordersService.deleteAllTransactions(order.getTransactions());  // Elimina le transazioni
                ordersRepository.delete(order);  // Elimina l'ordine in elaborazione
            } else if (order.getStatus() == Status.SHIPPED || order.getStatus() == Status.DELIVERED) {
                order.setUser(null);
                ordersRepository.save(order);
            }
        }
    }




    @Override
    public Optional<UserDTO> getUserById(UUID id) {
        Optional<User> user = userRepository.findById(id);
        return user.map(u -> modelMapper.map(u, UserDTO.class));
    }




 

    @Override
    @Transactional(readOnly = true)
    public Optional<UserLoginDto> findByEmailLogin(String email) {
        Optional<User> user = userRepository.findByEmail(email);
        return user.map(u -> modelMapper.map(u, UserLoginDto.class));
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserLoginDto> findByUsernameLogin(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        return user.map(u -> modelMapper.map(u, UserLoginDto.class));
    }
    @Override
    @Transactional(readOnly = true)
    @SneakyThrows
    public Map<String, String> refreshToken(String refreshToken) {
        if(TokenStore.getInstance().isTokenInvalid(refreshToken) )
            return null;
        Map<String, Object> claims = TokenStore.getInstance().parseTokenToSignedJWT(refreshToken).getJWTClaimsSet().getClaims();
        // issuer check
        String username = (String) claims.get("sub");
        log.debug("Refreshing token for user: {}", username);
        Optional<UserLoginDto> userO = findByIdLogin(UUID.fromString(username));
        if(userO.isEmpty())
            return null;
        UserLoginDto user = userO.get();

        LoggedUserDetails loggedUserDetails = new LoggedUserDetails(user);
        String accessToken = TokenStore.getInstance().createAccessToken(loggedUserDetails.getUsername(),backendUrl, 
                loggedUserDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()));
        return Map.of("access_token", accessToken, "refresh_token", refreshToken);

    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserDTO> findById(String subject) {
        return userRepository.findById(UUID.fromString(subject)).map(u -> modelMapper.map(u, UserDTO.class));
    }
    @Override
    public Optional<UserLoginDto> findByIdLogin(UUID id) {
        return userRepository.findById(id).map(u -> modelMapper.map(u, UserLoginDto.class));
    }



    @Override
    public boolean existsById(UUID id) {
        return userRepository.existsById(id);
    }

    @Override
    @Transactional(readOnly = true)
public Set<RoleDto> findRolesByUserId(UUID sub) {
    List<Role> roles = roleRepository.findByUsersId(sub);
    return roles.stream().map(r -> new RoleDto(r.getName())).collect(Collectors.toSet());
}
@Override
@Transactional(readOnly = true)
public Set<RoleDto> findRolesByUserEmail(String sub) {
    List<Role> roles = roleRepository.findByUsersEmail(sub);
    return roles.stream().map(r -> new RoleDto(r.getName())).collect(Collectors.toSet());
}

    @Override
    public List<User> getAllUsersAdmin() {
        return userRepository.findAllByRolesContaining(roleService.findByName("ROLE_ADMIN").get());
    }

    @Override
    public boolean isBlocked(UUID userId) {
        return userRepository.findById(userId).map(User::getBlocked).orElse(false);
    }

    @Override
    public Optional<UserDTO> getUserByUsername(String username) {
        return userRepository.findByUsername(username).map(u -> modelMapper.map(u, UserDTO.class));
    }

    @Override
    public AccountInfoDto getAccountInfoDto(UUID id) {
        Optional<User> user = userRepository.findById(id);
        if(user.isEmpty())
            return null;
        AccountInfoDto accountOauthDto = new AccountInfoDto();
        accountOauthDto.setId(user.get().getId());
        accountOauthDto.setRoles(user.get().getRoles().stream().map(r -> modelMapper.map(r, RoleDto.class)).collect(Collectors.toList()));
        return accountOauthDto;
    }

    @Override
    public String getEmail(UUID id) {
        return userRepository.findById(id).map(User::getEmail).orElse(null);
    }


    @Override
    public boolean isOauthUser(UUID userId) {
        return userRepository.findById(userId).map(User::getOauthUser).orElse(false);
    }


    @Override
    public String generateAUniqueUsername(String baseUsername) {
        // Step 1: Normalize the base username to ensure it respects the constraints
        String validUsername = normalizeUsername(baseUsername);
        
        // Step 2: Append random numbers to make it unique
        String uniqueUsername = validUsername;
        Random random = new Random();
        
        while (userRepository.existsByUsername(uniqueUsername)) {
            // Append random numbers while ensuring the username doesn't exceed the max length
            uniqueUsername = validUsername + random.nextInt(9999);  // Append up to 4 random digits
            
            // Ensure the length is still within bounds
            if (uniqueUsername.length() > MAX_USERNAME_LENGTH) {
                uniqueUsername = uniqueUsername.substring(0, MAX_USERNAME_LENGTH-10);
            }
        }
        
        return uniqueUsername;
    }
    
    // Normalize the username to meet length and pattern constraints
    private String normalizeUsername(String username) {
        // Step 1: Remove invalid characters
        StringBuilder validUsernameBuilder = new StringBuilder();
        
        for (char c : username.toCharArray()) {
            if (USERNAME_PATTERN.matcher(Character.toString(c)).matches()) {
                validUsernameBuilder.append(c);
            }
        }
        
        // Step 2: Ensure the username has valid length (truncate if necessary)
        String validUsername = validUsernameBuilder.toString();
        if (validUsername.length() > MAX_USERNAME_LENGTH) {
            validUsername = validUsername.substring(0, MAX_USERNAME_LENGTH);
        }
        
        // Step 3: Pad the username if it's too short
        if (validUsername.length() < MIN_USERNAME_LENGTH) {
            validUsername = padUsername(validUsername);
        }
        
        return validUsername;
    }
    
    // Pad the username if it's too short
    private String padUsername(String username) {
        StringBuilder paddedUsername = new StringBuilder(username);
        while (paddedUsername.length() < MIN_USERNAME_LENGTH) {
            paddedUsername.append("0");  // Append zeros to meet the minimum length
        }
        return paddedUsername.toString();
    }


    @Override
    public Optional<UserPublicProfileDto> findByUsernamePublicProfile(String username) {
        Optional<User> user = userRepository.findByUsername(username);
        if(user.isEmpty())
            return Optional.empty();
        UserPublicProfileDto userPublicProfileDto = new UserPublicProfileDto();
        userPublicProfileDto.setUsername(user.get().getUsername());
        userPublicProfileDto.setProfileImage(backendUrl + "/v1/files/" + user.get().getId());
        return Optional.of(userPublicProfileDto);
    }


    @Override
    public boolean isOnlyAdmin(String username) {
         // Recupera l'utente tramite il repository o un servizio che fornisce gli utenti
    Optional<User> user = userRepository.findByUsername(username);
    
    // Se l'utente non esiste, restituisci false
    if (user.isEmpty()) {
        return false;
    }
    
    // Ottieni tutti i ruoli dell'utente
    Set<Role> roles = user.get().getRoles();
    
    // Verifica se c'è solo un ruolo e se questo è 'ROLE_ADMIN'
    if (roles.size() == 1) {
        Role role = roles.iterator().next(); // ottieni l'unico ruolo
        return role.getName().equals("ROLE_ADMIN");
    }
    
    // Se ci sono più ruoli o nessuno, restituisci false
    return false;
      
    }



    @Override
    public List<RoleDto> getUserRoles(UUID userId) {
        // Trova l'utente nel database
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Mappa i ruoli dell'utente in RoleDto e restituisci una lista
        return user.getRoles().stream()
                .map(role -> new RoleDto(role.getName()))
                .collect(Collectors.toList());  // Usa Collectors.toList() per restituire una lista
    }

}
