package com.alanata.library;

import com.alanata.library.dto.request.BookCopyAvailabilityUpdateRequest;
import com.alanata.library.dto.request.BookCreateRequest;
import com.alanata.library.dto.request.BookUpdateRequest;
import com.alanata.library.dto.response.BookCopyResponse;
import com.alanata.library.dto.response.BookDetailsResponse;
import com.alanata.library.dto.response.BookListItemResponse;
import com.alanata.library.dto.response.BookPageResponse;
import com.alanata.library.entity.Book;
import com.alanata.library.entity.BookCopy;
import com.alanata.library.exception.ApiErrorResponse;
import com.alanata.library.exception.DuplicateResourceException;
import com.alanata.library.exception.InvalidRequestException;
import com.alanata.library.exception.ResourceNotFoundException;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ModelAndExceptionSmokeTest {

    @Test
    void createsRecordsEntitiesAndExceptions() {
        BookCreateRequest createRequest = new BookCreateRequest("Title", "Author", "ISBN", 2001);
        BookUpdateRequest updateRequest = new BookUpdateRequest("Title2", "Author2", "ISBN2", 2002);
        BookCopyAvailabilityUpdateRequest availabilityUpdateRequest = new BookCopyAvailabilityUpdateRequest(true);

        Book book = new Book();
        book.setId(1L);
        book.setTitle("Title");
        book.setAuthor("Author");
        book.setIsbn("ISBN");
        book.setPublishedYear(2001);

        BookCopy copy = new BookCopy();
        copy.setId(2L);
        copy.setBook(book);
        copy.setAvailable(true);
        book.setCopies(List.of(copy));

        BookCopyResponse copyResponse = new BookCopyResponse(2L, true);
        BookListItemResponse listItemResponse = new BookListItemResponse(1L, "Title", "Author", "ISBN", 2001);
        BookDetailsResponse detailsResponse = new BookDetailsResponse(1L, "Title", "Author", "ISBN", 2001, List.of(copyResponse));
        BookPageResponse pageResponse = new BookPageResponse(List.of(listItemResponse), 0, 20, 1, 1, true, true);

        ApiErrorResponse.FieldErrorResponse fieldError = new ApiErrorResponse.FieldErrorResponse("title", "must not be blank");
        ApiErrorResponse apiErrorResponse = new ApiErrorResponse(Instant.now(), 400, "Bad Request", "Validation failed", "/api/books", List.of(fieldError));

        assertThat(createRequest.title()).isEqualTo("Title");
        assertThat(updateRequest.isbn()).isEqualTo("ISBN2");
        assertThat(availabilityUpdateRequest.available()).isTrue();
        assertThat(book.getCopies()).hasSize(1);
        assertThat(copyResponse.id()).isEqualTo(2L);
        assertThat(listItemResponse.title()).isEqualTo("Title");
        assertThat(detailsResponse.copies()).hasSize(1);
        assertThat(pageResponse.content()).hasSize(1);
        assertThat(apiErrorResponse.fieldErrors()).hasSize(1);

        assertThat(new ResourceNotFoundException("missing")).hasMessage("missing");
        assertThat(new InvalidRequestException("invalid")).hasMessage("invalid");
        assertThat(new DuplicateResourceException("duplicate")).hasMessage("duplicate");
    }
}
