package com.example.demo.dto.auth;

import com.example.demo.constant.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

public record RegisterRequest(
    @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        @Size(min = 5, max = 255, message = "Email must be between 5 and 255")
        String email,
    @NotBlank(message = "Password is required")
        @Size(min = 8, max = 255, message = "Password must be between 8 and 255")
        String password,
    @NotBlank(message = "First name is required")
        @Size(min = 1, max = 100, message = "First name must be between 1 and 100")
        String firstName,
    @NotBlank(message = "Last name is required")
        @Size(min = 1, max = 100, message = "Last name must be between 1 and 100")
        String lastName,
    @NotNull(message = "Role is required") UserRole role,
    String phone,
    @NotNull(message = "Birthdate is required") LocalDate birthdate) {}
