package com.keshav.service;

import com.keshav.dto.*;

public interface IAuthService {

    RegisterResponseDTO register(RegisterRequestDTO request);

    LoginResponseDTO login(LoginRequestDTO request);

    ApiResponseDTO changePassword(String email, ChangePasswordRequestDTO request);

    ApiResponseDTO forgotPassword(ForgotPasswordRequestDTO request);

    ApiResponseDTO resetPassword(ResetPasswordRequestDTO request);
}