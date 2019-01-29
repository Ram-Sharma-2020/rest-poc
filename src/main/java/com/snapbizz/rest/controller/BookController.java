package com.snapbizz.rest.controller;

import static org.springframework.web.util.UriComponentsBuilder.fromUriString;

import java.util.List;

import javax.validation.Valid;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import com.snapbizz.rest.domain.Book;
import com.snapbizz.rest.exception.BookIsbnAlreadyExistsException;
import com.snapbizz.rest.exception.BookNotFoundException;
import com.snapbizz.rest.repository.BookRepository;

@RestController
@RequestMapping(value = "/api/books")
public class BookController {

    private static final int MAX_PAGE_SIZE = 50;

    private final BookRepository bookRepository;

    public BookController(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @PostMapping
    public ResponseEntity<?> createBook(@Valid @RequestBody Book book, UriComponentsBuilder ucBuilder) {
        if (bookRepository.findByIsbn(book.getIsbn()).isPresent()) {
            throw new BookIsbnAlreadyExistsException(book.getIsbn());
        }
        bookRepository.save(book);

        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/api/books/{isbn}").buildAndExpand(book.getIsbn()).toUri());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @GetMapping("/{isbn}")
    public ResponseEntity<Book> getBook(@PathVariable("isbn") String isbn) {
        return bookRepository.findByIsbn(isbn)
                .map(book -> new ResponseEntity<>(book, HttpStatus.OK))
                .orElseThrow(() -> new BookNotFoundException(isbn));
    }

    @GetMapping
    public ResponseEntity<List<Book>> getAllBooks(
            @PageableDefault(size = MAX_PAGE_SIZE) Pageable pageable,
            @RequestParam(required = false, defaultValue = "id") String sort,
            @RequestParam(required = false, defaultValue = "asc") String order) {
        final PageRequest pr = PageRequest.of(
                pageable.getPageNumber(), pageable.getPageSize(),
                Sort.by("asc" .equals(order) ? Sort.Direction.ASC : Sort.Direction.DESC, sort)
        );

        Page<Book> booksPage = bookRepository.findAll(pr);

        if (booksPage.getContent().isEmpty()) {
            return new ResponseEntity(HttpStatus.NO_CONTENT);
        } else {
            long totalBooks = booksPage.getTotalElements();
            int nbPageBooks = booksPage.getNumberOfElements();

            HttpHeaders headers = new HttpHeaders();
            headers.add("X-Total-Count", String.valueOf(totalBooks));

            if (nbPageBooks < totalBooks) {
                headers.add("first", buildPageUri(PageRequest.of(0, booksPage.getSize())));
                headers.add("last", buildPageUri(PageRequest.of(booksPage.getTotalPages() - 1, booksPage.getSize())));

                if (booksPage.hasNext()) {
                    headers.add("next", buildPageUri(booksPage.nextPageable()));
                }

                if (booksPage.hasPrevious()) {
                    headers.add("prev", buildPageUri(booksPage.previousPageable()));
                }

                return new ResponseEntity<>(booksPage.getContent(), headers, HttpStatus.PARTIAL_CONTENT);
            } else {
                return new ResponseEntity(booksPage.getContent(), headers, HttpStatus.OK);
            }
        }
    }

    @PutMapping("/{isbn}")
    public ResponseEntity<Book> updateBook(@PathVariable("isbn") String isbn, @Valid @RequestBody Book book) {
        return bookRepository.findByIsbn(isbn)
                .map(bookToUpdate -> {
                    bookToUpdate.setIsbn(book.getIsbn());
                    bookToUpdate.setTitle(book.getTitle());
                    bookToUpdate.setDescription(book.getDescription());
                    bookToUpdate.setAuthors(book.getAuthors());
                    bookToUpdate.setPublisher(book.getPublisher());
                    bookRepository.save(bookToUpdate);

                    return new ResponseEntity<>(bookToUpdate, HttpStatus.OK);
                })
                .orElseThrow(() -> new BookNotFoundException(isbn));
    }

    @PatchMapping("/{isbn}")
    public ResponseEntity<Book> updateBookDescription(@PathVariable("isbn") String isbn, @RequestBody String description) {
        return bookRepository.findByIsbn(isbn)
                .map(book -> {
                    book.setDescription(description);
                    bookRepository.save(book);

                    return new ResponseEntity<>(book, HttpStatus.OK);
                })
                .orElseThrow(() -> new BookNotFoundException(isbn));
    }

    @DeleteMapping("/{isbn}")
    public ResponseEntity<?> deleteBook(@PathVariable("isbn") String isbn) {
        return bookRepository.findByIsbn(isbn)
                .map(book -> {
                    bookRepository.delete(book);
                    return new ResponseEntity(HttpStatus.NO_CONTENT);
                })
                .orElseThrow(() -> new BookNotFoundException(isbn));
    }

    private String buildPageUri(Pageable page) {
        return fromUriString("/api/books")
                .query("page={page}&size={size}")
                .buildAndExpand(page.getPageNumber(), page.getPageSize())
                .toUriString();
    }

}
