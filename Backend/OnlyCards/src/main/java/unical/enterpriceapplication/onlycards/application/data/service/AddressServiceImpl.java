package unical.enterpriceapplication.onlycards.application.data.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import unical.enterpriceapplication.onlycards.application.data.entities.accounts.Address;
import unical.enterpriceapplication.onlycards.application.data.entities.accounts.User;
import unical.enterpriceapplication.onlycards.application.data.repository.AddressRepository;
import unical.enterpriceapplication.onlycards.application.dto.AddressDto;
import unical.enterpriceapplication.onlycards.application.dto.UserDTO;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AddressServiceImpl implements AddressService {
    private final AddressRepository addressRepository;
    private final UserService userService;
    private final ModelMapper modelMapper;

    @Override
    public void updateAddressById(AddressDto address, UUID addressId) {
        Optional<Address> addressToUpdate = addressRepository.findById(addressId);
        Address addressEntity = addressToUpdate.get();

        addressEntity.setCity(address.getCity());
        addressEntity.setState(address.getState());
        addressEntity.setStreet(address.getStreet());
        addressEntity.setZip(address.getZip());
        addressEntity.setDefaultAddress(address.getDefaultAddress());
        addressEntity.setName(address.getName());
        addressEntity.setSurname(address.getSurname());
        addressEntity.setTelephoneNumber(address.getTelephoneNumber());
        addressEntity.setWeekendDelivery(address.getWeekendDelivery());

        addressRepository.save(addressEntity);

        // Prendo gli altri indirizzi dell'utente settati come default e li setto a false se l'indirizzo appena modificato è settato come default
        if (address.getDefaultAddress()) {
            UserDTO user = userService.getUserById(addressEntity.getUser().getId()).get();
            for (AddressDto a : user.getAddresses()) {
                if (!a.getId().equals(addressId)) {
                    a.setDefaultAddress(false);
                    updateAddressById(a, a.getId());
                }
            }
        }
    }

    @Override
    public void addAddress(AddressDto address, UUID userId) {
        Address addressEntity = new Address();
        addressEntity.setCity(address.getCity());
        addressEntity.setState(address.getState());
        addressEntity.setStreet(address.getStreet());
        addressEntity.setZip(address.getZip());
        addressEntity.setDefaultAddress(address.getDefaultAddress());
        addressEntity.setName(address.getName());
        addressEntity.setSurname(address.getSurname());
        addressEntity.setTelephoneNumber(address.getTelephoneNumber());
        addressEntity.setWeekendDelivery(address.getWeekendDelivery());

        UserDTO user = userService.getUserById(userId).get();
        User userEntity = modelMapper.map(user, User.class);
        addressEntity.setUser(userEntity);

        addressRepository.save(addressEntity);

        // Prendo gli altri indirizzi dell'utente settati come default e li setto a false se l'indirizzo appena aggiunto è settato come default
        if (address.getDefaultAddress()) {
            for (AddressDto a : user.getAddresses()) {
                if (!a.getId().equals(addressEntity.getId())) {
                    a.setDefaultAddress(false);
                    updateAddressById(a, a.getId());
                }
            }
        }
    }

    @Override
    public void deleteAddressById(UUID addressId) {
        addressRepository.deleteById(addressId);
    }

    @Override
    public AddressDto getAddressById(UUID addressId) {
        Optional<Address> address = addressRepository.findById(addressId);
        return address.map(this::convertToDto).orElse(null);
    }

    @Override
    public List<AddressDto> getAddressesForUser(UUID userId) {
        log.debug("Fetching addresses for user with ID: {}", userId);

        // Recupera gli indirizzi dal repository
        List<Address> addresses = addressRepository.findByUserId(userId);

        // Aggiungi log per vedere se ci sono indirizzi
        log.debug("Found addresses: {}", addresses);

        // Verifica se gli indirizzi sono null o vuoti
        if (addresses == null || addresses.isEmpty()) {
            log.debug("No addresses found for user: {}", userId);
            return Collections.emptyList();
        }

        // Mappa gli indirizzi ad AddressDto
        List<AddressDto> addressDtos = addresses.stream()
                .map(address -> modelMapper.map(address, AddressDto.class))
                .collect(Collectors.toList());

        // Log per verificare il mapping
        log.debug("Mapped AddressDtos: {}", addressDtos);

        return addressDtos;
    }

    @Override
    public boolean isUserOwner(UUID addressId, UUID userId) {
        Optional<Address> address = addressRepository.findById(addressId);
        return address.map(value -> value.getUser().getId().equals(userId)).orElse(false);
    }


    private AddressDto convertToDto(Address address) {
        AddressDto dto = new AddressDto();
        dto.setId(address.getId());
        dto.setState(address.getState());
        dto.setCity(address.getCity());
        dto.setStreet(address.getStreet());
        dto.setZip(address.getZip());
        dto.setName(address.getName());
        dto.setSurname(address.getSurname());
        dto.setTelephoneNumber(address.getTelephoneNumber());
        dto.setDefaultAddress(address.getDefaultAddress());
        dto.setWeekendDelivery(address.getWeekendDelivery());
        return dto;
    }
}
