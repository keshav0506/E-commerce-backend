package com.keshav.controller;

import com.keshav.dto.AddressRequestDTO;
import com.keshav.dto.AddressResponseDTO;
import com.keshav.service.IAddressService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/addresses")
public class AddressController {

    private final IAddressService addressService;

    public AddressController(IAddressService addressService) {
        this.addressService = addressService;
    }

    @PostMapping
    public ResponseEntity<AddressResponseDTO> addAddress(
            @Valid @RequestBody AddressRequestDTO dto) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(addressService.addAddress(dto));
    }

    @GetMapping
    public ResponseEntity<List<AddressResponseDTO>> getMyAddresses() {

        return ResponseEntity.ok(
                addressService.getMyAddresses()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<AddressResponseDTO> getAddressById(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                addressService.getAddressById(id)
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressResponseDTO> updateAddress(
            @PathVariable Long id,
            @Valid @RequestBody AddressRequestDTO dto) {

        return ResponseEntity.ok(
                addressService.updateAddress(id, dto)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAddress(
            @PathVariable Long id) {

        addressService.deleteAddress(id);

        return ResponseEntity.noContent().build();
    }
}