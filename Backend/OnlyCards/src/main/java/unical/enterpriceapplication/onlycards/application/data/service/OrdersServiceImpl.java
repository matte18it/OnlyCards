package unical.enterpriceapplication.onlycards.application.data.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import unical.enterpriceapplication.onlycards.application.data.entities.accounts.User;
import unical.enterpriceapplication.onlycards.application.data.entities.accounts.Wallet;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Money;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.Product;
import unical.enterpriceapplication.onlycards.application.data.entities.cards.ProductType;
import unical.enterpriceapplication.onlycards.application.data.entities.orders.Orders;
import unical.enterpriceapplication.onlycards.application.data.entities.orders.Status;
import unical.enterpriceapplication.onlycards.application.data.entities.orders.Transactions;
import unical.enterpriceapplication.onlycards.application.data.repository.OrderSpecification;
import unical.enterpriceapplication.onlycards.application.data.repository.OrdersRepository;
import unical.enterpriceapplication.onlycards.application.data.repository.ProductRepository;
import unical.enterpriceapplication.onlycards.application.data.repository.ProductTypeRepository;
import unical.enterpriceapplication.onlycards.application.data.repository.TransactionsRepository;
import unical.enterpriceapplication.onlycards.application.data.repository.UserRepository;
import unical.enterpriceapplication.onlycards.application.data.repository.WalletRepository;
import unical.enterpriceapplication.onlycards.application.dto.OrderDetailDto;
import unical.enterpriceapplication.onlycards.application.dto.OrderEditDto;
import unical.enterpriceapplication.onlycards.application.dto.OrderInfoDto;
import unical.enterpriceapplication.onlycards.application.dto.OrderSortingOptions;
import unical.enterpriceapplication.onlycards.application.dto.OrdersDto;
import unical.enterpriceapplication.onlycards.application.dto.ProductDto;
import unical.enterpriceapplication.onlycards.application.dto.TransactionInfoDto;
import unical.enterpriceapplication.onlycards.application.dto.TransactionsDto;
import unical.enterpriceapplication.onlycards.application.exception.LimitExceedException;
import unical.enterpriceapplication.onlycards.application.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrdersServiceImpl implements OrdersService {
    private final OrdersRepository ordersRepository;
    private final TransactionsRepository transactionsRepository;
    private final WalletRepository walletRepository;
    private final WalletService walletService;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final ProductTypeRepository productTypeRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional(rollbackFor = {ResourceNotFoundException.class, LimitExceedException.class, Exception.class, IllegalArgumentException.class})
    public List<OrdersDto> createOrders(UUID buyerID, List<UUID> productsID) throws ResourceNotFoundException, LimitExceedException {
        User buyer = userRepository.findById(buyerID)
                .orElseThrow(() -> new ResourceNotFoundException(buyerID.toString(), "User not found"));

        // key -> venditori | value -> lista di prodotti di quel venditore
        Map<User, List<Product>> productsByVendor = new HashMap<>();
        for (UUID productId : productsID) {
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new ResourceNotFoundException(productId.toString(), "Product not found"));

            User seller = product.getUser();
            if(seller.getId() == buyerID){
                throw new IllegalArgumentException("User cannot buy a product that he has put up for sale");
            }

            /* Metodo che crea una lista di prodotti se il venditore non è stato ancora aggiunto,
             * se il venditore è già presente il metodo restituisce
             * la lista di prodotti associati a quel venditore.
             */
            productsByVendor.computeIfAbsent(seller, k -> new ArrayList<>()).add(product);
        }

        List<OrdersDto> ordersDTOList = new ArrayList<>();

        // Per ogni venditore crea un ordine
        for (Map.Entry<User, List<Product>> entry : productsByVendor.entrySet()) {
            User seller = entry.getKey();
            List<Product> products = entry.getValue();

            Orders order = new Orders();
            order.setUser(buyer);
            order.setVendorEmail(seller.getEmail());
            order.setStatus(Status.PENDING);
            order.setAddDate(LocalDateTime.now().toLocalDate());
            order.setModifyDate(LocalDateTime.now());

            // Per ogni prodotto crea una transazione per il venditore e una per l'acquirente
            List<Transactions> transactionsList = new ArrayList<>();
            for (Product product : products) {

                walletService.transferAmount(buyerID,seller.getId(),product.getPrice());

                Transactions debitTransaction = new Transactions();
                debitTransaction.setDate(LocalDateTime.now());
                debitTransaction.setValue(product.getPrice());
                debitTransaction.setType(false); // false -> debito
                debitTransaction.setWallet(buyer.getWallet());
                debitTransaction.setOrders(order);
                debitTransaction.setProduct(product);

                transactionsList.add(debitTransaction);
                transactionsRepository.save(debitTransaction);

                Transactions creditTransaction = new Transactions();
                creditTransaction.setDate(LocalDateTime.now());
                creditTransaction.setValue(product.getPrice());
                creditTransaction.setType(true); // true -> credito
                creditTransaction.setWallet(seller.getWallet());
                creditTransaction.setOrders(order);
                creditTransaction.setProduct(product);

                transactionsList.add(creditTransaction);
                transactionsRepository.save(creditTransaction);

                product.setSold(true);
                product.getProductType().setNumSell(product.getProductType().getNumSell() + 1);
            }

            order.setTransactions(transactionsList);

            Orders savedOrder = ordersRepository.save(order);

            OrdersDto orderDTO = orderToDTO(savedOrder);
            ordersDTOList.add(orderDTO);

            log.debug(order.toString());
        }

        return ordersDTOList;
    }

    private OrdersDto orderToDTO(Orders order) {
        OrdersDto orderDTO = new OrdersDto();
        orderDTO.setStatus(order.getStatus());
        orderDTO.setAddDate(order.getAddDate());
        orderDTO.setModifyDate(order.getModifyDate());

        List<TransactionsDto> transactionDTOs = order.getTransactions()
                .stream()
                .map(transaction -> {
                    TransactionsDto transactionsDto = new TransactionsDto();

                    ProductDto productDto = new ProductDto();
                    productDto.setId(transaction.getProduct().getId());
                    productDto.setStateDescription(transaction.getProduct().getStateDescription());
                    productDto.setReleaseDate(transaction.getProduct().getReleaseDate());
                    productDto.setImages(transaction.getProduct().getImages());
                    productDto.setPrice(transaction.getProduct().getPrice());
                    productDto.setCondition(transaction.getProduct().getCondition());
                    productDto.setSold(transaction.getProduct().isSold());

                    transactionsDto.setProduct(productDto);
                    transactionsDto.setWalletID(transaction.getWallet().getId());
                    transactionsDto.setType(transaction.isType());
                    transactionsDto.setDate(transaction.getDate());
                    transactionsDto.setValue(transaction.getValue());
                    return transactionsDto;
                }).collect(Collectors.toList());

        orderDTO.setTransactions(transactionDTOs);

        return orderDTO;
    }

    @Override
    public Page<OrdersDto> getOrders(OrderSpecification.Filter filter, int page) {
        Pageable pageable = PageRequest.of(page, 20);
        Specification<Orders> specification = OrderSpecification.buildSpecification(filter);
        Page<Orders> orders = ordersRepository.findAll(specification, pageable);

        log.debug("Found {} orders", orders.getTotalElements());

        return orders.map(order -> modelMapper.map(order, OrdersDto.class));
    }

    @Override
    public Page<OrderInfoDto> getAllOrders(OrderSortingOptions param, Sort.Direction direction, String buyer, String seller, Integer page, Integer size) {

        Sort sort = Sort.by(direction, param.getKey());

        Pageable pageable = PageRequest.of(page , size, sort);
        Page<Orders> orders ;
        if(buyer != null && seller != null){
            orders = ordersRepository.findAllByUser_UsernameContainingAndVendorEmailContaining(buyer, seller, pageable);

        }else if (buyer != null) {
            orders = ordersRepository.findAllByUser_UsernameContaining(buyer, pageable);
        }else if (seller!=null) {
            orders = ordersRepository.findAllByVendorEmailContaining(seller, pageable);

        }else{
            orders = ordersRepository.findAll(pageable);
        }


        log.debug("Found {} orders", orders.getTotalElements());


        // Mappa gli ordini e imposta manualmente lo username
        return orders.map(orderEntity -> {
            // Mappa l'entity `Orders` al DTO `OrderInfoDto`
            OrderInfoDto orderInfoDto = modelMapper.map(orderEntity, OrderInfoDto.class);
            // Verifica se l'utente associato all'ordine è null (utente eliminato)
            if (orderEntity.getUser() == null) {
                orderInfoDto.setBuyer("Utente eliminato");
            } else {
                orderInfoDto.setBuyer(orderEntity.getUser().getUsername());
            }

            orderInfoDto.setSeller(orderEntity.getVendorEmail());

            return orderInfoDto;
        });
    }

    @Override
    public boolean existsById(UUID orderId) {
        return ordersRepository.existsById(orderId);
    }

    @Override
    public OrderDetailDto getOrder(UUID orderId) {
        Optional<Orders> order = ordersRepository.findById(orderId);

        if (order.isEmpty()) {
            return null;
        }

        Orders orderEntity = order.get();
        OrderDetailDto orderDetailDto = modelMapper.map(orderEntity, OrderDetailDto.class);

        if (orderEntity.getUserLastEdit() != null) {
            Optional<User> user = userRepository.findById(UUID.fromString(orderEntity.getUserLastEdit()));
            user.ifPresent(value -> orderDetailDto.setUserLastEdit(value.getUsername()));
        }

        // Set per tenere traccia degli ID dei prodotti già aggiunti
        Set<UUID> uniqueProductIds = new HashSet<>();

        // Mappa le transazioni
        List<TransactionInfoDto> transactionDtos = orderEntity.getTransactions().stream()
                .map(transaction -> {
                    TransactionInfoDto transactionDto = new TransactionInfoDto();
                    // Mappa il valore (Prezzo) usando Money
                    Money priceDto = new Money();
                    priceDto.setAmount(transaction.getValue().getAmount());
                    priceDto.setCurrency(transaction.getValue().getCurrency());

                    transactionDto.setValue(priceDto);

                    // Verifica che product, productType e name non siano null
                    if (transaction.getProduct() == null) {
                        log.warn("Product is null for transaction ID: " + transaction.getId());
                    } else if (transaction.getProduct().getProductType() == null) {
                        log.warn("ProductType is null for transaction ID: " + transaction.getId());
                    } else if (transaction.getProduct().getProductType().getName() == null) {
                        log.warn("Product name is null for transaction ID: " + transaction.getId());
                    } else {
                        // Mappa il nome del prodotto
                        UUID productId = transaction.getProduct().getId();
                        if (!uniqueProductIds.contains(productId)) {
                            // Solo aggiungi se non è già presente
                            uniqueProductIds.add(productId);
                            transactionDto.setProductName(transaction.getProduct().getProductType().getName());
                            if (!transaction.getProduct().getImages().isEmpty()) {
                                transactionDto.setProductPhoto(transaction.getProduct().getImages().get(0).getPhoto());
                            }
                        } else {
                            // Se l'ID è già presente, restituisci null per saltare questo prodotto
                            return null;
                        }
                    }

                    return transactionDto;
                })
                .filter(Objects::nonNull) // Rimuovi i null prima di collezionare
                .collect(Collectors.toList());

        orderDetailDto.setTransactions(transactionDtos);

        return orderDetailDto;
    }



    @Override
    @Transactional
    public Orders updateOrder(UUID orderId, OrderEditDto orderDto) {
        Optional<Orders> order = ordersRepository.findById(orderId);
        if(order.isEmpty()){
            return null;
        }
        Orders orderEntity = order.get();
        switch (orderDto.getStatus()) {
            case CANCELLED -> {
                deleteAllTransactions(orderEntity.getTransactions());
                orderEntity.setTransactions(null);
                orderEntity.setStatus(orderDto.getStatus());
                orderEntity = ordersRepository.save(orderEntity);
            }

            default -> {
                orderEntity.setStatus(orderDto.getStatus());
                orderEntity = ordersRepository.save(orderEntity);
            }
        }

        return orderEntity;
    }
    @Override
    public void deleteAllTransactions(List<Transactions> transactions) {
        for (Transactions transaction : transactions) {
            Product product = transaction.getProduct();
            product.setSold(false);
            productRepository.save(product);
            ProductType productType = product.getProductType();
            if(transaction.isType())
                productType.setNumSell(productType.getNumSell() - 1);
            productTypeRepository.save(productType);
            Wallet wallet = transaction.getWallet();
            Double amount = transaction.getValue().getAmount();
            if(!transaction.isType()){ //re add money to wallet
                wallet.getBalance().setAmount(wallet.getBalance().getAmount() + amount);
                walletRepository.save(wallet);
            }else{ //remove money from wallet
                wallet.getBalance().setAmount(wallet.getBalance().getAmount() - amount);
                walletRepository.save(wallet);

            }
        }
    }

    @Override
    public boolean isNotValidStatus(UUID orderId, Status status) {

        Optional<Orders> order = ordersRepository.findById(orderId);
        if(order.isEmpty()){
            return false;
        }
        Orders orderEntity = order.get();
        return switch (orderEntity.getStatus()) {
            case SHIPPED -> status == Status.CANCELLED || status == Status.PENDING;
            case CANCELLED -> true;
            case DELIVERED -> true;
            default -> false;
        };
    }

    @Override
    public int countOrders(OrderSpecification.Filter filter) {
        Pageable pageable = PageRequest.of(0, 10);
        Specification<Orders> specification = OrderSpecification.buildSpecification(filter);
        Page<Orders> orders = ordersRepository.findAll(specification, pageable);

        // Restituisci il numero totale di ordini
        return (int) orders.getTotalElements();
    }
}