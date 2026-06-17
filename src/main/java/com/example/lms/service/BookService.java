package com.example.lms.service;

import com.example.lms.dto.BookDto;
import com.example.lms.dto.CreateBookRequestDto;
import com.example.lms.dto.UpdateBookRequestDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface BookService {

    BookDto addBook(CreateBookRequestDto createBookRequestDto);

    BookDto getBookById(Long id);

    Page<BookDto> getAllBooks(Pageable pageable, String title, String author);

    BookDto updateBook(Long id, UpdateBookRequestDto updateBookRequestDto);

    void softDeleteBook(Long id);
} 