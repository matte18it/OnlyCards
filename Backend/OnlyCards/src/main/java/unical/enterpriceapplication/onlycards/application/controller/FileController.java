package unical.enterpriceapplication.onlycards.application.controller;

import java.io.FileNotFoundException;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import unical.enterpriceapplication.onlycards.application.core.service.FirebaseFolders;
import unical.enterpriceapplication.onlycards.application.core.service.FirebaseStorageService;

@RestController
@RequestMapping(path = "/v1/files")
@RequiredArgsConstructor
@Validated
@Slf4j
public class FileController {
    // ----- VARIABILI -----
    private final FirebaseStorageService firebaseStorageService;

    // ----- METODI -----
    @Operation(summary = "Upload a file", description = "This endpoint allows an authorized user (Seller, Admin, or Buyer) to upload a file to Firebase storage. The file, typically an image, is associated with the user's ID and saved in a specific folder within Firebase. The file is validated, and if the upload is successful, it is stored securely in the designated location.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "File uploaded"),
            @ApiResponse(responseCode = "400", description = "File not uploaded"),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "500", description = "Internal server error"),
    })
    @PostMapping(produces = "application/json", consumes = "multipart/form-data")
    @PreAuthorize("(hasRole('SELLER') or hasRole('ADMIN') or hasRole('BUYER')) and #userId == @authService.getCurrentUserUUID()")
    public ResponseEntity<Void> uploadFile(
            @RequestParam("file") @NotNull MultipartFile file,
            @RequestParam("userId") @NotNull UUID userId
    ) {
        log.debug("Uploading file: {}", file.getOriginalFilename());

        try {
            firebaseStorageService.uploadFile(file, String.valueOf(userId), FirebaseFolders.USERS_PROFILE);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @Operation(summary = "Get a file", description = "This endpoint allows an authorized user (Seller, Admin, or Buyer) to retrieve a file from Firebase storage. The file, typically an image, is associated with the user's ID and retrieved from a specific folder within Firebase. If the file exists, its URL is returned to the user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "File found"),
            @ApiResponse(responseCode = "404", description = "File not found"),
            @ApiResponse(responseCode = "500", description = "Internal server error"),
    })
    @SecurityRequirements()
    @GetMapping(value = "/{userId}", produces = "application/json")
    public ResponseEntity<Map<String, String>> getFile(@PathVariable UUID userId) {
        log.debug("Getting file for user: {}", userId);

        try {
            String fileUrl = firebaseStorageService.getFile(String.valueOf(userId), FirebaseFolders.USERS_PROFILE);
            if (fileUrl != null) {
                return ResponseEntity.ok(Collections.singletonMap("url", fileUrl));
            } else {
                throw new FileNotFoundException("File not found for user: " + userId);
            }
        } catch (FileNotFoundException e) {
            log.error("Error retrieving file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            log.error("Internal error while retrieving file: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
