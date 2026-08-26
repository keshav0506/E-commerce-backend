package com.keshav.service;

import com.keshav.dto.AddressRequestDTO;
import com.keshav.dto.AddressResponseDTO;

import java.util.List;

public interface IAddressService {

    AddressResponseDTO addAddress(AddressRequestDTO dto);

    List<AddressResponseDTO> getMyAddresses();

    AddressResponseDTO getAddressById(Long id);

    AddressResponseDTO updateAddress(
            Long id,
            AddressRequestDTO dto
    );

    void deleteAddress(Long id);
}