package unical.enterpriceapplication.onlycards.application.controller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import unical.enterpriceapplication.onlycards.application.core.service.EmailSenderService;
import unical.enterpriceapplication.onlycards.application.dto.ProductCartDTO;
import unical.enterpriceapplication.onlycards.application.dto.RequestProductDto;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(path = "/v1/emails")
@RequiredArgsConstructor
@Validated
@Slf4j
public class EmailController {
    // ----- VARIABILI -----
    private final EmailSenderService emailSenderService;

    // ----- METODI -----
    @Operation(summary = "Request the addition of a new product type", description = "This endpoint allows a seller to submit a request to add a new product type. The request includes details such as the product name, game, photo, and description, all provided via a form. The request is validated, and if authorized, an email is sent to the admin with the submitted information for approval.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Email sent"),
            @ApiResponse(responseCode = "400", description = "Email not sent"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error"),
    })
    @PostMapping(value = "/request-product", produces = "application/json", consumes = "multipart/form-data")
    @PreAuthorize("hasRole('SELLER') and #requestProductDto.id == @authService.getCurrentUserUUID()")
    public ResponseEntity<Void> requestProduct(@ModelAttribute @Validated RequestProductDto requestProductDto) {
        log.debug("Requesting product");

        if (requestProductDto == null)
            throw new IllegalArgumentException("RequestProductDto cannot be null");

        try {
            emailSenderService.sendRequestEmail(requestProductDto);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Operation(summary = "Request help", description = "This endpoint allows a user to request assistance. The request includes details such as the subject and description, all provided via a form. The request is validated, and if authorized, an email is sent to the admin with the submitted information for further action.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Email sent"),
            @ApiResponse(responseCode = "400", description = "Email not sent"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error"),
    })
    @PostMapping(value = "/help-request", produces = "application/json")
    @PreAuthorize("(hasRole('SELLER') or hasRole('BUYER')) and #userId == @authService.getCurrentUserUUID()")
    public ResponseEntity<Void> helpRequest(
            @RequestParam("object") @Size(min = 3, max = 100) String object,
            @RequestParam("userId")  @NotNull UUID userId,
            @RequestParam("description") @Size(min = 3, max = 500) String description) {
        log.debug("Requesting help");

        try {
            emailSenderService.sendHelpRequest(object, String.valueOf(userId), description);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        }
        catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Operation(summary = "Send order confirmation", description = "This endpoint allows a user to send an order confirmation email. The request includes details such as the user ID and the products in the order, all provided via a form. The request is validated, and if authorized, an email is sent to the user with the order confirmation.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Email sent"),
            @ApiResponse(responseCode = "400", description = "Email not sent"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error"),
    })
    @PostMapping("/order-confirmation")
    @PreAuthorize("(hasRole('BUYER') or hasRole('SELLER')) and #userId == @authService.getCurrentUserUUID()")
    public ResponseEntity<Void> sendOrderConfirmation(@RequestParam("userID") UUID userId, @RequestParam("products") String productsJSON) {

        List<ProductCartDTO> products;
        Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
        try {
            // Gson converte la stringa JSON in una lista di oggetti
            products = gson.fromJson(productsJSON, new TypeToken<List<ProductCartDTO>>() {}.getType());

            emailSenderService.sendOrderConfirmation(userId.toString(), products);

            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }
}
