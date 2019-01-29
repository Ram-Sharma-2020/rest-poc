package com.snapbizz.rest.controller;

import org.springframework.hateoas.VndErrors;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.snapbizz.rest.exception.BookIsbnAlreadyExistsException;
import com.snapbizz.rest.exception.BookNotFoundException;

@ControllerAdvice
@RequestMapping(produces = "application/vnd.error")
public class BookControllerAdvice {

    @ResponseBody
    @ExceptionHandler(BookNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    VndErrors bookNotFoundExceptionHandler(BookNotFoundException ex) {
        return new VndErrors("error", ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(BookIsbnAlreadyExistsException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    VndErrors bookIsbnAlreadyExistsExceptionHandler(BookIsbnAlreadyExistsException ex) {
        return new VndErrors("error", ex.getMessage());
    }
}
