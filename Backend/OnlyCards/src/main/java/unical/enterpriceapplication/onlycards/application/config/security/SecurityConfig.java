package unical.enterpriceapplication.onlycards.application.config.security;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.HttpStatusReturningLogoutSuccessHandler;
import org.springframework.web.cors.CorsConfiguration;
import unical.enterpriceapplication.onlycards.application.config.security.filter.*;
import unical.enterpriceapplication.onlycards.application.dto.ServiceError;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig{
    @Value("${app.front-ends}")
    private List<String> frontEnds;




  
    private final AuthenticationConfiguration authenticationConfiguration;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        customAuthenticationFilter().setFilterProcessesUrl("/v1/auth/login");
        log.debug("Configuring security filter chain");
        return httpSecurity
                .csrf(AbstractHttpConfigurer::disable)
                .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint((request, response, authException) ->{
                        response.setContentType("application/json");
                        response.setStatus(HttpStatus.UNAUTHORIZED.value());
                         ServiceError error = new ServiceError();
                        error.setTimestamp(new Date());
                        error.setMessage(authException.getMessage());
                        error.setUrl(request.getRequestURI());
                response.getWriter().write(error.toJsonObject().toString());}
                ))
                .cors(cors -> cors.configurationSource(request -> {
                    var corsConfiguration = new CorsConfiguration();
                    corsConfiguration.setAllowedOrigins(frontEnds);
                    corsConfiguration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
                    corsConfiguration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "Accept", SecurityConstants.HEADER_REFRESH_TOKEN));
                    corsConfiguration.setMaxAge(3600L);
                    corsConfiguration.setAllowCredentials(true);
                    corsConfiguration.setExposedHeaders(List.of("Access-Control-Allow-Origin", "Access-Control-Allow-Credentials", "Authorization", SecurityConstants.HEADER_REFRESH_TOKEN, "Access-Control-Request-Method", "Access-Control-Request-Headers"));
                    return corsConfiguration;
                }))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.POST, "/v1/auth"+SecurityConstants.LOGIN_URI_ENDING).permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/auth"+SecurityConstants.REFRESH_TOKEN_URI_ENDING).permitAll()
                        .requestMatchers(HttpMethod.POST, "/v1/users").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/auth/oauth2/credentials").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/product-types/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/swagger.html").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/products/info/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/auth/oauth2/login/failure").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/wishlists/token/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/auth/captcha/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/users/*/public-wishlists").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/users/*/public-wishlists/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/static/**").permitAll()   
                        .requestMatchers(HttpMethod.GET, "/v1/users/*").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/users/*/products").permitAll()
                        .requestMatchers(HttpMethod.GET, "/v1/files/*").permitAll()
                        .anyRequest().authenticated()
                )
                .addFilter(customAuthenticationFilter())
                .addFilterBefore(customAuthorizationFiler(), UsernamePasswordAuthenticationFilter.class)
                .headers(headers -> headers.cacheControl(HeadersConfigurer.CacheControlConfig::disable))
                .oauth2Login(login -> login.defaultSuccessUrl("/v1/auth/oauth2/login/success")
                        .failureUrl("/v1/auth/oauth2/login/failure"))
                .logout(logout -> logout.logoutUrl("/v1/auth/logout")
                        .addLogoutHandler(customLogoutHandler())
                        .logoutSuccessHandler((new HttpStatusReturningLogoutSuccessHandler(HttpStatus.OK)))
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                )
                .build();
    }

    @Bean
     PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    @Bean
     CustomAuthorizationFiler customAuthorizationFiler() {
        return new CustomAuthorizationFiler();
    }

    @Bean
     CustomLogoutHandler customLogoutHandler() {
        return new CustomLogoutHandler();
    }
    @Bean
    public AuthenticationManager authManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }


    @Bean
    CustomAuthenticationFilter customAuthenticationFilter() throws Exception {
        return new CustomAuthenticationFilter(authManager());
    }
}