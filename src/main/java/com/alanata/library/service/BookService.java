package com.alanata.library.service;

import com.alanata.library.dto.request.BookCopyAvailabilityUpdateRequest;
import com.alanata.library.dto.request.BookCreateRequest;
import com.alanata.library.dto.request.BookUpdateRequest;
import com.alanata.library.dto.response.BookCopyResponse;
import com.alanata.library.dto.response.BookDetailsResponse;
import com.alanata.library.dto.response.BookPageResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface BookService {

    BookPageResponse getBooks(Pageable pageable);

    BookDetailsResponse createBook(BookCreateRequest request);

    BookDetailsResponse getBook(Long id);

    BookDetailsResponse updateBook(Long id, BookUpdateRequest request);

    void deleteBook(Long id);

    List<BookCopyResponse> getCopies(Long bookId);

    BookCopyResponse addCopy(Long bookId);

    BookCopyResponse updateCopyAvailability(Long bookId, Long copyId, BookCopyAvailabilityUpdateRequest request);
}
