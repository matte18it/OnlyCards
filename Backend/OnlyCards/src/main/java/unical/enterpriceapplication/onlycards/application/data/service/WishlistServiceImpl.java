package unical.enterpriceapplication.onlycards.application.data.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import unical.enterpriceapplication.onlycards.application.data.entities.accounts.User;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Product;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.ProductSorting;
import unical.enterpriceapplication.onlycards.application.data.entities.wishlist.UserWishlist;
import unical.enterpriceapplication.onlycards.application.data.entities.wishlist.WishListSorting;
import unical.enterpriceapplication.onlycards.application.data.entities.wishlist.Wishlist;
import unical.enterpriceapplication.onlycards.application.data.entities.wishlist.WishlistOwnership;
import unical.enterpriceapplication.onlycards.application.data.repository.AccountWishlistRepository;
import unical.enterpriceapplication.onlycards.application.data.repository.ProductRepository;
import unical.enterpriceapplication.onlycards.application.data.repository.UserRepository;
import unical.enterpriceapplication.onlycards.application.data.repository.WishlistRepository;
import unical.enterpriceapplication.onlycards.application.dto.ProductWishlistDto;
import unical.enterpriceapplication.onlycards.application.dto.UserWishlistDto;
import unical.enterpriceapplication.onlycards.application.dto.UserWishlistEditDto;
import unical.enterpriceapplication.onlycards.application.dto.WishlistDto;
import unical.enterpriceapplication.onlycards.application.dto.WishlistEditDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class WishlistServiceImpl implements WishlistService{
    private final WishlistRepository wishlistRepository;
    private final AccountWishlistRepository accountWishlistRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ModelMapper modelMapper;


    @Override
    public Optional<WishlistDto> getWishlistById(UUID id, UUID userId)  {
        Optional<Wishlist> wishlist = wishlistRepository.findById(id);
        return mapWishlist(wishlist, userId);
    }
    @Override
    public Optional<WishlistDto> getPublicWishlistByName(UUID userId, String wishlistName)  {
        Optional<Wishlist> wishlist= wishlistRepository.findByNameAndUserOwnerAndPublic(wishlistName, userId);

        return mapWishlist(wishlist, null);
    }


    @Override
    public Page<WishlistDto> getUserWishlists(UUID userId, WishListSorting sorting, Boolean isOwner,  Integer page, Integer size) {
        Pageable pageable;
        if(sorting==null)
            sorting=WishListSorting.NEW;
        pageable = switch (sorting) {
            case NEW -> PageRequest.of(page, size, Sort.by("lastUpdate").descending());
            default -> PageRequest.of(page, size, Sort.by("lastUpdate").ascending());
        };
        Page<Wishlist> wishlists;
        if (isOwner) {
            wishlists = wishlistRepository.findByUserIdAndOwnership(userId, WishlistOwnership.OWNER, pageable);
        } else {
            wishlists = wishlistRepository.findByUserIdAndOwnership(userId, WishlistOwnership.SHARED_WITH, pageable);
        }


        return wishlists.map(wishlist -> modelMapper.map(wishlist, WishlistDto.class));
    }
    @Override
    public Page<WishlistDto> getUserPublicWishlists(UUID userId, WishListSorting sorting, Integer page, Integer size) {
        Pageable pageable;
        if(sorting==null)
            sorting=WishListSorting.NEW;
        pageable = switch (sorting) {
            case NEW -> PageRequest.of(page, size, Sort.by("lastUpdate").descending());
            default -> PageRequest.of(page, size, Sort.by("lastUpdate").ascending());
        };
        
        Page<Wishlist> wishlistPage = wishlistRepository.findPublicWishlistsByUserId(userId, pageable);


        return wishlistPage.map(wishlist -> modelMapper.map(wishlist, WishlistDto.class));
    }

    @Override
public Page<WishlistDto> getUserWishlists(UUID userId, WishListSorting sorting, Integer page, Integer size) {
    if (sorting == null) {
        sorting = WishListSorting.NEW; // Ordinamento predefinito se non specificato
    }

    Pageable pageable = switch (sorting) {
        case NEW -> PageRequest.of(page, size, Sort.by("lastUpdate").descending());
        case OLD -> PageRequest.of(page, size, Sort.by("lastUpdate").ascending());
    };

    Page<Wishlist> wishlistPage = wishlistRepository.findByUserId(userId, pageable);

    return wishlistPage.map(wishlist -> modelMapper.map(wishlist, WishlistDto.class));
}


    @Override
    @Transactional(readOnly = true)
    public Page<ProductWishlistDto> getWishlistProducts(UUID id, int page, int size, ProductSorting sort, String productName, String owner)  {

        Pageable pageable;
        if(sort!=null){
        pageable = switch (sort) {
            case PRICE_ASC -> PageRequest.of(page, size, Sort.by("price.amount").ascending());
            case PRICE_DESC -> PageRequest.of(page, size, Sort.by("price.amount").descending());
            default -> PageRequest.of(page, size, Sort.by("productType.numSell").descending());
        };}
        else {
            pageable = PageRequest.of(page, size, Sort.by("productType.numSell").descending());
        }
        Page<Product> products;
        if(productName==null && owner==null)
            products = productRepository.findAllByWishlistId(id, pageable);
        else if(productName!=null && owner!=null)
            products = productRepository.findAllByWishlistIdAndProductNameAndProductOwner(id, pageable, productName, owner);
        else if(productName!=null)
            products = productRepository.findAllByWishlistIdAndProductName(id, pageable, productName);
        else
            products = productRepository.findAllByWishlistIdAndProductOwner(id, pageable, owner);


        return products.map(product -> modelMapper.map(product, ProductWishlistDto.class));
    }



    @Override
    public boolean isUserOwnerOrShared(UUID id, UUID userId)  {
        Optional<Wishlist> wishlist = wishlistRepository.findById(id);
        if(wishlist.isPresent()){
            for(UserWishlist userWishlist : wishlist.get().getUsers()){
                if(userWishlist.getUser().getId().equals(userId))
                    return true;
            }
        }
        return false;
    }

    @Override
    public boolean isUserOwner(UUID id, UUID userId)  {
        Optional<Wishlist> wishlist = wishlistRepository.findById(id);
        if(wishlist.isPresent()){
            for(UserWishlist userWishlist : wishlist.get().getUsers()){
                if(userWishlist.getUser().getId().equals(userId) && userWishlist.getOwnership().equals(WishlistOwnership.OWNER))
                    return true;
            }
        }
        return false;
    }

    @Override
    public void deleteProductFromWishlist(UUID wishlistId, UUID productId)  {


        Optional<Wishlist> wishlist = wishlistRepository.findById(wishlistId);
        if(wishlist.isPresent()){
            wishlist.get().setLastUpdate(LocalDateTime.now());
            wishlist.get().getProducts().removeIf(product -> product.getId().equals(productId));
            wishlistRepository.save(wishlist.get());
        }
    }

    @Override
    public boolean checkIfProductIsInWishlist(UUID wishlistId, UUID productId) {
        return wishlistRepository.findById(wishlistId)
                .map(wishlist -> wishlist.getProducts().stream()
                        .anyMatch(product -> product.getId().equals(productId)))
                .orElse(false);

    }
    @Override
    public boolean checkIfUserIsInWishlist(UUID wishlistId, UUID userId) {
        return accountWishlistRepository.findByUser_IdAndWishlist_Id(userId, wishlistId).isPresent();

    }


    @Override
    public void addProductToWishlist(UUID wishlistId, UUID productId)  {
        Optional<Wishlist> wishlist = wishlistRepository.findById(wishlistId);
        if(wishlist.isPresent()){
            Optional<Product> product = productRepository.findById(productId);
            if(product.isPresent()){
                wishlist.get().getProducts().add(product.get());
                wishlistRepository.save(wishlist.get());
            }
        }

    }

    @Override
    public void updateWishlist(UUID userId, UUID wishlistId, WishlistEditDto wishlistEditDto)  {
        Optional<Wishlist> wishlist = wishlistRepository.findById(wishlistId);
        if(wishlist.isPresent()) {
            if(wishlistEditDto.getName()!=null){
                wishlist.get().setName(wishlistEditDto.getName());
            }
            if(wishlistEditDto.getIsPublic()!=null){
                wishlist.get().setIsPublic(wishlistEditDto.getIsPublic());
            }
            wishlistRepository.save(wishlist.get());
        }

    }




    @Override
    public void addUserToWishlist(UUID wishlistId, UserWishlistEditDto account) {
        Optional<User> user = userRepository.findByUsername(account.getUsername());
        Optional<Wishlist> wishlist = wishlistRepository.findById(wishlistId);


        if(user.isEmpty() || wishlist.isEmpty())
            return;
        UserWishlist userWishlist = new UserWishlist();
        userWishlist.setUser(user.get());
        userWishlist.setWishlist(wishlist.get());
        userWishlist.setOwnership(WishlistOwnership.SHARED_WITH);
        accountWishlistRepository.save(userWishlist);

    }

    @Override
    @Transactional
    public void saveDto(WishlistEditDto wishlist, UUID userId) {
        Optional<User> user = userRepository.findById(userId);
        if(user.isEmpty())
            return;
        Wishlist newWishlist = new Wishlist();
        newWishlist.setName(wishlist.getName());
        newWishlist.setIsPublic(wishlist.getIsPublic());
        UserWishlist userWishlist = new UserWishlist();
        userWishlist.setWishlist(newWishlist);
        userWishlist.setUser(user.get());
        userWishlist.setOwnership(WishlistOwnership.OWNER);
        wishlistRepository.save(newWishlist);
        accountWishlistRepository.save(userWishlist);

    }

    @Override
    public void deleteUserFromWishlist(UUID wishlistId, UUID userId)  {
        Optional<UserWishlist> accountWishlist = accountWishlistRepository.findByUser_IdAndWishlist_Id(userId, wishlistId);
        accountWishlist.ifPresent(accountWishlistRepository::delete);

    }

    @Override
    public void deleteWishlist(UUID id) {
        Optional<Wishlist> wishlist = wishlistRepository.findById(id);
        wishlist.ifPresent(wishlistRepository::delete);

    }

    @Override
    public boolean isUserInWishlist(UUID id, UUID wishlistId) {
        return accountWishlistRepository.findByUser_IdAndWishlist_Id(id, wishlistId).isPresent();
    }

    @Override
    public int countWishlistsByOwner(UUID id) {
        return wishlistRepository.countWishlistByOwner(id);
    }


    private Optional<WishlistDto> mapWishlist(Optional<Wishlist> wishlist, UUID userId) {
        if(wishlist.isEmpty())
            return Optional.empty();
        WishlistDto wishlistDto = modelMapper.map(wishlist.get(), WishlistDto.class);
        wishlistDto.setLastUpdate(wishlist.get().getLastUpdate());
        wishlistDto.setIsPublic(wishlist.get().getIsPublic());
        if(wishlist.get().getToken()!=null && userId!=null && isUserOwner(wishlist.get().getId(), userId)) //only owner can see token
            wishlistDto.setToken(wishlist.get().getToken().getToken());
        List<UserWishlistDto> userWishlistDtos =new ArrayList<>();
        for (UserWishlist userWishlist : wishlist.get().getUsers()) {
            if(userId!=null && isUserOwner(wishlist.get().getId(), userId) ){
            UserWishlistDto userWishlistDto = new UserWishlistDto();
            userWishlistDto.setId(userWishlist.getUser().getId());
            userWishlistDto.setUsername(userWishlist.getUser().getUsername());
            userWishlistDto.setKeyOwnership(userWishlist.getOwnership().getKey());
            userWishlistDto.setValueOwnership(userWishlist.getOwnership().getValue());
            userWishlistDtos.add(userWishlistDto);}
            else if(userWishlist.getOwnership().equals(WishlistOwnership.OWNER)){ //only owner can see other shared with
                UserWishlistDto userWishlistDto = new UserWishlistDto();
                userWishlistDto.setId(userWishlist.getUser().getId());
                userWishlistDto.setUsername(userWishlist.getUser().getUsername());
                userWishlistDto.setKeyOwnership(userWishlist.getOwnership().getKey());
                userWishlistDto.setValueOwnership(userWishlist.getOwnership().getValue());
                userWishlistDtos.add(userWishlistDto);
            }
        }
        wishlistDto.setAccounts(userWishlistDtos);
        return Optional.of(wishlistDto);
    }

    @Override
    public boolean existsByUserOwnerAndName(UUID id, String name) {
        return wishlistRepository.findByNameAndUserOwner(name, id).isPresent();
    }



}
