package unical.enterpriceapplication.onlycards.application.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.UUID;

@Data
public class AddressDto {
    private UUID id;
    @NotNull
    private Boolean defaultAddress;
    @NotNull
    private Boolean weekendDelivery;
    @NotNull
    private String state;
    @NotNull
    private String city;
    @NotNull
    private String street;
    @NotNull
    private String zip;
    @NotNull
    private String name;
    @NotNull
    private String surname;
    @NotNull
    private String telephoneNumber;
}
