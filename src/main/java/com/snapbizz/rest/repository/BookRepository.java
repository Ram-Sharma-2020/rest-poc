package com.snapbizz.rest.repository;

import java.util.Optional;

import org.springframework.data.repository.PagingAndSortingRepository;

import com.snapbizz.rest.domain.Book;

public interface BookRepository extends PagingAndSortingRepository<Book, Long> {
    Optional<Book> findByIsbn(String isbn);
}
