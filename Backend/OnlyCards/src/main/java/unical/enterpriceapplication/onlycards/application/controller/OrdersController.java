package unical.enterpriceapplication.onlycards.application.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import jakarta.validation.constraints.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import unical.enterpriceapplication.onlycards.application.core.service.EmailSenderService;
import unical.enterpriceapplication.onlycards.application.data.entities.orders.Status;
import unical.enterpriceapplication.onlycards.application.data.repository.OrderSpecification;
import unical.enterpriceapplication.onlycards.application.data.service.OrdersService;
import unical.enterpriceapplication.onlycards.application.data.service.UserService;
import unical.enterpriceapplication.onlycards.application.dto.OrderDetailDto;
import unical.enterpriceapplication.onlycards.application.dto.OrderEditDto;
import unical.enterpriceapplication.onlycards.application.dto.OrderInfoDto;
import unical.enterpriceapplication.onlycards.application.dto.OrderSortingOptions;
import unical.enterpriceapplication.onlycards.application.dto.OrderStatusChangeRequestDto;
import unical.enterpriceapplication.onlycards.application.dto.OrdersDto;
import unical.enterpriceapplication.onlycards.application.dto.OrdersResponseDto;
import unical.enterpriceapplication.onlycards.application.dto.ServiceError;
import unical.enterpriceapplication.onlycards.application.exception.ConflictException;
import unical.enterpriceapplication.onlycards.application.exception.LimitExceedException;
import unical.enterpriceapplication.onlycards.application.exception.ResourceNotFoundException;

@RestController
@RequestMapping(path = "/v1/orders", produces = "application/json")
@RequiredArgsConstructor
@Slf4j
@Validated
public class OrdersController {
    private final UserService userService;
    private final OrdersService ordersService;
    private final EmailSenderService emailSenderService;

    @Operation(summary = "Create order", description = "This endpoint allows an authorized user (Buyer) to create an order. The user can create an order by providing the ID of the buyer and a list of product IDs. The order is created in the database, and an email notification is sent to the user to inform them of the order creation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order created"),
            @ApiResponse(responseCode = "400", description = "Order not created"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    @PostMapping(value = "/users/{buyerID}")
    @PreAuthorize("hasRole('BUYER') and #buyerID == @authService.getCurrentUserUUID()")
    public ResponseEntity<List<OrdersDto>> createOrder(@PathVariable UUID buyerID, @RequestBody List<UUID> productsID) throws ResourceNotFoundException, LimitExceedException {
        log.info("Creating order with buyerID: {}, productsID: {}", buyerID, productsID);
        return ResponseEntity.ok(ordersService.createOrders(buyerID, productsID));
    }

    @Operation(summary = "Get orders", description = "This endpoint allows an authorized user (Seller or Buyer) to retrieve a list of orders based on various filters. The user can filter the results by product name, status, type, price range, and date. The filters are processed through a specification to return only the orders that match the provided criteria.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Orders found"),
            @ApiResponse(responseCode = "400", description = "Orders not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error"),
    })
    @GetMapping(value = "/")
    @PreAuthorize("(hasRole('SELLER') or hasRole('BUYER')) and #userId == @authService.getCurrentUserUUID()")
    public ResponseEntity<OrdersResponseDto> getOrders(
            @RequestParam("userId") @NotNull UUID userId,
            @RequestParam("productName") @Size(max = 50) String productName,
            @RequestParam("status") String status,
            @RequestParam("type") boolean type,
            @RequestParam("minPrice") @PositiveOrZero Double minPrice,
            @RequestParam("maxPrice") @PositiveOrZero Double maxPrice,
            @RequestParam("date") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) String addDate,
            @RequestParam("page") @PositiveOrZero int page
    ) {
        log.info("Getting order with userId: {}, productName: {}, status: {}, type: {}, minPrice: {}, maxPrice: {}, date: {}, page: {}", userId, productName, status, type, minPrice, maxPrice, addDate, page);

        OrderSpecification.Filter filter = new OrderSpecification.Filter();
        // se è false setto l'id sennò il vendorEmail da ordine
        if (!type) filter.setUserId(String.valueOf(userId));
        else filter.setUserId(userService.getEmail(userId));
        filter.setProductName(productName);
        filter.setStatus(!Objects.equals(status, "") ? Status.valueOf(status) : null);
        filter.setType(type);
        filter.setMinPrice(minPrice);
        filter.setMaxPrice(maxPrice);
        filter.setAddDate(!Objects.equals(addDate, "") ? LocalDate.parse(addDate) : null);

        OrdersResponseDto ordersResponseDto = new OrdersResponseDto();
        ordersResponseDto.setOrdersDto(ordersService.getOrders(filter, page));
        ordersResponseDto.setTotalNumber(ordersService.countOrders(filter));

        return ResponseEntity.ok(ordersResponseDto);
    }
   @Operation(summary = "Orders", description = "Get all orders, with the possibility to filter and order them.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Orders retrieved"),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "403", description = "Forbidden, you must be a admin", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
    })
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
        public ResponseEntity<Page<OrderInfoDto>> getAllOrders( @RequestParam(required = false, name = "order-by", defaultValue = "created-date") OrderSortingOptions param, //sort by descending addDate by default
                                 @RequestParam(required = true, name = "direction", defaultValue = "desc") Sort.Direction direction,
                                @Size( max = 20) @RequestParam(required = false) String buyer,
                                @Size( max = 20) @RequestParam(required = false) String seller,
                               @PositiveOrZero @RequestParam(required = true, defaultValue = "0") Integer page,
                               @Positive @RequestParam(required = true, defaultValue = "15") @Max(30) Integer size) {

        log.info("Getting all orders with order-by: {}, direction: {}, buyer: {}, seller {}", param, direction, buyer, seller);
        return ResponseEntity.ok(ordersService.getAllOrders(param, direction, buyer, seller, page, size));
    }
    @Operation(summary = "Info on a order", description = "Get a order info with the order ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order retrieved"),
            @ApiResponse(responseCode = "404", description = "Order not found", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "403", description = "Forbidden, you must be a admin", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
    })
    @GetMapping("/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDetailDto> getOrder(@PathVariable UUID orderId) throws ResourceNotFoundException {
        if(!ordersService.existsById(orderId)){
            throw new ResourceNotFoundException(orderId.toString(), "order");
        }
        return ResponseEntity.ok(ordersService.getOrder(orderId));
    }
    @Operation(summary = "Edit a order", description = "Edit a order by the product ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order edited"),
            @ApiResponse(responseCode = "409", description = "It' not possible to change the order to the select value", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "404", description = "Order not found", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "401", description = "Unauthorized", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
            @ApiResponse(responseCode = "403", description = "Forbidden, you must be a admin  ", content={ @Content(mediaType = "application/json",schema= @Schema(implementation = ServiceError.class))}),
    })
    @PatchMapping(value = "/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> updateOrderStatus(@PathVariable UUID orderId, @RequestBody @Valid OrderEditDto order) throws ResourceNotFoundException, ConflictException {
        if(!ordersService.existsById(orderId)){
            throw new ResourceNotFoundException(orderId.toString(), "order");
        }
    

        if(ordersService.isNotValidStatus(orderId, order.getStatus())){
            throw new ConflictException("Cannot change order status to " + order.getStatus().toString());
        }
        ordersService.updateOrder(orderId, order);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Change order status", description = "This endpoint allows an authorized user (Buyer or Seller) to change the status of an order. The user can update the status of the order to one of the following values: 'SHIPPED', 'DELIVERED', or 'CANCELLED'. The order status is updated in the database, and an email notification is sent to the user to inform them of the change.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Order status changed"),
            @ApiResponse(responseCode = "400", description = "Order status not changed"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error"),
    })
    @PatchMapping(value = "/status/{orderId}")
    @PreAuthorize("(hasRole('BUYER') or hasRole('SELLER')) and #data.userId == @authService.getCurrentUserUUID()")
    public ResponseEntity<Void> changeStatus(@PathVariable UUID orderId, @RequestBody @Validated OrderStatusChangeRequestDto data) throws ResourceNotFoundException {
        log.info("Changing status of order with orderId: {}, status: {}", orderId, data.getStatus());

        if (!ordersService.existsById(orderId)) {
            throw new ResourceNotFoundException(orderId.toString(), "order");
        }

        OrderEditDto orderEditDto = new OrderEditDto();
        orderEditDto.setStatus(Status.fromDescription(data.getStatus()));
        emailSenderService.sendOrderStatusChange(ordersService.updateOrder(orderId, orderEditDto), data.getStatus());

        return ResponseEntity.ok().build();
    }
}
