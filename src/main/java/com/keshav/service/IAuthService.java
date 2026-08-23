package com.keshav.service;

import com.keshav.dto.RegisterRequestDTO;
import com.keshav.dto.RegisterResponseDTO;

public interface IAuthService {

    RegisterResponseDTO register(RegisterRequestDTO request);
}