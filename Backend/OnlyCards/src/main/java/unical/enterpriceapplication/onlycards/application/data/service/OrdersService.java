package unical.enterpriceapplication.onlycards.application.data.service;

import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import unical.enterpriceapplication.onlycards.application.data.entities.orders.Orders;
import unical.enterpriceapplication.onlycards.application.data.entities.orders.Status;
import unical.enterpriceapplication.onlycards.application.data.entities.orders.Transactions;
import unical.enterpriceapplication.onlycards.application.data.repository.OrderSpecification;
import unical.enterpriceapplication.onlycards.application.dto.OrderDetailDto;
import unical.enterpriceapplication.onlycards.application.dto.OrderEditDto;
import unical.enterpriceapplication.onlycards.application.dto.OrderInfoDto;
import unical.enterpriceapplication.onlycards.application.dto.OrderSortingOptions;
import unical.enterpriceapplication.onlycards.application.dto.OrdersDto;
import unical.enterpriceapplication.onlycards.application.exception.LimitExceedException;
import unical.enterpriceapplication.onlycards.application.exception.ResourceNotFoundException;

public interface OrdersService {
    double MAX_AMOUNT_ORDER = 500000.0;

    List<OrdersDto> createOrders(UUID buyerID, List<UUID> productsID) throws ResourceNotFoundException, LimitExceedException;
    Page<OrdersDto> getOrders(OrderSpecification.Filter filter, int page);
    Page<OrderInfoDto> getAllOrders(OrderSortingOptions param, Sort.Direction direction, String buyer, String seller, Integer page, Integer size);
    boolean existsById(UUID orderId);
    OrderDetailDto getOrder(UUID orderId);
    Orders updateOrder(UUID orderId, OrderEditDto order);
    boolean isNotValidStatus(UUID orderId, Status status);
    void deleteAllTransactions(List<Transactions> transactions);
    int countOrders(OrderSpecification.Filter filter);
}
