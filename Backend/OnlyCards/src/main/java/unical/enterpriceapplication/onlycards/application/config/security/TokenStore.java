package unical.enterpriceapplication.onlycards.application.config.security;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSObject;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.Payload;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jose.jwk.Curve;
import com.nimbusds.jose.jwk.ECKey;
import com.nimbusds.jose.jwk.KeyUse;
import com.nimbusds.jose.jwk.gen.ECKeyGenerator;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TokenStore {
    
    private  final ECKey key = generateSecret();
     

    public static String[] decodedBase64(String headerToken) {
        log.debug("Decoding base64 token {}", headerToken);
        byte[] decodedBytes = Base64.getDecoder().decode(headerToken);
        String pairedCredentials = new String(decodedBytes);
        return pairedCredentials.split(":", 2);
    }
    private  ECKey generateSecret()  {

        try {
            return new ECKeyGenerator(Curve.P_256)
                    .keyUse(KeyUse.SIGNATURE)
                    .keyID(UUID.randomUUID().toString())
                    .generate();
        } catch (JOSEException e) {
            throw new RuntimeException("Error while creating secret key", e);
        }
    }
    @Getter
    private final static TokenStore instance= new TokenStore();
    private TokenStore() {}

    public  String createAccessToken(String id, String iss, List<String> roles) {
        Instant issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expiration = issuedAt.plus(SecurityConstants.EXPIRATION_TIME, SecurityConstants.EXPIRATION_UNIT);
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();
        JWTClaimsSet claimsSet = builder.subject(id).issuer(iss)
                .issueTime(Date.from(issuedAt)).expirationTime(Date.from(expiration))
                .claim("roles", roles.stream().map(Object::toString)
                        .collect(Collectors.joining(","))).build();
        Payload payload = new Payload(claimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS256), payload);
        try {
            jwsObject.sign(new MACSigner(key.toString()));
        } catch (JOSEException e) {
            throw new RuntimeException("Error while signing the JWT", e);
        }
        log.debug("Access token created for user: {}, with expiration: {}", id, expiration);
        return jwsObject.serialize();

    }
    public  String createRefreshToken(String id, String iss)  {
        Instant issuedAt = Instant.now().truncatedTo(ChronoUnit.SECONDS);
        Instant expiration = issuedAt.plus(SecurityConstants.EXPIRATION_REFRESH_TOKEN_TIME, SecurityConstants.EXPIRATION_UNIT_REFRESH_TOKEN);

        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();
        JWTClaimsSet claimsSet = builder.subject(id).issuer(iss) 
                .issueTime(Date.from(issuedAt)).
                        expirationTime(Date.from(expiration)).build();
        Payload payload = new Payload(claimsSet.toJSONObject());

        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS256), payload);
        try {
            jwsObject.sign(new MACSigner(key.toString()));
        } catch (JOSEException e) {
            throw new RuntimeException("Error while signing the JWT", e);
        }
        log.debug("Refresh Token created for user: {}, with expiration: {}", id, expiration);
        return jwsObject.serialize();

    }
    
    public SignedJWT parseTokenToSignedJWT(String token) {
        try {
            return SignedJWT.parse(token);
        } catch (ParseException e) {
           return null;
        }
    }

    public boolean isTokenInvalid(String token) {
        JWSVerifier verifier ;
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            verifier = new MACVerifier(key.toString());
            // Verify the token signature
            if (!signedJWT.verify(verifier)) {
                return true;
            }
            // verify the token expiration
            if (signedJWT.getJWTClaimsSet().getExpirationTime().before(new Date())) {
                return true;
            }} catch (JOSEException | ParseException e) {
           return true;
        }
        return false;

    }
    public Map<String, String> getClaims(String token) {
        Map<String, String> claims = new HashMap<>();
        // Parse the token
        SignedJWT signedJWT ;
        try {
            signedJWT = SignedJWT.parse(token);
        } catch (ParseException e) {
           return null;
        }

        // Extract claims
        JWTClaimsSet jwtClaimsSet ;
        try {
            jwtClaimsSet = signedJWT.getJWTClaimsSet();
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        for (Map.Entry<String, Object> entry : jwtClaimsSet.getClaims().entrySet()) {
            claims.put(entry.getKey(), entry.getValue().toString());
        }
        return claims;
    }

    public LocalDateTime getLocalDateFromClaim(String exp) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("EEE MMM dd HH:mm:ss zzz yyyy", Locale.ENGLISH);
        return LocalDateTime.parse(exp, formatter);
    }
    public String generateCapabilityToken(String resource,String iss,  String id, List<String> permissions) {
        JWTClaimsSet.Builder builder = new JWTClaimsSet.Builder();
    
        // Aggiungi i claim di base
        JWTClaimsSet claimsSet = builder
                .subject(id)
                .issuer(iss)
                .claim("resource", resource)
                .claim("permissions", permissions)  // Aggiungi l'array permissions come claim
                .build();
    
        Payload payload = new Payload(claimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(new JWSHeader(JWSAlgorithm.HS256), payload);
    
        try {
            // Firma il token con la chiave segreta
            jwsObject.sign(new MACSigner(key.toString()));
        } catch (JOSEException e) {
            throw new RuntimeException("Error while signing the capability token:", e);
        }
    
        log.debug("Capability token created for resource: {}", id);
        
        // Ritorna il token come stringa
        return jwsObject.serialize();
    }
    public String getResourceIdFromToken(String resource, String iss, String token, List<String> requiredPermissions) {
        Map<String, String> claims = new HashMap<>();
        // Parse del token
        SignedJWT signedJWT;
        try {
            signedJWT = SignedJWT.parse(token);
            JWTClaimsSet jwtClaimsSet = signedJWT.getJWTClaimsSet();
    
            // Estrarre i claims
            for (Map.Entry<String, Object> entry : jwtClaimsSet.getClaims().entrySet()) {
                claims.put(entry.getKey(), entry.getValue().toString());
            }
    
            // Verifica risorsa e issuer
            if (claims.get("resource").equals(resource) && claims.get("iss").equals(iss)) {
                // Controlla se il token contiene tutti i permessi richiesti
                List<String> permissions = jwtClaimsSet.getStringListClaim("permissions");
    
                if (permissions != null && permissions.containsAll(requiredPermissions)) {
                    return claims.get("sub");  // Ritorna l'ID della risorsa se i permessi sono sufficienti
                }
            }
        } catch (ParseException e) {
            return null;
        }
        return null;
    }
}
