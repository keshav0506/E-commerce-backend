package com.keshav.service;

import com.keshav.dto.LoginRequestDTO;
import com.keshav.dto.LoginResponseDTO;
import com.keshav.dto.RegisterRequestDTO;
import com.keshav.dto.RegisterResponseDTO;

public interface IAuthService {

    RegisterResponseDTO register(RegisterRequestDTO request);
    LoginResponseDTO login(LoginRequestDTO request);
}