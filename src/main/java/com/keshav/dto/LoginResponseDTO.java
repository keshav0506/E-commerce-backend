package com.keshav.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponseDTO {

    private Long id;
    private String name;
    private String email;
    private String role;
    private String token;
}