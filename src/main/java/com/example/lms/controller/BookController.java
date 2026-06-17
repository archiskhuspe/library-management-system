package com.example.lms.controller;

import com.example.lms.dto.BookDto;
import com.example.lms.dto.CreateBookRequestDto;
import com.example.lms.dto.UpdateBookRequestDto;
import com.example.lms.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/books")
@RequiredArgsConstructor
@Tag(name = "Book Management", description = "APIs for managing books in the library")
public class BookController {

    private final BookService bookService;

    @Operation(summary = "Add a new book", description = "Creates a new book entry in the library.")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Book created successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "409", description = "Duplicate ISBN",
                content = @Content(mediaType = "application/json"))
    })
    @PostMapping
    public ResponseEntity<BookDto> addBook(@Valid @RequestBody CreateBookRequestDto createBookRequestDto) {
        BookDto newBook = bookService.addBook(createBookRequestDto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
            .path("/{id}").buildAndExpand(newBook.id()).toUri();
        return ResponseEntity.created(location).body(newBook);
    }

    @Operation(summary = "Get a book by ID", description = "Retrieves details of a specific book by its ID.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Book found",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDto.class))),
        @ApiResponse(responseCode = "404", description = "Book not found",
                content = @Content(mediaType = "application/json"))
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookDto> getBookById(
        @Parameter(description = "ID of the book to retrieve", required = true, example = "1")
        @PathVariable Long id) {
        BookDto bookDto = bookService.getBookById(id);
        return ResponseEntity.ok(bookDto);
    }

    @Operation(summary = "List all books", description = "Retrieves a paginated list of books. Can be filtered by title and author.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Books listed successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = Page.class))) // Note: Schema should be Page<BookDto>
    })
    @GetMapping
    public ResponseEntity<Page<BookDto>> getAllBooks(
        @Parameter(description = "Pagination and sorting parameters") @PageableDefault(size = 10, sort = "title") Pageable pageable,
        @Parameter(description = "Filter by book title (case-insensitive, partial match)", example = "Lord")
        @RequestParam(required = false) String title,
        @Parameter(description = "Filter by book author (case-insensitive, partial match)", example = "Tolkien")
        @RequestParam(required = false) String author) {
        Page<BookDto> bookPage = bookService.getAllBooks(pageable, title, author);
        return ResponseEntity.ok(bookPage);
    }

    @Operation(summary = "Update book details", description = "Updates details of an existing book. Only provided fields are updated.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Book updated successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = BookDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "404", description = "Book not found",
                content = @Content(mediaType = "application/json")),
        @ApiResponse(responseCode = "409", description = "Duplicate ISBN if ISBN is changed to an existing one",
                content = @Content(mediaType = "application/json"))
    })
    @PutMapping("/{id}")
    public ResponseEntity<BookDto> updateBook(
        @Parameter(description = "ID of the book to update", required = true, example = "1")
        @PathVariable Long id,
        @Valid @RequestBody UpdateBookRequestDto updateBookRequestDto) {
        BookDto updatedBook = bookService.updateBook(id, updateBookRequestDto);
        return ResponseEntity.ok(updatedBook);
    }

    @Operation(summary = "Soft delete a book", description = "Marks a book as deleted. The book is not physically removed from the database.")
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Book soft-deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Book not found",
                content = @Content(mediaType = "application/json"))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> softDeleteBook(
        @Parameter(description = "ID of the book to soft delete", required = true, example = "1")
        @PathVariable Long id) {
        bookService.softDeleteBook(id);
        return ResponseEntity.noContent().build();
    }
} 