package unical.enterpriceapplication.onlycards.application.data.service;

import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import unical.enterpriceapplication.onlycards.application.config.security.TokenStore;
import unical.enterpriceapplication.onlycards.application.data.entities.wishlist.CapabilityToken;
import unical.enterpriceapplication.onlycards.application.data.entities.wishlist.Wishlist;
import unical.enterpriceapplication.onlycards.application.data.repository.CapabilityTokenRepository;
import unical.enterpriceapplication.onlycards.application.data.repository.WishlistRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class CapabilityTokenServiceImpl implements CapabilityTokenService {
    private final CapabilityTokenRepository capabilityTokenRepository;
    private final WishlistRepository wishlistRepository;
    @Value("${app.back-end}")
    private String backendUrl;
    @Override
    @Transactional
    public String generateToken(UUID wishlistId)  {
        Optional<Wishlist> wishlist = wishlistRepository.findById(wishlistId);
        if(wishlist.isEmpty()){
            return null;
        }
        CapabilityToken capabilityToken = new CapabilityToken();
        String token=  TokenStore.getInstance().generateCapabilityToken("wishlist",backendUrl,  wishlistId.toString(), Collections.singletonList("read"));
        capabilityToken.setToken(token);
        capabilityToken.setWishlist(wishlist.get());
        CapabilityToken capabilityToken1 = capabilityTokenRepository.save(capabilityToken);
        wishlist.get().setToken(capabilityToken1);
        wishlistRepository.save(wishlist.get());

        return token;
    }

    @Override
    @Transactional
    public void deleteToken(UUID wishlistId, String token) {
        Optional<CapabilityToken> capabilityToken = capabilityTokenRepository.findByWishlist_Id( wishlistId);
        if(capabilityToken.isPresent()){
            if(!Objects.equals(capabilityToken.get().getToken(), token)){
                return;
            }
            Wishlist wishlist = capabilityToken.get().getWishlist();
            wishlist.setToken(null);
            wishlistRepository.save(wishlist);
            capabilityTokenRepository.delete(capabilityToken.get());
        }

    }




    @Override
    @Transactional
    public UUID getWishlistIdFromToken(String token)  {
        Optional<CapabilityToken>  capabilityToken= capabilityTokenRepository.findByToken(token);
        if(capabilityToken.isEmpty()){
            return null;
        }
        String tokenString =TokenStore.getInstance().getResourceIdFromToken("wishlist", backendUrl, capabilityToken.get().getToken(), Collections.singletonList("read")); // necessità del permesso read per accedergli, possiamo aggiungere altri permessi
         if(tokenString==null)
             return null;
        UUID wishlistId = UUID.fromString(TokenStore.getInstance().getResourceIdFromToken("wishlist",backendUrl,  capabilityToken.get().getToken(), Collections.singletonList("read")));
        if(wishlistId.equals(capabilityToken.get().getWishlist().getId()))
            return wishlistId;
        else{
            return null;
        }
    }
}
