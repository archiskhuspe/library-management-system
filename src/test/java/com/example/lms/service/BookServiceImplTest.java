package com.example.lms.service;

import com.example.lms.dto.BookDto;
import com.example.lms.dto.CreateBookRequestDto;
import com.example.lms.dto.UpdateBookRequestDto;
import com.example.lms.exception.DuplicateIsbnException;
import com.example.lms.exception.ResourceNotFoundException;
import com.example.lms.mapper.BookMapper;
import com.example.lms.model.Book;
import com.example.lms.repository.BookRepository;
import com.example.lms.repository.specification.BookSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookMapper bookMapper;

    @Mock
    private BookSpecification bookSpecification;

    @InjectMocks
    private BookServiceImpl bookServiceImpl;

    private Book book1;
    private BookDto bookDto1;
    private CreateBookRequestDto createBookRequestDto;
    private UpdateBookRequestDto updateBookRequestDto;

    @BeforeEach
    void setUp() {
        book1 = Book.builder()
            .id(1L)
            .title("Test Book 1")
            .author("Author 1")
            .isbn("1234567890")
            .publishedDate(LocalDate.now().minusYears(1))
            .isDeleted(false)
            .build();

        bookDto1 = new BookDto(1L, "Test Book 1", "Author 1", "1234567890", LocalDate.now().minusYears(1));
        createBookRequestDto = new CreateBookRequestDto("New Book", "New Author", "0987654321", LocalDate.now());
        updateBookRequestDto = new UpdateBookRequestDto("Updated Title", null, null, null);
    }

    @Test
    void addBook_success() {
        when(bookRepository.existsByIsbn(createBookRequestDto.isbn())).thenReturn(false);
        when(bookMapper.toEntity(createBookRequestDto)).thenReturn(book1); // Assuming book1 is what toEntity would return for this DTO
        when(bookRepository.save(any(Book.class))).thenReturn(book1);
        when(bookMapper.toDto(book1)).thenReturn(bookDto1);

        BookDto result = bookServiceImpl.addBook(createBookRequestDto);

        assertNotNull(result);
        assertEquals(bookDto1.title(), result.title());
        verify(bookRepository).existsByIsbn(createBookRequestDto.isbn());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void addBook_duplicateIsbn_active() {
        when(bookRepository.existsByIsbn(createBookRequestDto.isbn())).thenReturn(true);

        assertThrows(DuplicateIsbnException.class, () -> bookServiceImpl.addBook(createBookRequestDto));
        verify(bookRepository).existsByIsbn(createBookRequestDto.isbn());
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void addBook_duplicateIsbn_dataIntegrityViolation() {
        when(bookRepository.existsByIsbn(createBookRequestDto.isbn())).thenReturn(false);
        when(bookMapper.toEntity(createBookRequestDto)).thenReturn(new Book()); // some book entity
        when(bookRepository.save(any(Book.class))).thenThrow(new DataIntegrityViolationException("Duplicate ISBN from DB"));

        DuplicateIsbnException exception = assertThrows(DuplicateIsbnException.class, () -> bookServiceImpl.addBook(createBookRequestDto));
        assertTrue(exception.getMessage().contains("already exists (possibly soft-deleted)"));

        verify(bookRepository).existsByIsbn(createBookRequestDto.isbn());
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void getBookById_found() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));
        when(bookMapper.toDto(book1)).thenReturn(bookDto1);

        BookDto result = bookServiceImpl.getBookById(1L);

        assertNotNull(result);
        assertEquals(bookDto1.title(), result.title());
        verify(bookRepository).findById(1L);
    }

    @Test
    void getBookById_notFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookServiceImpl.getBookById(1L));
        verify(bookRepository).findById(1L);
    }

    @Test
    void getAllBooks_success() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("title"));
        Page<Book> bookPage = new PageImpl<>(Collections.singletonList(book1), pageable, 1);
        Specification<Book> spec = Specification.where(null);

        when(bookSpecification.getBooksByCriteria(any(), any())).thenReturn(spec);
        when(bookRepository.findAll(spec, pageable)).thenReturn(bookPage);
        when(bookMapper.toDto(book1)).thenReturn(bookDto1);

        Page<BookDto> result = bookServiceImpl.getAllBooks(pageable, null, null);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(bookDto1.title(), result.getContent().get(0).title());
        verify(bookRepository).findAll(spec, pageable);
    }

     @Test
    void getAllBooks_withSearchCriteria_success() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by("title"));
        String titleSearch = "Test";
        String authorSearch = "Author";
        Page<Book> bookPage = new PageImpl<>(Collections.singletonList(book1), pageable, 1);
        Specification<Book> spec = (root, query, cb) -> cb.conjunction(); // Dummy spec

        when(bookSpecification.getBooksByCriteria(titleSearch, authorSearch)).thenReturn(spec);
        when(bookRepository.findAll(spec, pageable)).thenReturn(bookPage);
        when(bookMapper.toDto(book1)).thenReturn(bookDto1);

        Page<BookDto> result = bookServiceImpl.getAllBooks(pageable, titleSearch, authorSearch);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(bookDto1.title(), result.getContent().get(0).title());
        verify(bookSpecification).getBooksByCriteria(titleSearch, authorSearch);
        verify(bookRepository).findAll(spec, pageable);
    }

    @Test
    void getAllBooks_emptyResult() {
        Pageable pageable = PageRequest.of(0, 10);
        Specification<Book> spec = Specification.where(null); // No specific criteria

        when(bookSpecification.getBooksByCriteria(null, null)).thenReturn(spec);
        // Mock repository to return an empty page
        when(bookRepository.findAll(spec, pageable)).thenReturn(Page.empty(pageable));

        Page<BookDto> result = bookServiceImpl.getAllBooks(pageable, null, null);

        assertNotNull(result, "Result page should not be null");
        assertTrue(result.isEmpty(), "Result page should be empty");
        assertEquals(0, result.getTotalElements(), "Total elements should be 0");
        assertEquals(0, result.getContent().size(), "Content list should be empty");

        verify(bookSpecification).getBooksByCriteria(null, null);
        verify(bookRepository).findAll(spec, pageable);
        // Ensure bookMapper.toDto is never called if the page is empty
        verify(bookMapper, never()).toDto(any(Book.class));
    }

    @Test
    void updateBook_success_noIsbnChange() {
        UpdateBookRequestDto localUpdateDto = new UpdateBookRequestDto("Updated Title", "Updated Author", book1.getIsbn(), LocalDate.now());
        Book updatedBookEntity = Book.builder().id(1L).title("Updated Title").author("Updated Author").isbn(book1.getIsbn()).publishedDate(LocalDate.now()).build();
        BookDto updatedBookDto = new BookDto(1L, "Updated Title", "Updated Author", book1.getIsbn(), LocalDate.now());

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));
        when(bookRepository.save(any(Book.class))).thenReturn(updatedBookEntity);
        when(bookMapper.toDto(updatedBookEntity)).thenReturn(updatedBookDto);

        BookDto result = bookServiceImpl.updateBook(1L, localUpdateDto);

        assertNotNull(result);
        assertEquals("Updated Title", result.title());
        assertEquals("Updated Author", result.author());
        verify(bookRepository).findById(1L);
        verify(bookRepository).save(any(Book.class));
        verify(bookRepository, never()).existsByIsbn(anyString()); // ISBN not changed, so no check
    }

    @Test
    void updateBook_success_withIsbnChange() {
        UpdateBookRequestDto localUpdateDto = new UpdateBookRequestDto("Updated Title", "Updated Author", "111222333", LocalDate.now());
        Book updatedBookEntity = Book.builder().id(1L).title("Updated Title").author("Updated Author").isbn("111222333").publishedDate(LocalDate.now()).build();
        BookDto updatedBookDto = new BookDto(1L, "Updated Title", "Updated Author", "111222333", LocalDate.now());

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));
        when(bookRepository.existsByIsbn("111222333")).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(updatedBookEntity);
        when(bookMapper.toDto(updatedBookEntity)).thenReturn(updatedBookDto);

        BookDto result = bookServiceImpl.updateBook(1L, localUpdateDto);

        assertNotNull(result);
        assertEquals("111222333", result.isbn());
        verify(bookRepository).findById(1L);
        verify(bookRepository).existsByIsbn("111222333");
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void updateBook_notFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookServiceImpl.updateBook(1L, updateBookRequestDto));
        verify(bookRepository).findById(1L);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void updateBook_isbnChange_duplicateIsbn_active() {
        UpdateBookRequestDto localUpdateDto = new UpdateBookRequestDto(null, null, "777888999", null);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1)); // Original ISBN is "1234567890"
        when(bookRepository.existsByIsbn("777888999")).thenReturn(true);

        assertThrows(DuplicateIsbnException.class, () -> bookServiceImpl.updateBook(1L, localUpdateDto));
        verify(bookRepository).findById(1L);
        verify(bookRepository).existsByIsbn("777888999");
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void updateBook_isbnChange_duplicateIsbn_dataIntegrity() {
        UpdateBookRequestDto localUpdateDto = new UpdateBookRequestDto(null, null, "777888999", null);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));
        when(bookRepository.existsByIsbn("777888999")).thenReturn(false);
        // Simulate save causes DataIntegrityViolationException
        when(bookRepository.save(any(Book.class))).thenThrow(new DataIntegrityViolationException("DB duplicate ISBN"));

        DuplicateIsbnException exception = assertThrows(DuplicateIsbnException.class, () -> bookServiceImpl.updateBook(1L, localUpdateDto));
        assertTrue(exception.getMessage().contains("already exists (possibly soft-deleted)"));

        verify(bookRepository).findById(1L);
        verify(bookRepository).existsByIsbn("777888999");
        verify(bookRepository).save(any(Book.class));
    }

    @Test
    void updateBook_noChanges() {
        UpdateBookRequestDto noChangeDto = new UpdateBookRequestDto(null, null, null, null);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));
        when(bookMapper.toDto(book1)).thenReturn(bookDto1); // Map existing entity

        BookDto result = bookServiceImpl.updateBook(1L, noChangeDto);

        assertNotNull(result);
        assertEquals(bookDto1.title(), result.title()); // Should be original details
        verify(bookRepository).findById(1L);
        verify(bookRepository, never()).save(any(Book.class)); // Save should not be called
        verify(bookMapper).toDto(book1); // Ensure it's mapping the original book
    }


    @Test
    void softDeleteBook_success() {
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book1));
        doNothing().when(bookRepository).deleteById(1L);

        bookServiceImpl.softDeleteBook(1L);

        verify(bookRepository).findById(1L);
        verify(bookRepository).deleteById(1L);
    }

    @Test
    void softDeleteBook_notFound() {
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> bookServiceImpl.softDeleteBook(1L));
        verify(bookRepository).findById(1L);
        verify(bookRepository, never()).deleteById(anyLong());
    }
} 