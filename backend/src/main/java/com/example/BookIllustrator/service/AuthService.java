package com.example.BookIllustrator.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.BookIllustrator.dto.auth.request.AuthRequest;
import com.example.BookIllustrator.dto.auth.response.AuthResponse;
import com.example.BookIllustrator.entity.User;
import com.example.BookIllustrator.repository.UserRepository;
import com.example.BookIllustrator.util.JWTUtil;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final JWTUtil jwtUtil;
    
    // @Transactional (all-or-nothing)
    @Transactional
    public AuthResponse authenticate(AuthRequest authRequest) {
        try {
            String email = authRequest.getEmail();
            String name = authRequest.getName();
            User user = userRepository.findByEmail(email)
                    .map(existingUser -> {
                        existingUser.setName(name); // Update the name if it has changed
                        return userRepository.save(existingUser);
                    })
                    .orElseGet(() -> {
                        User newUser = new User();
                        newUser.setEmail(email);
                        newUser.setName(name);
                        return userRepository.save(newUser);
                    });
            String token = jwtUtil.generateJWTToken(user.getEmail(), user.getId());
            return new AuthResponse(token, user.getEmail());
        } catch (Exception e) {
            throw new RuntimeException("Authentication failed: " + e.getMessage());
        }
    }


}
