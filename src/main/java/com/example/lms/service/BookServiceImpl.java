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
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class BookServiceImpl implements BookService {

    private static final Logger log = LoggerFactory.getLogger(BookServiceImpl.class);
    private final BookRepository bookRepository;
    private final BookMapper bookMapper;
    private final BookSpecification bookSpecification;

    @Override
    @Transactional
    public BookDto addBook(CreateBookRequestDto createBookRequestDto) {
        log.info("Attempting to add a new book with ISBN: {}", createBookRequestDto.isbn());

        // The database unique constraint on ISBN is the ultimate guard.
        // This check provides a slightly cleaner error before hitting the DB constraint.
        // Note: bookRepository.existsByIsbn respects the @Where(clause = "is_deleted=false")
        // So, this check is for *active* books. The DB constraint will prevent adding an ISBN
        // that exists even if the existing book record is soft-deleted.
        if (bookRepository.existsByIsbn(createBookRequestDto.isbn())) {
            log.warn("Attempt to add book with duplicate active ISBN: {}", createBookRequestDto.isbn());
            throw new DuplicateIsbnException(createBookRequestDto.isbn());
        }

        Book book = bookMapper.toEntity(createBookRequestDto);
        try {
            Book savedBook = bookRepository.save(book);
            log.info("Successfully added book with ID: {} and ISBN: {}", savedBook.getId(), savedBook.getIsbn());
            return bookMapper.toDto(savedBook);
        } catch (DataIntegrityViolationException e) {
            // This catch block handles the scenario where the DB unique constraint is violated,
            // potentially by an ISBN that exists on a soft-deleted record (which existsByIsbn wouldn't catch).
            log.warn("Data integrity violation while adding book, likely duplicate ISBN including soft-deleted: {}", createBookRequestDto.isbn(), e);
            // Check again, this time could be a race condition or a soft-deleted duplicate
            // For a more specific check against all records (including soft-deleted) for ISBN, a custom query would be needed
            // if the DB constraint message isn't descriptive enough or specific handling is required.
            // For now, re-throwing a generic or specific duplicate ISBN exception.
            throw new DuplicateIsbnException("Book with ISBN '" + createBookRequestDto.isbn() + "' already exists (possibly soft-deleted). Cause: " + e.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public BookDto getBookById(Long id) {
        log.debug("Fetching book by ID: {}", id);
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Book not found with ID: {}", id);
                return new ResourceNotFoundException("Book", "ID", id);
            });
        return bookMapper.toDto(book);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookDto> getAllBooks(Pageable pageable, String title, String author) {
        log.debug("Fetching all books. Page: {}, Size: {}, Title search: '{}', Author search: '{}'",
            pageable.getPageNumber(), pageable.getPageSize(), title, author);
        Specification<Book> spec = bookSpecification.getBooksByCriteria(title, author);
        Page<Book> bookPage = bookRepository.findAll(spec, pageable);
        log.info("Found {} books matching criteria.", bookPage.getTotalElements());
        return bookPage.map(bookMapper::toDto);
    }

    @Override
    @Transactional
    public BookDto updateBook(Long id, UpdateBookRequestDto updateBookRequestDto) {
        log.info("Attempting to update book with ID: {}", id);
        Book existingBook = bookRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Book not found for update with ID: {}", id);
                return new ResourceNotFoundException("Book", "ID", id);
            });

        boolean updated = false;

        if (StringUtils.hasText(updateBookRequestDto.title())) {
            existingBook.setTitle(updateBookRequestDto.title());
            updated = true;
        }
        if (StringUtils.hasText(updateBookRequestDto.author())) {
            existingBook.setAuthor(updateBookRequestDto.author());
            updated = true;
        }
        if (updateBookRequestDto.publishedDate() != null) {
            existingBook.setPublishedDate(updateBookRequestDto.publishedDate());
            updated = true;
        }

        // Handle ISBN change: check for duplicates if ISBN is being updated
        if (StringUtils.hasText(updateBookRequestDto.isbn()) && !updateBookRequestDto.isbn().equals(existingBook.getIsbn())) {
            log.info("ISBN is being updated for book ID: {}. New ISBN: {}, Old ISBN: {}", id, updateBookRequestDto.isbn(), existingBook.getIsbn());
            // Similar to add book, DB constraint is the final check.
            // existsByIsbn checks active records.
            if (bookRepository.existsByIsbn(updateBookRequestDto.isbn())) {
                log.warn("Attempt to update book ID: {} with duplicate active ISBN: {}", id, updateBookRequestDto.isbn());
                throw new DuplicateIsbnException(updateBookRequestDto.isbn());
            }
            existingBook.setIsbn(updateBookRequestDto.isbn());
            updated = true;
        }

        if (updated) {
            try {
                Book updatedBook = bookRepository.save(existingBook);
                log.info("Successfully updated book with ID: {}", updatedBook.getId());
                return bookMapper.toDto(updatedBook);
            } catch (DataIntegrityViolationException e) {
                log.warn("Data integrity violation while updating book ID: {}, likely duplicate ISBN: {}", id, existingBook.getIsbn(), e);
                 throw new DuplicateIsbnException("Book with ISBN '" + existingBook.getIsbn() + "' already exists (possibly soft-deleted). Cause: " + e.getMessage());
            }
        } else {
            log.info("No changes detected for book ID: {}. Returning existing data.", id);
            return bookMapper.toDto(existingBook); // No changes, return current state
        }
    }

    @Override
    @Transactional
    public void softDeleteBook(Long id) {
        log.info("Attempting to soft delete book with ID: {}", id);
        Book book = bookRepository.findById(id)
            .orElseThrow(() -> {
                log.warn("Book not found for soft delete with ID: {}", id);
                return new ResourceNotFoundException("Book", "ID", id);
            });
        
        // The @SQLDelete annotation on the Book entity handles the soft delete logic.
        // Calling deleteById will trigger the SQL UPDATE statement defined in @SQLDelete.
        bookRepository.deleteById(book.getId()); 
        // Or, if we wanted to be more explicit here or if @SQLDelete wasn't used:
        // book.setDeleted(true);
        // bookRepository.save(book);
        log.info("Successfully soft-deleted book with ID: {}", id);
    }
} 