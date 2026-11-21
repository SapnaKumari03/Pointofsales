package com.j20_pos.service;

import com.j20_pos.dto.AuthRequest;
import com.j20_pos.dto.AuthResponse;
import com.j20_pos.dto.RegisterRequestDTO;
import com.j20_pos.dto.UserDTO;

public interface AuthService {

    AuthResponse register(RegisterRequestDTO request);

    AuthResponse login(AuthRequest request);

    UserDTO getCurrentUser();
}
