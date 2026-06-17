package com.example.lms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "Data Transfer Object for updating an existing member. Only provided fields will be updated.")
public record UpdateMemberRequestDto(
    @Schema(description = "New name of the member. If provided, must not be empty.", example = "Johnathan Doe")
    @Size(min = 1, max = 255, message = "Name must be between 1 and 255 characters if provided")
    String name,

    @Schema(description = "New email of the member. If provided, must be a valid email and not empty.", example = "johnathan.doe@example.com")
    @Email(message = "Email should be a valid email address if provided")
    @Size(min = 1, max = 255, message = "Email must be between 1 and 255 characters if provided")
    String email,

    @Schema(description = "New join date of the member. If provided, must be in the past or present.", example = "2023-05-10")
    @PastOrPresent(message = "Join date must be in the past or present if provided")
    LocalDate joinDate
) {} 