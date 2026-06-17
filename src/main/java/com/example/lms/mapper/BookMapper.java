package com.example.lms.mapper;

import com.example.lms.dto.BookDto;
import com.example.lms.dto.CreateBookRequestDto;
import com.example.lms.model.Book;
import org.springframework.stereotype.Component;

@Component
public class BookMapper {

    public BookDto toDto(Book book) {
        if (book == null) {
            return null;
        }
        return new BookDto(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getIsbn(),
            book.getPublishedDate()
        );
    }

    public Book toEntity(CreateBookRequestDto createBookRequestDto) {
        if (createBookRequestDto == null) {
            return null;
        }
        return Book.builder()
            .title(createBookRequestDto.title())
            .author(createBookRequestDto.author())
            .isbn(createBookRequestDto.isbn())
            .publishedDate(createBookRequestDto.publishedDate())
            // isDeleted defaults to false in the entity
            .build();
    }

    // updateEntityFromDto method will be handled in the service layer for partial updates
    // public void updateEntityFromDto(UpdateBookRequestDto dto, Book entity) { ... }
} 