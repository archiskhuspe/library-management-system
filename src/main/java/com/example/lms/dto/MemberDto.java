package com.example.lms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Data Transfer Object for Member details")
public record MemberDto(
    @Schema(description = "Unique identifier of the member", example = "1")
    Long id,

    @Schema(description = "Name of the member", example = "John Doe")
    String name,

    @Schema(description = "Email address of the member", example = "john.doe@example.com")
    String email,

    @Schema(description = "Date when the member joined", example = "2023-01-15")
    LocalDate joinDate
) {} 