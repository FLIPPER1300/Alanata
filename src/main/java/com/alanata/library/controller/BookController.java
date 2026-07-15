package com.alanata.library.controller;

import com.alanata.library.dto.request.BookCopyAvailabilityUpdateRequest;
import com.alanata.library.dto.request.BookCreateRequest;
import com.alanata.library.dto.request.BookUpdateRequest;
import com.alanata.library.dto.response.BookCopyResponse;
import com.alanata.library.dto.response.BookDetailsResponse;
import com.alanata.library.dto.response.BookPageResponse;
import com.alanata.library.service.BookService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/books")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping
    public BookPageResponse getBooks(
            @ParameterObject
            @PageableDefault(sort = "title", direction = Direction.ASC) Pageable pageable
    ) {
        return bookService.getBooks(pageable);
    }

    @PostMapping
    public ResponseEntity<BookDetailsResponse> createBook(@Valid @RequestBody BookCreateRequest request) {
        BookDetailsResponse response = bookService.createBook(request);
        return ResponseEntity.created(URI.create("/api/books/" + response.id())).body(response);
    }

    @GetMapping("/{id}")
    public BookDetailsResponse getBook(@PathVariable Long id) {
        return bookService.getBook(id);
    }

    @PutMapping("/{id}")
    public BookDetailsResponse updateBook(@PathVariable Long id, @Valid @RequestBody BookUpdateRequest request) {
        return bookService.updateBook(id, request);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/copies")
    public List<BookCopyResponse> getCopies(@PathVariable Long id) {
        return bookService.getCopies(id);
    }

    @PostMapping("/{id}/copies")
    public ResponseEntity<BookCopyResponse> addCopy(@PathVariable Long id) {
        BookCopyResponse response = bookService.addCopy(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{id}/copies/{copyId}")
    public BookCopyResponse updateCopyAvailability(
            @PathVariable Long id,
            @PathVariable Long copyId,
            @Valid @RequestBody BookCopyAvailabilityUpdateRequest request
    ) {
        return bookService.updateCopyAvailability(id, copyId, request);
    }
}
