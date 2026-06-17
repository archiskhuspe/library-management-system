package com.example.lms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "Data Transfer Object for creating a new book")
public record CreateBookRequestDto(
    @Schema(description = "Title of the book", example = "The Lord of the Rings", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Title cannot be blank")
    @Size(max = 255, message = "Title cannot exceed 255 characters")
    String title,

    @Schema(description = "Author of the book", example = "J.R.R. Tolkien", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Author cannot be blank")
    @Size(max = 255, message = "Author cannot exceed 255 characters")
    String author,

    @Schema(description = "ISBN of the book", example = "978-0618260274", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "ISBN cannot be blank")
    @Size(max = 20, message = "ISBN cannot exceed 20 characters") // ISBN-13 is 13 chars, ISBN-10 is 10. Max 20 for flexibility.
    String isbn,

    @Schema(description = "Publication date of the book", example = "1954-07-29")
    @PastOrPresent(message = "Published date must be in the past or present")
    LocalDate publishedDate
) {} 