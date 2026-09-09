package com.purchasewarrantytracker.controller;

import com.purchasewarrantytracker.config.SecurityConfig;
import com.purchasewarrantytracker.controller.HealthController;
import com.purchasewarrantytracker.exception.GlobalExceptionHandler;
import com.purchasewarrantytracker.model.HealthResponse;
import com.purchasewarrantytracker.model.User;
import com.purchasewarrantytracker.repository.UserRepository;
import com.purchasewarrantytracker.service.HealthService;
import com.purchasewarrantytracker.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = {AuthController.class, HealthController.class})
@ContextConfiguration(classes = {AuthController.class, SecurityConfig.class, GlobalExceptionHandler.class, HealthController.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private HealthService healthService;

    @Test
    void signupReturnsCreatedUser() throws Exception {
        User user = new User(1L, "John Doe", "john@example.com", "encoded", LocalDateTime.now());
        when(userService.signup(any(User.class))).thenReturn(user);

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"John Doe\",\"email\":\"john@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void signupRejectsDuplicateEmail() throws Exception {
        when(userService.signup(any(User.class))).thenThrow(new IllegalArgumentException("Email is already registered"));

        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"John Doe\",\"email\":\"john@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void signupRejectsInvalidRequest() throws Exception {
        mockMvc.perform(post("/api/auth/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"\",\"email\":\"invalid\",\"password\":\"123\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void loginReturnsUserOnValidCredentials() throws Exception {
        User user = new User(1L, "John Doe", "john@example.com", "encoded", LocalDateTime.now());
        when(userService.login("john@example.com", "password123")).thenReturn(user);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"john@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("John Doe"))
                .andExpect(jsonPath("$.email").value("john@example.com"));
    }

    @Test
    void loginRejectsInvalidCredentials() throws Exception {
        when(userService.login("john@example.com", "wrong-password"))
                .thenThrow(new IllegalArgumentException("Invalid email or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"john@example.com\",\"password\":\"wrong-password\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void meReturnsUnauthenticatedWhenNoSession() throws Exception {
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void protectedProductEndpointRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void healthEndpointIsPublic() throws Exception {
        when(healthService.getHealth()).thenReturn(new HealthResponse("UP", "Application is running", Instant.now()));

        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk());
    }

    @Test
    void changePasswordReturnsOkWhenValid() throws Exception {
        User user = new User(1L, "John Doe", "john@example.com", "encoded", LocalDateTime.now());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doNothing().when(userService).changePassword(eq(1L), eq("current123"), eq("newpassword123"));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList())
        );

        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"current123\",\"newPassword\":\"newpassword123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Password changed successfully"));

        SecurityContextHolder.clearContext();
    }

    @Test
    void changePasswordRejectsIncorrectCurrentPassword() throws Exception {
        User user = new User(1L, "John Doe", "john@example.com", "encoded", LocalDateTime.now());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doThrow(new IllegalArgumentException("Current password is incorrect"))
                .when(userService).changePassword(eq(1L), eq("wrong"), any(String.class));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList())
        );

        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"wrong\",\"newPassword\":\"newpassword123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        SecurityContextHolder.clearContext();
    }

    @Test
    void changePasswordRejectsShortNewPassword() throws Exception {
        User user = new User(1L, "John Doe", "john@example.com", "encoded", LocalDateTime.now());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        doThrow(new IllegalArgumentException("New password must be at least 6 characters"))
                .when(userService).changePassword(eq(1L), any(String.class), eq("123"));

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, Collections.emptyList())
        );

        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"current123\",\"newPassword\":\"123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));

        SecurityContextHolder.clearContext();
    }

    @Test
    void changePasswordReturnsUnauthenticatedWhenNoSession() throws Exception {
        mockMvc.perform(post("/api/auth/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"current123\",\"newPassword\":\"newpassword123\"}"))
                .andExpect(status().isUnauthorized());
    }
}
