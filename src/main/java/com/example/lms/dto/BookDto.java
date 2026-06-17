package com.example.lms.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Data Transfer Object for Book details")
public record BookDto(
    @Schema(description = "Unique identifier of the book", example = "1")
    Long id,

    @Schema(description = "Title of the book", example = "The Great Gatsby")
    String title,

    @Schema(description = "Author of the book", example = "F. Scott Fitzgerald")
    String author,

    @Schema(description = "ISBN of the book", example = "978-3-16-148410-0")
    String isbn,

    @Schema(description = "Publication date of the book", example = "1925-04-10")
    LocalDate publishedDate
) {} 