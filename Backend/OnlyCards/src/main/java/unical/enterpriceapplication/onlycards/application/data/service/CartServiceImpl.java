package unical.enterpriceapplication.onlycards.application.data.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import unical.enterpriceapplication.onlycards.application.config.CacheConfig;
import unical.enterpriceapplication.onlycards.application.data.entities.accounts.Cart;
import unical.enterpriceapplication.onlycards.application.data.entities.accounts.User;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Product;
import unical.enterpriceapplication.onlycards.application.data.repository.CartRepository;
import unical.enterpriceapplication.onlycards.application.data.repository.ProductRepository;
import unical.enterpriceapplication.onlycards.application.data.repository.UserRepository;
import unical.enterpriceapplication.onlycards.application.dto.ProductCartDTO;
import unical.enterpriceapplication.onlycards.application.exception.LimitExceedException;
import unical.enterpriceapplication.onlycards.application.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
public class CartServiceImpl implements CartService {

    private final ProductRepository productRepository;
    private final CartRepository cartRepository;
    private final UserRepository userRepository;

    @Override
    public void save(Cart cart) {
        cartRepository.save(cart);
    }

    @Override
    public Cart createCart(UUID userID) throws ResourceNotFoundException {
        User user = userRepository.findById(userID)
                .orElseThrow(() -> new ResourceNotFoundException(userID.toString(),"User"));

        if(user.getCart() != null) return user.getCart();
        Cart cart = new Cart();
        cart.setProducts(new ArrayList<>());
        cart.setUser(user);
        return cartRepository.save(cart);
    }

    @Override
    @Cacheable(value = CacheConfig.CACHE_FOR_CART, key = "#userID") // la cache viene svuotata ogni giorno e quando viene modificato il cart
    public List<ProductCartDTO> getCartCards(UUID userID) throws ResourceNotFoundException {
        // recuperiamo le carte, le convertiamo in DTO e le restituiamo
        createCart(userID);
        Cart cart = cartRepository.findByUserId(userID)
                .orElseThrow(() -> new ResourceNotFoundException(userID.toString(),"Cart not found"));

        log.debug("Cart products: {}", cart.getProducts());

        return cart.getProducts().stream()
                .map(product -> {
                    ProductCartDTO dto = new ProductCartDTO();
                    dto.setId(product.getId());
                    dto.setCondition(product.getCondition());
                    dto.setImages(product.getImages());
                    dto.setPrice(product.getPrice());
                    dto.setReleaseDate(product.getReleaseDate());
                    dto.setStateDescription(product.getStateDescription());
                    //CardType
                    dto.setCardName(product.getProductType().getName());
                    dto.setCardLanguage(product.getProductType().getLanguage());
                    dto.setNumSell(product.getProductType().getNumSell());
                    dto.setGame(product.getProductType().getGame());
                    dto.setType(product.getProductType().getType());
                    //Account
                    dto.setUsername(product.getUser().getUsername());
                    dto.setEmail(product.getUser().getEmail());
                    return dto;
                })
                .toList();
    }

    @Override
    @CacheEvict(value = CacheConfig.CACHE_FOR_CART, allEntries = true)
    public void addProduct(UUID userID, UUID productID) throws ResourceNotFoundException, LimitExceedException {
        createCart(userID);

        Cart cart = cartRepository.findByUserId(userID)
                .orElseThrow(() -> new ResourceNotFoundException(userID.toString(),"Cart"));

        if(cart.getProducts().size()+1 >= MAX_PRODUCTS)
            throw new LimitExceedException("Product", MAX_PRODUCTS);

        Product product = productRepository.findById(productID)
                .orElseThrow(() -> new ResourceNotFoundException(productID.toString(),"Product"));
        
        if(product.getUser().getId() == userID){
            throw new IllegalArgumentException("User cannot buy a product that he has put up for sale");
        }
        
        if(!cart.getProducts().contains(product)){ // se il prodotto non è già presente nel carrello lo aggiungo
            cart.getProducts().add(product);
        }
        cartRepository.save(cart);
        log.debug("Product {} added to cart by user {}", productID, userID);
    }

    @Override
    @CacheEvict(value = CacheConfig.CACHE_FOR_CART, allEntries = true)
    public void removeProduct(UUID userID, UUID productID) throws ResourceNotFoundException {
        createCart(userID);
        Cart cart = cartRepository.findByUserId(userID)
                .orElseThrow(() -> new ResourceNotFoundException(userID.toString(),"Cart"));

        Product product = productRepository.findById(productID)
                .orElseThrow(() -> new ResourceNotFoundException(userID.toString(),"Product"));

        cart.getProducts().remove(product);
        cartRepository.save(cart);
        log.debug("Product {} removed from cart by user {}", productID, userID);
    }










}
