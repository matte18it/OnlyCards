package unical.enterpriceapplication.onlycards.application.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.modelmapper.AbstractConverter;
import org.modelmapper.Converter;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeMap;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import unical.enterpriceapplication.onlycards.application.data.entities.cards.Condition;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Currency;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.FeatureProduct;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Money;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Product;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.ProductPhoto;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.ProductType;
import unical.enterpriceapplication.onlycards.application.data.entities.wishlist.UserWishlist;
import unical.enterpriceapplication.onlycards.application.data.entities.wishlist.Wishlist;
import unical.enterpriceapplication.onlycards.application.dto.AccountDto;
import unical.enterpriceapplication.onlycards.application.dto.FeatureDTO;
import unical.enterpriceapplication.onlycards.application.dto.ProductDto;
import unical.enterpriceapplication.onlycards.application.dto.ProductInfoDto;
import unical.enterpriceapplication.onlycards.application.dto.ProductTypeDto;
import unical.enterpriceapplication.onlycards.application.dto.ProductWishlistDto;
import unical.enterpriceapplication.onlycards.application.dto.UserWishlistDto;
import unical.enterpriceapplication.onlycards.application.dto.WishlistDto;

@Configuration
public class ModelMapperConfig {
    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        // Abilito il matching tra i campi e setto l'access level a public
        modelMapper.getConfiguration().setFieldMatchingEnabled(true).setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PUBLIC);

        // Register a custom converter from List<FeatureProduct> to List<FeatureDTO>
        Converter<List<FeatureProduct>, List<FeatureDTO>> toFeatureDtoList = new AbstractConverter<>() {
            @Override
            protected List<FeatureDTO> convert(List<FeatureProduct> source) {
                if (source == null) {
                    return Collections.emptyList();
                }
                return source.stream()
                        .map(featureCard -> {
                            FeatureDTO featureDTO = new FeatureDTO();
                            featureDTO.setName(featureCard.getFeature().getName());
                            featureDTO.setValue(featureCard.getValue());

                            return featureDTO;
                        }).collect(Collectors.toList());
            }
        };

        // Register a custom converter from List<FeatureProduct> to List<FeatureDTO>
        Converter<Wishlist, WishlistDto> toWishlistToWishlistDto = new AbstractConverter<>() {
            @Override
            protected WishlistDto convert(Wishlist source) {
                    WishlistDto wishlistDto = new WishlistDto();
                    wishlistDto.setId(source.getId());
                    wishlistDto.setName(source.getName());


                    return wishlistDto;
            }
        };
        TypeMap<Wishlist, WishlistDto> typeMap = modelMapper.createTypeMap(Wishlist.class, WishlistDto.class);
        typeMap.setConverter(toWishlistToWishlistDto);



        // Use the converter in the type map configuration
        modelMapper.createTypeMap(ProductType.class, ProductTypeDto.class)
                .addMappings(mapper -> mapper.using(toFeatureDtoList).map(ProductType::getFeatures, ProductTypeDto::setFeatures));

        Converter<Object[], ProductTypeDto> toCardTypeDtoWithMoney = new AbstractConverter<>() {
            @Override
            protected ProductTypeDto convert(Object[] source) {
                Double priceAmount = (Double) source[0];
                Currency priceCurrency = (Currency) source[1];
                ProductType productType = (ProductType) source[2];

                ProductTypeDto dto = modelMapper.map(productType, ProductTypeDto.class);
                dto.setPrice(new Money(priceAmount, priceCurrency));
                return dto;
            }
        };

        TypeMap<Object[], ProductTypeDto> typeMap2 = modelMapper.createTypeMap(Object[].class, ProductTypeDto.class);
        typeMap2.setConverter(toCardTypeDtoWithMoney);

        Converter<Object[], ProductDto> toCardDtoConverter = new AbstractConverter<>() {
            @Override
            protected ProductDto convert(Object[] source) {
                // Crea un nuovo ProductDto
                ProductDto productDto = new ProductDto();

                // Mappa i valori di Account
                AccountDto accountDto = new AccountDto();
                accountDto.setUsername((String) source[0]);
                productDto.setAccount(accountDto);

                // Crea e mappa i valori di ProductType
                ProductTypeDto productTypeDto = new ProductTypeDto();
                productTypeDto.setId((UUID) source[0]);
                productTypeDto.setName((String) source[1]);
                productTypeDto.setNumSell((Integer) source[2]);
                productTypeDto.setLanguage((String) source[3]);
                productTypeDto.setPhoto((String) source[4]);
                productTypeDto.setGame((String) source[5]);
                productTypeDto.setMinPrice(new Money((Double) source[6], (Currency) source[7]));
                productTypeDto.setType((String) source[8]);

                // Mappa i valori delle features
                String featuresString = (String) source[12];
                List<FeatureDTO> features = Arrays.stream(featuresString.split(", "))
                        .map(feature -> {
                            String[] parts = feature.split(": ");
                            FeatureDTO featureDTO = new FeatureDTO();
                            featureDTO.setName(parts[0]);
                            featureDTO.setValue(parts[1]);
                            return featureDTO;
                        }).collect(Collectors.toList());
                productTypeDto.setFeatures(features);

                productDto.setProductType(productTypeDto);

                // Mappa gli altri valori di ProductDto
                productDto.setCondition((Condition) source[8]);
                productDto.setPrice(new Money((Double) source[9], (Currency) source[10]));
                productDto.setStateDescription((String) source[11]);

                return productDto;
            }
        };

        // Create a new TypeMap for the custom conversion
        TypeMap<Object[], ProductDto> typeMap3 = modelMapper.createTypeMap(Object[].class, ProductDto.class);
        typeMap3.setConverter(toCardDtoConverter);

        // Register a custom converter from List<Product> to List<ProductDto>
        Converter<Product, ProductWishlistDto> toCardDtoList = new AbstractConverter<>() {
            @Override
            protected ProductWishlistDto convert(Product card) {
                    ProductWishlistDto cardDTO = new ProductWishlistDto();
                    cardDTO.setId(card.getId());
                    cardDTO.setReleaseDate(card.getReleaseDate());
                    cardDTO.setImages(card.getImages().stream().map(ProductPhoto::getPhoto).toList());
                    cardDTO.setPrice(card.getPrice());
                    cardDTO.setName(card.getProductType().getName());
                    cardDTO.setLanguage(card.getProductType().getLanguage());
                    cardDTO.setGameUrl(card.getProductType().getGame());
                    cardDTO.setGame(card.getProductType().getGame());
                    cardDTO.setAccount(modelMapper.map(card.getUser(), UserWishlistDto.class));
                    cardDTO.setCondition(card.getCondition().getValue());
                    return cardDTO;

            }
        };
        // Create set converter
        TypeMap<Product, ProductWishlistDto> typeMap4 = modelMapper.createTypeMap(Product.class, ProductWishlistDto.class);
        typeMap4.setConverter(toCardDtoList);

        // Register a custom converter from List<Product> to List<ProductDto>
        Converter<UserWishlist, UserWishlistDto> accountWishlistAccountWishlistDtoConverter = new AbstractConverter<>() {
            @Override
            protected UserWishlistDto convert(UserWishlist accountWishlist) {
                UserWishlistDto userWishlistDto = new UserWishlistDto();
                userWishlistDto.setUsername(accountWishlist.getUser().getUsername());
                userWishlistDto.setId(accountWishlist.getUser().getId());
                userWishlistDto.setValueOwnership(accountWishlist.getOwnership().getValue());
                userWishlistDto.setKeyOwnership(accountWishlist.getOwnership().getKey());
                return userWishlistDto;

            }
        };
        // Create set converter
        TypeMap<UserWishlist, UserWishlistDto> typeMap5 = modelMapper.createTypeMap(UserWishlist.class, UserWishlistDto.class);
        typeMap5.setConverter(accountWishlistAccountWishlistDtoConverter);

        // Register a custom converter from List<Product> to List<ProductDto>
        Converter<Product, ProductInfoDto> toCardDtoInfoList = new AbstractConverter<>() {
            @Override
            protected ProductInfoDto convert(Product card) {
                ProductInfoDto cardDTO = new ProductInfoDto();
                    cardDTO.setId(card.getId());
                    cardDTO.setReleaseDate(card.getReleaseDate());
                    cardDTO.setImages(card.getImages().stream().map(ProductPhoto::getPhoto).toList());
                    cardDTO.setPrice(card.getPrice());
                    cardDTO.setName(card.getProductType().getName());
                    cardDTO.setLanguage(card.getProductType().getLanguage());
                    cardDTO.setGame(card.getProductType().getGame());
                    cardDTO.setCondition(card.getCondition().getValue());
                    return cardDTO;

            }
        };
        // Create set converter
        TypeMap<Product, ProductInfoDto> typeMap6 = modelMapper.createTypeMap(Product.class, ProductInfoDto.class);
        typeMap6.setConverter(toCardDtoInfoList);

        return modelMapper;
    }
}
