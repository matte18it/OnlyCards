package unical.enterpriceapplication.onlycards.application.data.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import unical.enterpriceapplication.onlycards.application.data.entities.wishlist.UserWishlist;
import unical.enterpriceapplication.onlycards.application.data.repository.AccountWishlistRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class UserWishlistServiceImpl implements UserWishlistService {
    private final AccountWishlistRepository accountWishlistRepository;

    @Override
    public void save(UserWishlist userWishlist) {
        accountWishlistRepository.save(userWishlist);
    }
}
