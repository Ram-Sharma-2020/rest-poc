package com.snapbizz.rest.exception;

public class BookIsbnAlreadyExistsException extends RuntimeException {

    public BookIsbnAlreadyExistsException(String isbn) {
        super("book already exists for ISBN: '" + isbn + "'");
    }
}
