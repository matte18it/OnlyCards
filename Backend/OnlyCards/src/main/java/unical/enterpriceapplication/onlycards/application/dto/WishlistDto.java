package unical.enterpriceapplication.onlycards.application.dto;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)

public class WishlistDto {

    private UUID id;

    private String name;
  
    private List<UserWishlistDto> accounts = new ArrayList<>();


    private List<ProductWishlistDto> products = new ArrayList<>();

    private LocalDateTime lastUpdate;
    private String token;
    private Boolean isPublic;


}
