package unical.enterpriceapplication.onlycards.application.core.service;

import java.util.Base64;
import java.util.Map;

import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class GoogleUserInfoService {
    private final RestTemplate restTemplate;
    public GoogleUserInfoService() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(5000); // Connection timeout (5 seconds)
        requestFactory.setReadTimeout(5000); // Read timeout (5 seconds)
        this.restTemplate = new RestTemplate(requestFactory);
    }
    private final String tokenUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
    private final String revokeUrl = "https://oauth2.googleapis.com/revoke";
        public Map<String, Object> fetchGoogleUserInfo(String accessToken) {
        try {
            // Construct the URL with query parameters
            String url = UriComponentsBuilder.fromHttpUrl(tokenUrl)
                    .queryParam("access_token", accessToken)
                    .toUriString();

            // Make the request to the Google API
            ParameterizedTypeReference<Map<String, Object>> responseType = 
            new ParameterizedTypeReference<Map<String, Object>>() {};
            ResponseEntity<Map<String, Object>> response = restTemplate.exchange(
                url, 
                HttpMethod.GET, 
                null, 
                responseType
            );

            // Check the status code and return the body if successful
            if (response.getStatusCode() == HttpStatus.OK) {
                return response.getBody();
            } else {
                log.error("Failed to fetch Google user info. Status Code: {}", response.getStatusCode());
                return null;
            }
        } catch (Exception e) {
            log.error("Error fetching Google user info", e.getMessage());
            log.trace(e.toString());
            return null;
        }
    }
    public boolean isGoogleAccessToken(String token) {
        if (token == null || token.isEmpty()) {
            return false;  // Invalid token
        }

        // Split the token by dots to check if it follows the JWT format
        String[] tokenParts = token.split("\\.");

        // A valid JWT must have exactly 3 parts (header, payload, signature)
        if (tokenParts.length == 3) {
            // try to decode (header and payload) to Base64
            try {
                Base64.getDecoder().decode(tokenParts[0]); // Header
                Base64.getDecoder().decode(tokenParts[1]); // Payload
                return false;  // It's a JWT, not a Google access token
            } catch (IllegalArgumentException e) {
                // If decoding fails, it's not a valid JWT
                return true;  // Not a JWT, so it could be a Google access token
            }
        }

        // If it doesn't have 3 parts or it's not properly Base64-encoded, it's not a JWT
        return true;  // Likely a Google access token or another type of token
    }
      // Method to revoke Google access token
      public void invalidateAccessToken(String token) {
        try {
            // Construct the URL for token revocation
            String url = UriComponentsBuilder.fromHttpUrl(revokeUrl)
                    .queryParam("token", token)
                    .toUriString();

            // Create an empty HttpEntity (since no body is required)
            HttpEntity<String> requestEntity = new HttpEntity<>(new HttpHeaders());

            // Send POST request to revoke the token
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            // Check the response status
            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Token revoked successfully.");
            } else {
                log.error("Failed to revoke token. Status Code: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("Error revoking token: {}", e.getMessage());
        }
    }
}
