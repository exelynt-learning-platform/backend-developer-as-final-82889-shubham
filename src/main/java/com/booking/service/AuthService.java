package com.booking.service;


import com.booking.dto.request.LoginRequest;
import com.booking.dto.response.LoginResponse;

public interface AuthService {

    LoginResponse login(LoginRequest request);
}