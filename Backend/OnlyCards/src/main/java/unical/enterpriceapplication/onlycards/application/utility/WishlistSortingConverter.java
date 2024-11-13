package unical.enterpriceapplication.onlycards.application.utility;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import unical.enterpriceapplication.onlycards.application.data.entities.wishlist.WishListSorting;

@Component
public class WishlistSortingConverter implements Converter<String, WishListSorting> {
    @Override
    public WishListSorting convert(String source) {
        return WishListSorting.valueOf(source.toUpperCase());
    }
}
