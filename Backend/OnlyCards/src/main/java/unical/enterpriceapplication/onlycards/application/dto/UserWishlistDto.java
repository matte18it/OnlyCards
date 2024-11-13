package unical.enterpriceapplication.onlycards.application.dto;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserWishlistDto {
    private UUID id;
   
    private String username;
    
    private String keyOwnership;
    private String valueOwnership;
}
