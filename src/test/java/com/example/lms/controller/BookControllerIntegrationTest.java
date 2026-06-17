package com.example.lms.controller;

import com.example.lms.dto.BookDto;
import com.example.lms.dto.CreateBookRequestDto;
import com.example.lms.dto.UpdateBookRequestDto;
import com.example.lms.model.Book;
import com.example.lms.repository.BookRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional // Ensures tests are rolled back, keeping DB state clean
class BookControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository; // For setup and verification

    private Book book1;

    @BeforeEach
    void setUp() {
        // Clean up before each test if @Transactional is not sufficient or for specific scenarios
        bookRepository.deleteAll();

        book1 = Book.builder()
            .title("Initial Book")
            .author("Initial Author")
            .isbn("111222333")
            .publishedDate(LocalDate.of(2000, 1, 1))
            .isDeleted(false)
            .build();
        // No need to save here if tests manage their own data creation via API calls
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void bookCrudFlow_createRetrieveUpdateDelete() throws Exception {
        // 1. Create Book
        CreateBookRequestDto createDto = new CreateBookRequestDto(
            "CRUD Test Book", "CRUD Author", "000111222", LocalDate.of(2023, 1, 15)
        );

        MvcResult createResult = mockMvc.perform(post("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDto)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id", notNullValue()))
            .andExpect(jsonPath("$.title", is("CRUD Test Book")))
            .andExpect(jsonPath("$.isbn", is("000111222")))
            .andReturn();

        BookDto createdBook = objectMapper.readValue(createResult.getResponse().getContentAsString(), BookDto.class);
        Long bookId = createdBook.id();
        assertNotNull(bookId);

        // 2. Retrieve Book
        mockMvc.perform(get("/api/v1/books/{id}", bookId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id", is(bookId.intValue())))
            .andExpect(jsonPath("$.title", is("CRUD Test Book")));

        // 3. Update Book
        UpdateBookRequestDto updateDto = new UpdateBookRequestDto(
            "CRUD Test Book Updated", "CRUD Author Updated", null, // Keep ISBN same or provide new one
            LocalDate.of(2023, 2, 20)
        );

        mockMvc.perform(put("/api/v1/books/{id}", bookId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title", is("CRUD Test Book Updated")))
            .andExpect(jsonPath("$.author", is("CRUD Author Updated")));

        // Verify update in DB (optional, controller test primarily checks API contract)
        Book updatedDbBook = bookRepository.findById(bookId).orElseThrow();
        assertEquals("CRUD Test Book Updated", updatedDbBook.getTitle());

        // 4. Soft Delete Book
        mockMvc.perform(delete("/api/v1/books/{id}", bookId))
            .andExpect(status().isNoContent());

        // 5. Verify Soft Delete (Attempt to retrieve should fail with 404)
        mockMvc.perform(get("/api/v1/books/{id}", bookId))
            .andExpect(status().isNotFound());

        // Verify isDeleted flag in DB (requires a method to fetch including soft-deleted or check count)
        // This is more of a repository/service level test. For controller, not finding it is sufficient.
        // Optional: Check that the book is still in DB but marked as deleted if repository allows fetching all
        List<Book> allBooksIncludingDeleted = bookRepository.findAll(); // Standard findAll will honor @Where
        // To properly verify soft delete at DB level here, you'd need a repo method that ignores @Where.
        // For now, the 404 on GET is the key verification for this integration test.
        long activeBookCount = bookRepository.count(); // This will count only active books due to @Where
        assertEquals(0, activeBookCount, "No active books should remain after soft delete.");
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN")
    void addBook_duplicateIsbn_shouldFailWithConflict() throws Exception {
        // Create an initial book directly in repo or via API
        Book initialBook = Book.builder().title("Unique ISBN Book").author("Author").isbn("999888777").publishedDate(LocalDate.now()).build();
        bookRepository.save(initialBook);

        CreateBookRequestDto createDuplicateDto = new CreateBookRequestDto(
            "Another Book", "Another Author", "999888777", // Same ISBN
            LocalDate.of(2024, 1, 1)
        );

        mockMvc.perform(post("/api/v1/books")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createDuplicateDto)))
            .andExpect(status().isConflict()); // Expecting 409 Conflict
    }

    @Test
    @WithMockUser(roles = "LIBRARIAN") // Assuming GET all books also requires at least authenticated user, Librarian for consistency
    void getAllBooks_withPaginationAndSearch() throws Exception {
        bookRepository.save(Book.builder().title("Lord of the Rings").author("J.R.R. Tolkien").isbn("123").publishedDate(LocalDate.now()).build());
        bookRepository.save(Book.builder().title("The Hobbit").author("J.R.R. Tolkien").isbn("456").publishedDate(LocalDate.now()).build());
        bookRepository.save(Book.builder().title("A Game of Thrones").author("George R.R. Martin").isbn("789").publishedDate(LocalDate.now()).build());

        // Test pagination
        mockMvc.perform(get("/api/v1/books").param("page", "0").param("size", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(2)))
            .andExpect(jsonPath("$.totalElements", is(3)))
            .andExpect(jsonPath("$.totalPages", is(2)));

        // Test search by title
        mockMvc.perform(get("/api/v1/books").param("title", "Lord"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].title", is("Lord of the Rings")));

        // Test search by author
        mockMvc.perform(get("/api/v1/books").param("author", "Tolkien"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(2)));

        // Test search by title and author
         mockMvc.perform(get("/api/v1/books").param("title", "Game").param("author", "Martin"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].title", is("A Game of Thrones")));
    }
} 