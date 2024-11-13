package unical.enterpriceapplication.onlycards.application.controller;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import static org.springframework.http.HttpHeaders.AUTHORIZATION;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import unical.enterpriceapplication.onlycards.application.config.security.SecurityConstants;
import unical.enterpriceapplication.onlycards.application.core.service.AuthService;
import unical.enterpriceapplication.onlycards.application.data.service.UserService;
import unical.enterpriceapplication.onlycards.application.dto.AccountInfoDto;
import unical.enterpriceapplication.onlycards.application.dto.OauthRegistrationDto;
import unical.enterpriceapplication.onlycards.application.dto.RoleDto;
import unical.enterpriceapplication.onlycards.application.dto.UserDTO;
import unical.enterpriceapplication.onlycards.application.dto.UserRegistrationDto;
import unical.enterpriceapplication.onlycards.application.exception.ConflictException;
import unical.enterpriceapplication.onlycards.application.exception.MissingRequestHeaderException;
import unical.enterpriceapplication.onlycards.application.exception.ResourceNotFoundException;



@Controller
@RequestMapping(path = "/v1/auth", produces = "application/json")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    @Value("${app.front-ends}")
    private List<String> frontEnds;

    private final UserService userService;
    private final AuthService authService;

    @Operation(summary = "Refresh token", description = "A valid refresh token is required to get a new access token. The refresh token is sent in the header of the request. The new access token is sent in the response header.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "400", description = "Missing refresh token"),
            @ApiResponse(responseCode = "401", description = "Invalid refresh token"),
    })
    @SecurityRequirements()
    @GetMapping(SecurityConstants.REFRESH_TOKEN_URI_ENDING)
    @SneakyThrows
    public void refreshToken(HttpServletRequest request, HttpServletResponse response)  {
        String refreshToken = request.getHeader(SecurityConstants.HEADER_REFRESH_TOKEN);
        if(refreshToken != null ) {
            log.debug("Refresh token request: {}", refreshToken);
            Map<String, String> tokenMap = userService.refreshToken(refreshToken);
            if(tokenMap==null)
                throw new InsufficientAuthenticationException("Invalid refresh token");
            response.addHeader(AUTHORIZATION, SecurityConstants.BEARER_TOKEN_PREFIX + tokenMap.get("access_token"));
            response.addHeader(SecurityConstants.HEADER_REFRESH_TOKEN, tokenMap.get("refresh_token"));
        } else {
            throw new MissingRequestHeaderException(SecurityConstants.HEADER_REFRESH_TOKEN);
        }
    }
    @Operation(summary = "Login", description = "Login endpoint. The user is authenticated and a refresh and access token are generated. The tokens are send in the response header.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User logged in successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials"),
    })
    @PostMapping(value = SecurityConstants.LOGIN_URI_ENDING)
    @SecurityRequirements()
    public ResponseEntity<String> signIn(jakarta.servlet.http.HttpServletResponse ignored)  {
        log.debug("User {} logged in", SecurityContextHolder.getContext().getAuthentication().getName());
        return new ResponseEntity<>(null, HttpStatus.OK);

    }

    @GetMapping(path = "/oauth2/login/success",  produces = "text/html")
    @SneakyThrows
    public String OAuth2Success(@AuthenticationPrincipal Object user,  Model model)  {
        try{
           if(authService.getCurrentUserUUID() == null){
            OAuth2User oauth2User = (OAuth2User) user;
            model.addAttribute("email",oauth2User.getAttribute("email"));
            return "oauthRegistrationPage";
           }
            Optional<UserDTO> userDTO = userService.findById(authService.getCurrentUserUUID());
        
        if(userDTO.isPresent() && userDTO.get().isOauthUser()) {
            log.debug("User {} logged in", userDTO.get().getEmail());
            if(userDTO.get().isBlocked()){
                model.addAttribute("error", "You are blocked.");
                return "failureLoginPage";
            }
            model.addAttribute("user", userDTO.get().getEmail());
        model.addAttribute("roles", userDTO.get().getRoles().stream()
                .map(RoleDto::getName)
                .collect(Collectors.joining(",")));
        model.addAttribute("userId", userDTO.get().getId());
        model.addAttribute("frontEnds", frontEnds.stream()
                .map(Object::toString)
                .collect(Collectors.joining(",")));
                log.trace("list of data passed to the view: {}", model);
            return "successLoginPage";
        }else if(userDTO.isPresent() && !userDTO.get().isOauthUser()){
            model.addAttribute("error", "You have already used this email.");
            return "failureLoginPage";
        }else{
            model.addAttribute("email",userDTO.get().getEmail());
            return "oauthRegistrationPage";
        }
    

        }catch (Exception e){
            log.error("Error during OAuth2 login", e);
        model.addAttribute("error", "Please try again later.");
            return "failureLoginPage";
        }}
       



    @GetMapping(path = "/oauth2/login/failure",  produces = "text/html")
    @SecurityRequirements()
    @SneakyThrows
    public String OAuth2Failure( Model model)  {

        return "failureLoginPage";
    }
    @PostMapping("oauth2/users")
    public ResponseEntity<Void>  postMethodName(@RequestBody @Valid OauthRegistrationDto userRegistration, @AuthenticationPrincipal Object user, Model model)throws ConflictException {
        OAuth2User oauth2User = (OAuth2User) user;
        oauth2User.getAttributes().forEach((k,v)-> log.trace("Key: {}, Value: {}", k, v));

        log.debug("User {} try to log in", oauth2User.getAttributes().get("email"));
        Optional<UserDTO> userDTO = userService.findByEmail(oauth2User.getAttributes().get("email").toString());
        if(userDTO.isPresent()){
            throw new ConflictException("You have already used this email.");
        }
        Optional<UserDTO> userDTO2 = userService.findByUsername(userRegistration.getUsername());
        if(userDTO2.isPresent()){
            throw new ConflictException("You have already used this username.");
        }
        UserRegistrationDto userRegistrationDto = new UserRegistrationDto();
        userRegistrationDto.setEmail(oauth2User.getAttributes().get("email").toString());
        userRegistrationDto.setUsername(userRegistration.getUsername());
        userRegistrationDto.setPassword("passwordOauth2!");
        userService.saveUser(userRegistrationDto, true);
        return ResponseEntity.ok().build();
        }
        @Operation(summary = "get the info of a logged user", description = "Retrive the logged user info")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "User not found"),
    })
    
@GetMapping(path = "/credentials", produces = "application/json")
@SecurityRequirements()
public ResponseEntity<AccountInfoDto> getLoggedUserInfo(@AuthenticationPrincipal Object principal) throws ResourceNotFoundException {
    try {
        log.debug("Retrieving user info for principal: {}", principal);
        log.debug("type of principal: {}", principal.getClass().getName());
        // Handle the case where the user is authenticated via OAuth2 
        if (principal instanceof OAuth2User) {
            OAuth2User oauth2User = (OAuth2User) principal;
            logOAuth2UserAttributes(oauth2User); // Optional: Log the attributes for debugging
            
            String email = (String) oauth2User.getAttributes().get("email");
            Optional<UserDTO> user = userService.findByEmail(email);
            if (user.isEmpty()) {
                throw new ResourceNotFoundException(null, "User");
            }
            return ResponseEntity.ok(userService.getAccountInfoDto(user.get().getId()));
            
        // Handle the case where the user is authenticated with standard UserDetails
        } else if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            String username = userDetails.getUsername();
            log.debug("Username: {}", username);
            Optional<UserDTO> user = userService.findById(username);
            if (user.isEmpty()) {
                throw new ResourceNotFoundException(null, "User");
            }
            return ResponseEntity.ok(userService.getAccountInfoDto(user.get().getId()));
            
        } else {
            throw new ResourceNotFoundException(null, "User");
        }
    } catch (Exception e) {
        log.error("Error while retrieving user info", e);
        throw new ResourceNotFoundException(null, "User");
    }}

private void logOAuth2UserAttributes(OAuth2User oauth2User) {
    oauth2User.getAttributes().forEach((key, value) -> {
        log.trace("Key: {}, Value: {}", key, value);
    });}


    
 
    
    





}