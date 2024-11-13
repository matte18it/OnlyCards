package unical.enterpriceapplication.onlycards.application.data.service;

import org.springframework.stereotype.Service;
import unical.enterpriceapplication.onlycards.application.data.entities.accounts.Cart;
import unical.enterpriceapplication.onlycards.application.dto.ProductCartDTO;
import unical.enterpriceapplication.onlycards.application.exception.LimitExceedException;
import unical.enterpriceapplication.onlycards.application.exception.ResourceNotFoundException;

import java.util.List;
import java.util.UUID;

@Service
public interface CartService {
    int MAX_PRODUCTS = 20;

    void save(Cart cart);
    Cart createCart(UUID userID) throws ResourceNotFoundException;
    List<ProductCartDTO> getCartCards(UUID UserId) throws ResourceNotFoundException;
    void addProduct(UUID userID, UUID productID) throws ResourceNotFoundException, LimitExceedException;
    void removeProduct(UUID userID, UUID productID) throws ResourceNotFoundException;

}
