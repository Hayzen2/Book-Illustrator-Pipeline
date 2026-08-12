package com.example.BookIllustrator.dto.auth.response;

import jakarta.validation.constraints.Email;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthResponse {
    private String token;
    @Email(message = "Invalid email format")
    private String email;
}
