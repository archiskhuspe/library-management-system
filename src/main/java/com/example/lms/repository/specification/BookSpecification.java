package com.example.lms.repository.specification;

import com.example.lms.model.Book;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Component
public class BookSpecification {

    public Specification<Book> getBooksByCriteria(String title, String author) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Global @Where(clause = "is_deleted = false") already handles this for active records.
            // If we needed to explicitly add it here (e.g. if @Where was not used or for specific cases):
            // predicates.add(criteriaBuilder.isFalse(root.get("isDeleted")));

            if (StringUtils.hasText(title)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("title")),
                    "%" + title.toLowerCase() + "%"));
            }

            if (StringUtils.hasText(author)) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("author")),
                    "%" + author.toLowerCase() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
} 