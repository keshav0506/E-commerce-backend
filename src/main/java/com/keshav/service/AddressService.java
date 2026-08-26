package com.keshav.service;

import com.keshav.dto.AddressRequestDTO;
import com.keshav.dto.AddressResponseDTO;
import com.keshav.entity.Address;
import com.keshav.entity.User;
import com.keshav.exception.AddressNotFoundException;
import com.keshav.exception.UserNotFoundException;
import com.keshav.repository.AddressRepository;
import com.keshav.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService implements IAddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressService(
            AddressRepository addressRepository,
            UserRepository userRepository) {

        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    @Override
    public AddressResponseDTO addAddress(AddressRequestDTO dto) {

        User user = getCurrentUser();

        // If this address is default,
        // remove default status from existing addresses
        if (dto.isDefault()) {
            List<Address> existingAddresses =
                    addressRepository.findByUser(user);

            for (Address address : existingAddresses) {
                address.setDefault(false);
            }

            addressRepository.saveAll(existingAddresses);
        }

        Address address = new Address();

        address.setFullName(dto.getFullName());
        address.setPhone(dto.getPhone());
        address.setAddressLine(dto.getAddressLine());
        address.setCity(dto.getCity());
        address.setState(dto.getState());
        address.setPostalCode(dto.getPostalCode());
        address.setCountry(dto.getCountry());
        address.setDefault(dto.isDefault());
        address.setUser(user);

        Address savedAddress =
                addressRepository.save(address);

        return convertToResponseDTO(savedAddress);
    }

    @Override
    public List<AddressResponseDTO> getMyAddresses() {

        User user = getCurrentUser();

        return addressRepository.findByUser(user)
                .stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    @Override
    public AddressResponseDTO getAddressById(Long id) {

        User user = getCurrentUser();

        Address address = addressRepository
                .findByIdAndUser(id, user)
                .orElseThrow(() ->
                        new AddressNotFoundException(
                                "Address not found with id: " + id
                        )
                );

        return convertToResponseDTO(address);
    }

    @Override
    public AddressResponseDTO updateAddress(
            Long id,
            AddressRequestDTO dto) {

        User user = getCurrentUser();

        Address existingAddress =
                addressRepository.findByIdAndUser(id, user)
                        .orElseThrow(() ->
                                new AddressNotFoundException(
                                        "Address not found with id: " + id
                                )
                        );

        // If changing this address to default
        if (dto.isDefault()) {

            List<Address> existingAddresses =
                    addressRepository.findByUser(user);

            for (Address address : existingAddresses) {
                address.setDefault(false);
            }

            addressRepository.saveAll(existingAddresses);
        }

        existingAddress.setFullName(dto.getFullName());
        existingAddress.setPhone(dto.getPhone());
        existingAddress.setAddressLine(dto.getAddressLine());
        existingAddress.setCity(dto.getCity());
        existingAddress.setState(dto.getState());
        existingAddress.setPostalCode(dto.getPostalCode());
        existingAddress.setCountry(dto.getCountry());
        existingAddress.setDefault(dto.isDefault());

        Address updatedAddress =
                addressRepository.save(existingAddress);

        return convertToResponseDTO(updatedAddress);
    }

    @Override
    public void deleteAddress(Long id) {

        User user = getCurrentUser();

        Address address =
                addressRepository.findByIdAndUser(id, user)
                        .orElseThrow(() ->
                                new AddressNotFoundException(
                                        "Address not found with id: " + id
                                )
                        );

        addressRepository.delete(address);
    }

    private User getCurrentUser() {

        Authentication authentication =
                SecurityContextHolder
                        .getContext()
                        .getAuthentication();

        String email = authentication.getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found with email: " + email
                        )
                );
    }

    private AddressResponseDTO convertToResponseDTO(
            Address address) {

        return new AddressResponseDTO(
                address.getId(),
                address.getFullName(),
                address.getPhone(),
                address.getAddressLine(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry(),
                address.isDefault()
        );
    }
}