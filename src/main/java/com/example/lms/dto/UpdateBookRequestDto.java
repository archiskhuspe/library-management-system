package com.example.lms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

@Schema(description = "Data Transfer Object for updating an existing book. Only provided fields will be updated.")
public record UpdateBookRequestDto(
    @Schema(description = "New title of the book. If provided, must not be empty.", example = "The Silmarillion")
    @Size(min = 1, max = 255, message = "Title must be between 1 and 255 characters if provided")
    String title,

    @Schema(description = "New author of the book. If provided, must not be empty.", example = "J.R.R. Tolkien")
    @Size(min = 1, max = 255, message = "Author must be between 1 and 255 characters if provided")
    String author,

    @Schema(description = "New ISBN of the book. If provided, must not be empty.", example = "978-0618391110")
    @Size(min = 1, max = 20, message = "ISBN must be between 1 and 20 characters if provided")
    String isbn,

    @Schema(description = "New publication date of the book. If provided, must be in the past or present.", example = "1977-09-15")
    @PastOrPresent(message = "Published date must be in the past or present if provided")
    LocalDate publishedDate
) {} 