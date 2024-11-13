package unical.enterpriceapplication.onlycards.application.utility;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.ProductSorting;
@Component
public class ProductSortingConverter implements Converter<String, ProductSorting> {
    @Override
    public ProductSorting convert(String source) {
        return ProductSorting.fromKey(source).orElseThrow(() -> new IllegalArgumentException("Invalid sorting option"));
    }
}
