package com.example.BookIllustrator.controller;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.http.ResponseEntity;
import com.example.BookIllustrator.dto.auth.request.AuthRequest;
import com.example.BookIllustrator.service.AuthService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import com.example.BookIllustrator.dto.auth.response.AuthResponse;
import com.example.BookIllustrator.dto.api.ApiResponse;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody AuthRequest authRequest) {
        AuthResponse response = authService.authenticate(authRequest);
        return ResponseEntity.ok(new ApiResponse<>(200, "Login successful", response));
    }   

}
