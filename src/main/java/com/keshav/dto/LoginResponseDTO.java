package com.keshav.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponseDTO {

    private Long id;
    private String name;
    private String email;
    private String role;
    private String token;
    @Builder.Default
    private String tokenType = "Bearer";
    private String supplierStatus;

    public LoginResponseDTO(Long id, String name, String email, String role, String token) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.token = token;
        this.tokenType = "Bearer";
    }
}