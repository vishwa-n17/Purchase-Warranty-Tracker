package com.purchasewarrantytracker.service;

import com.purchasewarrantytracker.model.User;
import com.purchasewarrantytracker.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Test
    void signupCreatesUserWithEncodedPassword() {
        User input = new User(null, "John Doe", "john@example.com", "password123", null);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded-password");

        User saved = new User(1L, "John Doe", "john@example.com", "encoded-password", LocalDateTime.now());
        when(userRepository.save(any(User.class))).thenReturn(saved);

        User result = userService.signup(input);

        assertEquals(1L, result.getId());
        assertEquals("john@example.com", result.getEmail());
        verify(passwordEncoder).encode("password123");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void signupRejectsDuplicateEmail() {
        User input = new User(null, "John Doe", "john@example.com", "password123", null);
        when(userRepository.existsByEmail("john@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.signup(input));
        verify(userRepository, never()).save(any());
    }

    @Test
    void signupRejectsShortPassword() {
        User input = new User(null, "John Doe", "john@example.com", "12345", null);

        assertThrows(IllegalArgumentException.class, () -> userService.signup(input));
        verify(userRepository, never()).save(any());
    }

    @Test
    void signupRejectsBlankName() {
        User input = new User(null, "  ", "john@example.com", "password123", null);

        assertThrows(IllegalArgumentException.class, () -> userService.signup(input));
        verify(userRepository, never()).save(any());
    }

    @Test
    void loginSucceedsWithValidCredentials() {
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(new User(1L, "John Doe", "john@example.com", "encoded-password", LocalDateTime.now())));
        when(passwordEncoder.matches("password123", "encoded-password")).thenReturn(true);

        User result = userService.login("john@example.com", "password123");

        assertEquals(1L, result.getId());
        assertEquals("John Doe", result.getName());
    }

    @Test
    void loginFailsWithInvalidPassword() {
        when(userRepository.findByEmail("john@example.com"))
                .thenReturn(Optional.of(new User(1L, "John Doe", "john@example.com", "encoded-password", LocalDateTime.now())));
        when(passwordEncoder.matches("wrong-password", "encoded-password")).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> userService.login("john@example.com", "wrong-password"));
    }

    @Test
    void loginFailsWhenUserNotFound() {
        when(userRepository.findByEmail("unknown@example.com")).thenReturn(Optional.empty());

        assertThrows(IllegalArgumentException.class, () -> userService.login("unknown@example.com", "password123"));
    }
}
