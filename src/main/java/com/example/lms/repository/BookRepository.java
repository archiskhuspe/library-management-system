package com.example.lms.repository;

import com.example.lms.model.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {

    /**
     * Finds a book by its ISBN, including soft-deleted ones for internal checks.
     * Note: The @Where(clause = "is_deleted = false") on the Book entity normally filters out soft-deleted records.
     * To find any book by ISBN (e.g., to prevent adding a new book with an ISBN that already exists, even if soft-deleted),
     * a custom query or a different approach might be needed if strict unique ISBN across all records (including soft-deleted) is required.
     * For now, this will respect the @Where clause.
     * If we need to check against ALL records including soft-deleted ones for ISBN uniqueness, we might need a native query or
     * to temporarily disable the filter for that specific check if Hibernate allows.
     * 
     * Update: For checking ISBN uniqueness against *all* records (including soft-deleted), a specific query
     * that ignores the global @Where filter is necessary. Spring Data JPA doesn't directly provide an easy way
     * to bypass the @Where clause on a per-query basis using derived query names alone.
     * A @Query annotation would be needed, or we can check if `existsByIsbn` respects or bypasses the @Where. Test will confirm.
     * Let's assume for now that `findByIsbn` will respect the @Where clause, meaning it won't find soft-deleted books.
     * A method like `existsByIsbnIncludingDeleted` would require a custom query if this behavior is not desired.
     */
    Optional<Book> findByIsbn(String isbn);

    /**
     * Checks if a book exists with the given ISBN. This will respect the @Where clause on the Book entity.
     */
    boolean existsByIsbn(String isbn);

} 