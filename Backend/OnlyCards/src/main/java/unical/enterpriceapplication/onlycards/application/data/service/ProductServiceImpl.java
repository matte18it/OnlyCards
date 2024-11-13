package unical.enterpriceapplication.onlycards.application.data.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import unical.enterpriceapplication.onlycards.application.core.service.FirebaseFolders;
import unical.enterpriceapplication.onlycards.application.core.service.FirebaseStorageService;
import unical.enterpriceapplication.onlycards.application.data.entities.accounts.User;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Currency;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Money;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Product;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.ProductPhoto;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.ProductType;
import unical.enterpriceapplication.onlycards.application.data.repository.ProductPhotoRepository;
import unical.enterpriceapplication.onlycards.application.data.repository.ProductRepository;
import unical.enterpriceapplication.onlycards.application.data.repository.ProductTypeRepository;
import unical.enterpriceapplication.onlycards.application.dto.AccountDto;
import unical.enterpriceapplication.onlycards.application.dto.ProductCartDTO;
import unical.enterpriceapplication.onlycards.application.dto.ProductDto;
import unical.enterpriceapplication.onlycards.application.dto.ProductEditDto;
import unical.enterpriceapplication.onlycards.application.dto.ProductImageDto;
import unical.enterpriceapplication.onlycards.application.dto.ProductInfoDto;
import unical.enterpriceapplication.onlycards.application.dto.ProductTypeDto;
import unical.enterpriceapplication.onlycards.application.dto.SaveProductDto;
import unical.enterpriceapplication.onlycards.application.dto.UserDTO;
import unical.enterpriceapplication.onlycards.application.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductServiceImpl implements ProductService {
    int MAX_IMAGE_FOR_PRODUCT = 5;
    private final ProductRepository productRepository;
    private final ProductTypeRepository productTypeRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;
    private final ProductPhotoRepository productPhotoRepository;
    private final FirebaseStorageService firebaseStorageService;

  

    @Override
    public Optional<Product> findById(UUID id) {
        return productRepository.findById(id);
    }

    @Override
    public Optional<ProductDto> getProductById(UUID id) {
        return productRepository.findById(id).map(card -> modelMapper.map(card, ProductDto.class));
    }



 

    @Override
    @Transactional
    public void deleteProduct(UUID id) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isEmpty()) {
            return;
        }
        Product productEntity = product.get();
        for(ProductPhoto image : productEntity.getImages()) {
            firebaseStorageService.deleteFile(String.valueOf(image.getId()), FirebaseFolders.PRODUCT);
            log.info("Deleted image: " + image.getId());
        }
        productRepository.delete(productEntity);
    }

    @Override
    public Product save(Product card) {
        return productRepository.save(card);
    }

    @Override
    public Page<ProductDto> getCardInfo(UUID id, int page) {
        Pageable pageable = PageRequest.of(page, 50);
        Page<Product> products = productRepository.findAllByProductId(id, pageable);
        return products.map(this::convertToProductDto);
    }
    private ProductDto convertToProductDto(Product product) {
        ProductDto productDto = modelMapper.map(product, ProductDto.class);

        // Imposta AccountDto
        AccountDto accountDto = modelMapper.map(product.getUser(), AccountDto.class);
        productDto.setAccount(accountDto);

        return productDto;
    }

    @Override
    public ProductCartDTO getSaleCard(UUID productID) throws ResourceNotFoundException {
        Product product = productRepository.findById(productID)
                .orElseThrow(() -> new ResourceNotFoundException("Product not found",productID.toString()));

        ProductCartDTO dto = new ProductCartDTO();

        dto.setId(product.getId());
        dto.setStateDescription(product.getStateDescription());
        dto.setReleaseDate(product.getReleaseDate());
        dto.setImages(product.getImages());
        dto.setPrice(product.getPrice());
        dto.setCondition(product.getCondition());
        dto.setSold(product.isSold());

        User user = product.getUser();
        if (user != null) {
            dto.setUsername(user.getUsername());
            dto.setEmail(user.getEmail());
            dto.setCellphone(user.getCellphoneNumber());
        }
        dto.setCardName(product.getProductType().getName());
        dto.setCardLanguage(product.getProductType().getLanguage());
        dto.setNumSell(product.getProductType().getNumSell());
        dto.setGame(product.getProductType().getGame());
        dto.setType(product.getProductType().getType());

        return dto;
    }



    @Override
    public Map<String, String> getCurrencies() {
        return Stream.of(Currency.values())
        .collect(Collectors.toMap(Enum::name, Currency::getSymbol));
    }
    @Override
    @Transactional
    public void updateProduct(UUID productId,  ProductEditDto productEditDto) {
        Optional<Product> product = productRepository.findById(productId);
        if (product.isEmpty()) {
            return;
        }
        Product productEntity = product.get();
        if(productEditDto.getCondition()!=null)
            productEntity.setCondition(productEditDto.getCondition());
        if(productEditDto.getStateDescription()!=null)
            productEntity.setStateDescription(productEditDto.getStateDescription());
        if(productEditDto.getPrice()!=null){
            Money price = new Money(productEditDto.getPrice().getAmount(), productEditDto.getPrice().getCurrency());
            productEntity.setPrice(price);
        }
        for(ProductImageDto image : productEditDto.getImages()) {
           UUID imageId = UUID.randomUUID();
            firebaseStorageService.uploadFile(image.getPhoto(), String.valueOf(imageId), FirebaseFolders.PRODUCT);
            String imageUrl = firebaseStorageService.getFile(String.valueOf(imageId), FirebaseFolders.PRODUCT);
            ProductPhoto productImage = new ProductPhoto();
            productImage.setPhoto(imageUrl);
            productImage.setId(imageId);
            productImage.setProduct(productEntity);
            productPhotoRepository.save(productImage);
        }
        productRepository.save(productEntity);

    }
    @Override
    public int getMaxImageForProduct() {
        return MAX_IMAGE_FOR_PRODUCT;
    }
    @Override
    public boolean isSold(UUID productId) {
        return productRepository.existsByIdAndSoldTrue(productId);
    }

    @Override
    @Transactional
    public void saveProduct(String userId, SaveProductDto saveProductDto) {
        // creo il prodotto
        Product newProduct = new Product();
        newProduct.setStateDescription(saveProductDto.getDescription());
        newProduct.setCondition(saveProductDto.getCondition());
        newProduct.setPrice(new Money(saveProductDto.getPrice().getAmount(), saveProductDto.getPrice().getCurrency()));
        newProduct.setProductType(productTypeRepository.findById(UUID.fromString(saveProductDto.getProductType())).orElseThrow());
        newProduct.setSold(false);
        newProduct.setReleaseDate(LocalDate.now());
        newProduct.setSold(false);

        UserDTO user = userService.getUserById(UUID.fromString(userId)).orElseThrow();
        newProduct.setUser(modelMapper.map(user, User.class));

        // lo salvo
        Product savedProduct = productRepository.save(newProduct);

        // ora salvo le immagini del prodotto cambiando il nome in idProdotto_index
        List<ProductPhoto> savedImage = new ArrayList<>();
        for(int i = 0; i < saveProductDto.getImages().size(); i++) {
            MultipartFile image = saveProductDto.getImages().get(i).getPhoto();
            UUID imageId = UUID.randomUUID();
            firebaseStorageService.uploadFile(image, imageId.toString(), FirebaseFolders.PRODUCT);
            String imageUrl = firebaseStorageService.getFile(imageId.toString(), FirebaseFolders.PRODUCT);
            ProductPhoto productImage = new ProductPhoto();
            productImage.setPhoto(imageUrl);
            productImage.setId(imageId);
            productImage.setProduct(savedProduct);

            savedImage.add(productPhotoRepository.save(productImage));
        }

        savedProduct.setImages(savedImage);
        productRepository.save(savedProduct);

        // verifico se il minPrice del productType va aggiornato
        ProductType productType = savedProduct.getProductType();
        Money minPrice = productType.getMinPrice();
        if(minPrice.getAmount() == 0 || savedProduct.getPrice().getAmount() < minPrice.getAmount()) {
            productType.setMinPrice(savedProduct.getPrice());
            productTypeRepository.save(productType);
        }
    }

    @Override
    public List<ProductDto> getLastAddedProducts(String game) {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Object> products = productRepository.findAllLastAdd(game, pageable);

        return getProductDtos(products);
    }

    @Override
    public List<ProductDto> getProductsByUser(UUID userId, int page) {
        Pageable pageable = PageRequest.of(page, 30);
        Page<Object> products = productRepository.findAllByUserId(userId, pageable);

        return getProductDtos(products);
    }

    @Override
    public int countProductById(UUID id) {
        return productRepository.countProductById(id);
    }

    private List<ProductDto> getProductDtos(Page<Object> products) {
        return products.stream()
                .map(obj -> {
                    Object[] arr = (Object[]) obj;
                    Product product = (Product) arr[0];
                    ProductType productType = (ProductType) arr[1];
                    ProductDto productDto = modelMapper.map(product, ProductDto.class);
                    productDto.setProductType(modelMapper.map(productType, ProductTypeDto.class));
                    return productDto;
                })
                .collect(Collectors.toList());
    }

    @Override
    public Page<ProductInfoDto> getUserProducts(UUID id, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Product> products = productRepository.findAllByUserIdAndSold(id, pageable);
        return products.map(product -> modelMapper.map(product, ProductInfoDto.class));
    }

    @Override
    public boolean isUserOwner(UUID id, UUID userId) {
        Optional<Product> product = productRepository.findById(id);
        return product.map(value -> value.getUser().getId().equals(userId)).orElse(false);
    }
}
