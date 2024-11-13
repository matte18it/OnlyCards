package unical.enterpriceapplication.onlycards.application.data.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;

import unical.enterpriceapplication.onlycards.application.data.entities.cards.ProductSorting;
import unical.enterpriceapplication.onlycards.application.data.entities.wishlist.WishListSorting;
import unical.enterpriceapplication.onlycards.application.dto.ProductWishlistDto;
import unical.enterpriceapplication.onlycards.application.dto.UserWishlistEditDto;
import unical.enterpriceapplication.onlycards.application.dto.WishlistDto;
import unical.enterpriceapplication.onlycards.application.dto.WishlistEditDto;

public interface WishlistService {
    int MAX_WISHLIST_FOR_USER = 30;
    int MAX_SHARED_WITH = 15;
    int MAX_PRODUCTS_IN_WISHLIST = 50;

    boolean checkIfProductIsInWishlist(UUID wishlistId, UUID productId);
    Optional<WishlistDto> getWishlistById(UUID id, UUID userId) ;
    Page<WishlistDto> getUserWishlists(UUID userId, WishListSorting sorting, Boolean isOwner, Integer page, Integer size);
    Page<WishlistDto> getUserWishlists(UUID userId, WishListSorting sorting,  Integer page, Integer size);
    Page<ProductWishlistDto> getWishlistProducts(UUID id, int page, int size, ProductSorting sort, String name, String owner) ;
    void deleteProductFromWishlist(UUID wishlistId, UUID productId);
    boolean isUserOwnerOrShared(UUID id, UUID userId) ;
    boolean isUserOwner(UUID id, UUID userId) ;
    void addProductToWishlist(UUID wishlistId, UUID productId) ;
    void updateWishlist(UUID userId, UUID wishlistId, WishlistEditDto wishlistEditDto) ;
    void addUserToWishlist(UUID wishlistId, UserWishlistEditDto account) ;
    void saveDto(WishlistEditDto wishlist, UUID userId);
    void deleteUserFromWishlist(UUID wishlistId, UUID userId) ;
    void deleteWishlist(UUID id);
    boolean checkIfUserIsInWishlist(UUID wishlistId, UUID userId);
    boolean isUserInWishlist(UUID id, UUID wishlistId);
    int countWishlistsByOwner(UUID id);
    Page<WishlistDto> getUserPublicWishlists(UUID userId, WishListSorting sorting, Integer page, Integer size);
    Optional<WishlistDto> getPublicWishlistByName(UUID userId, String wishlistName);
    boolean existsByUserOwnerAndName(UUID id, String name);
}
