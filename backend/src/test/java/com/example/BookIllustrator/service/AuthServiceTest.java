package com.example.BookIllustrator.service;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.BookIllustrator.dto.auth.request.AuthRequest;
import com.example.BookIllustrator.dto.auth.response.AuthResponse;
import com.example.BookIllustrator.entity.User;
import com.example.BookIllustrator.repository.UserRepository;
import com.example.BookIllustrator.util.JWTUtil;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JWTUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void authenticateCreatesUserWhenEmailDoesNotExist() {
        AuthRequest request = new AuthRequest("New Reader", "reader@example.com");
        User createdUser = new User();
        createdUser.setId(42L);
        createdUser.setName("New Reader");
        createdUser.setEmail("reader@example.com");

        when(userRepository.findByEmail("reader@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(createdUser);
        when(jwtUtil.generateJWTToken("reader@example.com", 42L)).thenReturn("token-123");

        AuthResponse response = authService.authenticate(request);

        assertEquals("token-123", response.getToken());
        assertEquals("reader@example.com", response.getEmail());
        verify(userRepository).findByEmail("reader@example.com");
        verify(userRepository).save(any(User.class));
        verify(jwtUtil).generateJWTToken("reader@example.com", 42L);
    }

    @Test
    void authenticateUpdatesExistingUserWhenEmailAlreadyExists() {
        AuthRequest request = new AuthRequest("Updated Name", "existing@example.com");
        User existingUser = new User();
        existingUser.setId(7L);
        existingUser.setName("Original Name");
        existingUser.setEmail("existing@example.com");

        User savedUser = new User();
        savedUser.setId(7L);
        savedUser.setName("Updated Name");
        savedUser.setEmail("existing@example.com");

        when(userRepository.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));
        when(userRepository.save(existingUser)).thenReturn(savedUser);
        when(jwtUtil.generateJWTToken("existing@example.com", 7L)).thenReturn("token-456");

        AuthResponse response = authService.authenticate(request);

        assertNotNull(response);
        assertEquals("token-456", response.getToken());
        assertEquals("existing@example.com", response.getEmail());
        assertEquals("Updated Name", existingUser.getName());
        verify(userRepository).findByEmail("existing@example.com");
        verify(userRepository).save(existingUser);
        verify(jwtUtil).generateJWTToken("existing@example.com", 7L);
        verify(userRepository, times(1)).save(any(User.class));
    }
}