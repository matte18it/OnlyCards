package unical.enterpriceapplication.onlycards.application.data.service;

import unical.enterpriceapplication.onlycards.application.dto.AddressDto;

import java.util.List;
import java.util.UUID;

public interface AddressService {
    void updateAddressById(AddressDto address, UUID addressId);
    void addAddress(AddressDto address, UUID userId);
    void deleteAddressById(UUID addressId);
    List<AddressDto> getAddressesForUser(UUID userId);
    AddressDto getAddressById(UUID addressId);
    boolean isUserOwner(UUID addressId, UUID userId);
}
