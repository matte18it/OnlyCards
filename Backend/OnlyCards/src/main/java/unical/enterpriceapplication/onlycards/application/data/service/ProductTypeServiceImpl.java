package unical.enterpriceapplication.onlycards.application.data.service;

import java.sql.Date;
import java.util.*;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import unical.enterpriceapplication.onlycards.application.config.CacheConfig;
import unical.enterpriceapplication.onlycards.application.core.service.FirebaseFolders;
import unical.enterpriceapplication.onlycards.application.core.service.FirebaseStorageService;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Currency;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Feature;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.FeatureProduct;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Money;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.ProductType;
import unical.enterpriceapplication.onlycards.application.data.repository.FeatureProductRepository;
import unical.enterpriceapplication.onlycards.application.data.repository.FeatureRepository;
import unical.enterpriceapplication.onlycards.application.data.repository.ProductTypeRepository;
import unical.enterpriceapplication.onlycards.application.data.repository.ProductTypeSpecification;
import unical.enterpriceapplication.onlycards.application.dto.*;
import unical.enterpriceapplication.onlycards.application.utility.AdvancedSearchParser;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductTypeServiceImpl implements ProductTypeService {
    private final ProductTypeRepository productTypeRepository;
    private final FeatureRepository featureRepository;
    private final FeatureProductRepository featureProductRepository;
    private final FirebaseStorageService storageService;
    private final ModelMapper modelMapper;
    private final AdvancedSearchParser advancedSearchParser;

    @Override
    @Cacheable(value = CacheConfig.CACHE_FOR_FEATURES, key = "#game")
    public List<FeatureSearchDTO> getFeatures(String game) {
        // Recupera le feature basate sul tipo di gioco
        Set<Feature> features = featureRepository.findFeaturesByCardTypeGame(game);

        // Trasforma ogni feature in un FeatureSearchDTO
        return features.stream().map(feature -> {
            // Crea un nuovo DTO
            FeatureSearchDTO dto = new FeatureSearchDTO();
            dto.setName(feature.getName());

            // Recupera i valori da tutte le FeatureProduct associate a questa Feature
            Set<String> values = feature.getCards().stream()
                    .map(FeatureProduct::getValue)
                    .collect(Collectors.toSet());

            dto.setValue(values);
            return dto;
        }).collect(Collectors.toList());
    }

    @Override
    public Page<ProductTypeDto> getProductTypes(ProductTypeSpecification.Filter filter, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Specification<ProductType> productTypeSpecification= ProductTypeSpecification.buildSpecification(filter);
        Page<ProductType> productTypePage = productTypeRepository.findAll(productTypeSpecification,pageable);


        return productTypePage.map(cardType -> modelMapper.map(cardType, ProductTypeDto.class));


    }

    @Override
    public ProductType save(ProductType card) {
        return productTypeRepository.saveAndFlush(card);
    }

    @Override
    public Optional<ProductType> findById(UUID id) {
        return productTypeRepository.findById(id);
    }

    @Override
    public boolean isProductTypePresent(UUID cardTypeId) {
        return productTypeRepository.existsById(cardTypeId);
    }

    @Override
    @Transactional
    public ProductTypeDto saveProductType( ProductTypeRegistrationDto productTypeRegistrationDto) {
        ProductType productType = new ProductType();
        productType.setName(productTypeRegistrationDto.getName());
        productType.setGame(productTypeRegistrationDto.getGame());
        productType.setLanguage(productTypeRegistrationDto.getLanguage());
        productType.setType(productTypeRegistrationDto.getType());
        ProductType savedProductType = productTypeRepository.save(productType);
        if(productTypeRegistrationDto.getPhoto()!=null){
            storageService.uploadFile(productTypeRegistrationDto.getPhoto(), String.valueOf(savedProductType.getId()), FirebaseFolders.PRODUCT_TYPE);
            String photoUrl= storageService.getFile(String.valueOf(savedProductType.getId()), FirebaseFolders.PRODUCT_TYPE);
            savedProductType.setPhoto(photoUrl);
            savedProductType = productTypeRepository.save(savedProductType);
        }
        else if(productTypeRegistrationDto.getPhotoUrl()!=null){
            savedProductType.setPhoto(productTypeRegistrationDto.getPhotoUrl());
            savedProductType = productTypeRepository.save(savedProductType);
        }

        for(FeatureDTO featureDTO : productTypeRegistrationDto.getFeatures()) {
            Optional<Feature> feature = featureRepository.findByName(featureDTO.getName());
            Feature savedFeature;
            if(feature.isEmpty()) {
                Feature newFeature = new Feature();
                newFeature.setName(featureDTO.getName());
                savedFeature = featureRepository.save(newFeature);
            }else
                savedFeature = feature.get();
            FeatureProduct featureProduct = new FeatureProduct();
            featureProduct.setFeature(savedFeature);
            featureProduct.setProductType(savedProductType);
            featureProduct.setValue(featureDTO.getValue());
            featureProductRepository.save(featureProduct);
        }

        return modelMapper.map(savedProductType, ProductTypeDto.class);
    }

    @Override
    public List<String> findDistinctGame() {
        return productTypeRepository.findDistinctGames();
    }

    @Override
    public List<ProductTypeDto> getTopSeller(String type) {
        // Recupero delle ProductType più vendute
        List<ProductType> productTypes = productTypeRepository.findTopSeller(type);

        // Mappatura da ProductType a ProductTypeDto
        return productTypes.stream().map(cardType -> modelMapper.map(cardType, ProductTypeDto.class)).collect(Collectors.toList());
    }

    @Override
    public List<ProductTypeDto> getBestPurchases(String type) {
        Pageable pageable = PageRequest.of(0, 10);
        List<Object[]> cardTypes = productTypeRepository.findBestPurchases(type, pageable);

        return cardTypes.stream().map(row -> {
            ProductTypeDto productTypeDto = new ProductTypeDto();

            productTypeDto.setId((UUID) row[0]);
            productTypeDto.setName((String) row[1]);
            productTypeDto.setLanguage((String) row[2]);
            productTypeDto.setPhoto((String) row[3]);
            productTypeDto.setNumSell((Integer) row[4]);
            productTypeDto.setLastAdd(((Date) row[5]).toLocalDate());
            productTypeDto.setMinPrice(new Money((Double) row[6], Currency.EUR));
            productTypeDto.setGame((String) row[7]);
            productTypeDto.setType((String) row[8]);

            Double priceAmount = (Double) row[9];
            Money price = new Money();
            price.setAmount(priceAmount);
            price.setCurrency(Currency.EUR);
            productTypeDto.setPrice(price);

            String featuresConcat = (String) row[10];
            if (featuresConcat != null && !featuresConcat.isEmpty()) {
                List<FeatureDTO> features = Arrays.stream(featuresConcat.split(", "))
                        .filter(featureStr -> featureStr.contains(": "))
                        .map(featureStr -> {
                            FeatureDTO featureDTO = new FeatureDTO();
                            String[] parts = featureStr.split(": ");
                            featureDTO.setName(parts[0]);
                            featureDTO.setValue(parts[1]);
                            return featureDTO;
                        })
                        .collect(Collectors.toList());
                productTypeDto.setFeatures(features);
            } else {
                productTypeDto.setFeatures(Collections.emptyList());
            }

            return productTypeDto;
        }).collect(Collectors.toList());
    }

    @Override
    public List<String> findDistinctLanguage() {
        return productTypeRepository.findDistinctLanguages();
    }

    @Override
    public List<String> findDistinctType() {
        return productTypeRepository.findDistinctTypes();
    }

    @Override
    public boolean isProductTypeUsed(UUID productTypeId) {
        return productTypeRepository.findById(productTypeId).map(productType -> !productType.getProducts().isEmpty()).orElse(false);
    }

    @Override
    @Transactional
    public void deleteProductType(UUID productTypeId) {
        productTypeRepository.deleteById(productTypeId);
        storageService.deleteFile(String.valueOf(productTypeId), FirebaseFolders.PRODUCT_TYPE);
    }

    @Override
    public ProductTypeDto getCardType(UUID id) {
        return productTypeRepository.findById(id).map(cardType -> modelMapper.map(cardType, ProductTypeDto.class)).orElse(null);
    }

    @Override
    public List<ProductTypeDto> getCardTypeSet(String setName, String game, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        List<ProductType> productTypes = productTypeRepository.findCardTypeSet(game, setName, pageable);

        return productTypes.stream().map(cardType -> modelMapper.map(cardType, ProductTypeDto.class)).collect(Collectors.toList());
    }

    @Override
    public Long getCardTypeSetCount(String setName, String game) {
        return (long) productTypeRepository.findCardTypeSet(game, setName, PageRequest.of(0, Integer.MAX_VALUE)).size();
    }
    @Override
    @Transactional
    public ProductTypeDto modifyProductType(UUID productId,  ProductTypeRegistrationDto productTypeRegistrationDto){
        Optional<ProductType> productTypeOptional = productTypeRepository.findById(productId);
        ProductType productType;
        if(productTypeOptional.isEmpty()) {
            return null;
        }else 
            productType = productTypeOptional.get();
        if(productType.getPhoto()!=null ){ 
            storageService.deleteFile(String.valueOf(productId), FirebaseFolders.PRODUCT_TYPE);
        }
        productType.setName(productTypeRegistrationDto.getName());
        productType.setGame(productTypeRegistrationDto.getGame());
        productType.setLanguage(productTypeRegistrationDto.getLanguage());
        productType.setType(productTypeRegistrationDto.getType());
        //productType.setPhoto(null);  don't delete the current photo
        ProductType savedProductType = productTypeRepository.save(productType);
        if(productTypeRegistrationDto.getPhoto()!=null){
            storageService.uploadFile(productTypeRegistrationDto.getPhoto(), String.valueOf(savedProductType.getId()), FirebaseFolders.PRODUCT_TYPE);
            String photoUrl= storageService.getFile(String.valueOf(savedProductType.getId()), FirebaseFolders.PRODUCT_TYPE);
            savedProductType.setPhoto(photoUrl);
            savedProductType = productTypeRepository.save(savedProductType);
        }
        else if(productTypeRegistrationDto.getPhotoUrl()!=null){
            savedProductType.setPhoto(productTypeRegistrationDto.getPhotoUrl());
            savedProductType = productTypeRepository.save(savedProductType);
        }
        featureProductRepository.deleteByProductType(savedProductType);
        for(FeatureDTO featureDTO : productTypeRegistrationDto.getFeatures()) {
            Optional<Feature> feature = featureRepository.findByName(featureDTO.getName());
            Feature savedFeature;
            if(feature.isEmpty()) {
                Feature newFeature = new Feature();
                newFeature.setName(featureDTO.getName());
                savedFeature = featureRepository.save(newFeature);
            }else
                savedFeature = feature.get();
            Optional<FeatureProduct> optionalFeatureProduct = featureProductRepository.findByFeatureAndProductType(savedFeature, savedProductType);
            if(optionalFeatureProduct.isPresent()){
                optionalFeatureProduct.get().setValue(featureDTO.getValue());
                featureProductRepository.save(optionalFeatureProduct.get());
            continue;
            }

            FeatureProduct featureProduct = new FeatureProduct();
            featureProduct.setFeature(savedFeature);
            featureProduct.setProductType(savedProductType);
            featureProduct.setValue(featureDTO.getValue());
            featureProductRepository.save(featureProduct);
        }
        return modelMapper.map(savedProductType, ProductTypeDto.class);
    }

    @Override
    public List<ProductTypeDto> getProductTypesSeller(String type, int page) {
        Pageable pageable = PageRequest.of(page, 10);

        return productTypeRepository.findAllByGame(type, pageable).stream().map(cardType -> modelMapper.map(cardType, ProductTypeDto.class)).collect(Collectors.toList());
    }

    @Override
    public List<AdvancedSearchDto> advancedSearch(String gameType, String name) {
        if (name == null || name.isBlank())
            throw new IllegalArgumentException("Il nome della carta non può essere vuoto");

        // Recupera l'URL dell'API in base al tipo di gioco
        String apiUrl = switch (gameType) {
            case "Pokémon" -> "https://api.pokemontcg.io/v2/cards?q=name:" + name;
            case "Magic" -> "https://api.scryfall.com/cards/search?q=" + name;
            case "Yu-Gi-Oh!" -> "https://db.ygoprodeck.com/api/v7/cardinfo.php?name=" + name;
            default -> "";
        };

        // Creo un oggetto WebClient per effettuare la chiamata all'API
        WebClient webClient = WebClient.create();

        try {
            // Effettua la chiamata API e deserializza la risposta
            String responseJson = webClient.mutate()
                    .codecs(configurer -> configurer
                            .defaultCodecs()
                            .maxInMemorySize(16 * 1024 * 1024)
                    ).build().get()
                    .uri(apiUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Parsing JSON per ogni gioco
            List<AdvancedSearchDto> results = new ArrayList<>();
            switch (gameType) {
                case "Pokémon" -> results = advancedSearchParser.parsePokemonResponse(responseJson);
                case "Magic" -> results = advancedSearchParser.parseMagicResponse(responseJson);
                case "Yu-Gi-Oh!" -> results.add(advancedSearchParser.parseYuGiOhResponse(responseJson));
                default -> {
                    return Collections.emptyList();
                }
            }

            return results;
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                // Gestione degli errori 4xx (client)
                log.error("Errore client durante la chiamata API: {}", e.getMessage(), e);
                throw new RuntimeException("Errore client: richiesta non valida o risorsa non trovata");
            } else if (e.getStatusCode().is5xxServerError()) {
                // Gestione degli errori 5xx (server)
                log.error("Errore server durante la chiamata API: {}", e.getMessage(), e);
                throw new RuntimeException("Errore server: il server API non è disponibile al momento");
            } else {
                // Gestione di altri errori di risposta
                log.error("Errore sconosciuto durante la chiamata API: {}", e.getMessage(), e);
                throw new RuntimeException("Errore sconosciuto durante la chiamata API");
            }
        } catch (Exception e) {
            // Gestione di altri errori imprevisti
            log.error("Errore sconosciuto durante la chiamata API: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante la chiamata API");
        }
    }

    @Override
    @Transactional
    public ProductTypeDto saveAdvancedSearch(String cardId, String gameType) {
        if (cardId == null || cardId.isBlank())
            throw new IllegalArgumentException("L'ID della carta non può essere vuoto");

        // Recupera l'URL dell'API in base al tipo di gioco
        String apiUrl = switch (gameType) {
            case "Pokémon" -> "https://api.pokemontcg.io/v2/cards/" + cardId;
            case "Magic" -> "https://api.scryfall.com/cards/" + cardId;
            case "Yu-Gi-Oh!" -> "https://db.ygoprodeck.com/api/v7/cardinfo.php?id=" + cardId;
            default -> "";
        };

        // Creo un oggetto WebClient per effettuare la chiamata all'API
        WebClient webClient = WebClient.create();

        try {
            // Effettua la chiamata API e deserializza la risposta
            String responseJson = webClient.mutate()
                    .codecs(configurer -> configurer
                            .defaultCodecs()
                            .maxInMemorySize(16 * 1024 * 1024)
                    ).build().get()
                    .uri(apiUrl)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            // Parsing JSON per ogni gioco
            AdvancedSearchDto result;
            switch (gameType) {
                case "Pokémon" -> result = advancedSearchParser.parseSinglePokemonResponse(responseJson);
                case "Magic" -> result = advancedSearchParser.parseSingleMagicResponse(responseJson);
                case "Yu-Gi-Oh!" -> result = advancedSearchParser.parseYuGiOhResponse(responseJson);
                default -> {
                    return null;
                }
            }

            return saveProductAdvancedSearch(result, gameType);
        } catch (WebClientResponseException e) {
            if (e.getStatusCode().is4xxClientError()) {
                // Gestione degli errori 4xx (client)
                log.error("Errore client durante la chiamata API: {}", e.getMessage(), e);
                throw new RuntimeException("Errore client: richiesta non valida o risorsa non trovata");
            } else if (e.getStatusCode().is5xxServerError()) {
                // Gestione degli errori 5xx (server)
                log.error("Errore server durante la chiamata API: {}", e.getMessage(), e);
                throw new RuntimeException("Errore server: il server API non è disponibile al momento");
            } else {
                // Gestione di altri errori di risposta
                log.error("Errore sconosciuto durante la chiamata API: {}", e.getMessage(), e);
                throw new RuntimeException("Errore sconosciuto durante la chiamata API");
            }
        } catch (Exception e) {
            // Gestione di altri errori imprevisti
            log.error("Errore sconosciuto durante la chiamata API: {}", e.getMessage(), e);
            throw new RuntimeException("Errore durante la chiamata API");
        }
    }
    @Transactional
    public ProductTypeDto saveProductAdvancedSearch(AdvancedSearchDto result, String gameType) {
        ProductTypeRegistrationDto productTypeRegistrationDto = new ProductTypeRegistrationDto();
        productTypeRegistrationDto.setName(result.getName());
        productTypeRegistrationDto.setGame(gameType);
        productTypeRegistrationDto.setLanguage("EN");
        productTypeRegistrationDto.setType("Card");

        List<FeatureDTO> features = new ArrayList<>();
        if (result.getSetName() != null && !result.getSetName().isEmpty()) {
            FeatureDTO feature = new FeatureDTO();
            feature.setName("set");
            feature.setValue(result.getSetName());

            features.add(feature);
        }
        if (result.getCollectorNumber() != null && !result.getCollectorNumber().isEmpty()) {
            FeatureDTO feature = new FeatureDTO();
            feature.setName("set card number");
            feature.setValue(result.getCollectorNumber());

            features.add(feature);
        }
        if (result.getRarity() != null && !result.getRarity().isEmpty()) {
            FeatureDTO feature = new FeatureDTO();
            feature.setName("rarity");
            feature.setValue(result.getRarity());

            features.add(feature);
        }
        if (result.getType() != null && !result.getType().isEmpty()) {
            FeatureDTO feature = new FeatureDTO();
            feature.setName("category");
            feature.setValue(result.getType());

            features.add(feature);
        }
        productTypeRegistrationDto.setFeatures(features);
        productTypeRegistrationDto.setPhotoUrl(result.getImage());

        return saveProductType(productTypeRegistrationDto);
    }
}
