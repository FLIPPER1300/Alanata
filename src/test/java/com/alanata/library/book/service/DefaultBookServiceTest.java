package com.alanata.library.book.service;

import com.alanata.library.dto.request.BookCreateRequest;
import com.alanata.library.dto.request.BookUpdateRequest;
import com.alanata.library.dto.request.BookCopyAvailabilityUpdateRequest;
import com.alanata.library.dto.response.BookCopyResponse;
import com.alanata.library.dto.response.BookDetailsResponse;
import com.alanata.library.dto.response.BookPageResponse;
import com.alanata.library.entity.Book;
import com.alanata.library.entity.BookCopy;
import com.alanata.library.exception.DuplicateResourceException;
import com.alanata.library.exception.InvalidRequestException;
import com.alanata.library.exception.ResourceNotFoundException;
import com.alanata.library.mapper.BookCopyMapper;
import com.alanata.library.mapper.BookMapper;
import com.alanata.library.repository.BookCopyRepository;
import com.alanata.library.repository.BookRepository;
import com.alanata.library.service.DefaultBookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DefaultBookServiceTest {

    @Mock
    BookRepository bookRepository;

    @Mock
    BookCopyRepository bookCopyRepository;

    @Mock
    BookMapper bookMapper;

    @Mock
    BookCopyMapper bookCopyMapper;

    @InjectMocks
    DefaultBookService service;

    @Test
    void createsBookAndReturnsDetails() {
        when(bookRepository.existsByTitle("Clean Code")).thenReturn(false);
        when(bookRepository.existsByIsbn("978-0132350884")).thenReturn(false);
        Book mapped = book(null, "Clean Code", "Robert C. Martin", "978-0132350884", 2008);
        Book saved = book(1L, "Clean Code", "Robert C. Martin", "978-0132350884", 2008);
        when(bookMapper.toEntity(org.mockito.ArgumentMatchers.any(BookCreateRequest.class))).thenReturn(mapped);
        when(bookRepository.save(org.mockito.ArgumentMatchers.any(Book.class))).thenReturn(saved);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(saved));
        when(bookMapper.toDetailsResponse(saved)).thenReturn(new com.alanata.library.dto.response.BookDetailsResponse(1L, "Clean Code", "Robert C. Martin", "978-0132350884", 2008, java.util.List.of()));

        var response = service.createBook(new BookCreateRequest("Clean Code", "Robert C. Martin", "978-0132350884", 2008));

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.title()).isEqualTo("Clean Code");
        verify(bookRepository).save(org.mockito.ArgumentMatchers.any(Book.class));
    }

    @Test
    void returnsPagedBooks() {
        when(bookRepository.findAll(PageRequest.of(0, 10))).thenReturn(org.springframework.data.domain.Page.empty());

        BookPageResponse page = service.getBooks(PageRequest.of(0, 10));

        assertThat(page.content()).isEmpty();
    }

    @Test
    void updatesBook() {
        Book book = book(1L, "Clean Code", "Robert C. Martin", "978-0132350884", 2008);

        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.existsByTitleAndIdNot("Clean Code (Updated)", 1L)).thenReturn(false);
        when(bookRepository.save(book)).thenReturn(book);
        when(bookMapper.toDetailsResponse(book)).thenReturn(new com.alanata.library.dto.response.BookDetailsResponse(1L, "Clean Code (Updated)", "Robert C. Martin", "978-0132350884", 2008, java.util.List.of()));

        var response = service.updateBook(1L, new BookUpdateRequest("Clean Code (Updated)", null, null, null));

        assertThat(response.title()).isEqualTo("Clean Code (Updated)");
    }

    @Test
    void createBookRejectsDuplicateIsbn() {
        when(bookRepository.existsByTitle("Clean Code")).thenReturn(false);
        when(bookRepository.existsByIsbn("978-0132350884")).thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> service.createBook(new BookCreateRequest("Clean Code", "Robert C. Martin", "978-0132350884", 2008))
        );
    }

    @Test
    void updateBookRejectsDuplicateTitle() {
        Book existing = book(1L, "Old", "Author", "isbn-1", 2008);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bookRepository.existsByTitleAndIdNot("New Title", 1L)).thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> service.updateBook(1L, new BookUpdateRequest(" New Title ", null, null, null))
        );
    }

    @Test
    void updateBookRejectsDuplicateIsbn() {
        Book existing = book(1L, "Old", "Author", "isbn-1", 2008);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bookRepository.existsByTitleAndIdNot("Old", 1L)).thenReturn(false);
        when(bookRepository.existsByIsbnAndIdNot("new-isbn", 1L)).thenReturn(true);

        assertThrows(
                DuplicateResourceException.class,
                () -> service.updateBook(1L, new BookUpdateRequest("Old", null, " new-isbn ", null))
        );
    }

    @Test
    void updateBookRejectsFuturePublishedYear() {
        Book existing = book(1L, "Old", "Author", "isbn-1", 2008);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));

        assertThrows(
                InvalidRequestException.class,
                () -> service.updateBook(1L, new BookUpdateRequest(null, null, null, 99999))
        );
    }

    @Test
    void updateBookPersistsChanges() {
        Book existing = book(1L, "Old", "Author", "isbn-1", 2008);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(bookRepository.existsByTitleAndIdNot("New", 1L)).thenReturn(false);
        when(bookRepository.existsByIsbnAndIdNot("new-isbn", 1L)).thenReturn(false);
        doAnswer(invocation -> {
            BookUpdateRequest req = invocation.getArgument(0);
            Book target = invocation.getArgument(1);
            target.setTitle(req.title());
            target.setIsbn(req.isbn());
            return null;
        }).when(bookMapper).updateEntity(any(BookUpdateRequest.class), any(Book.class));
        when(bookRepository.save(existing)).thenReturn(existing);
        when(bookMapper.toDetailsResponse(existing)).thenReturn(
                new BookDetailsResponse(1L, "New", "Author", "new-isbn", 2008, List.of())
        );

        BookDetailsResponse response = service.updateBook(1L, new BookUpdateRequest(" New ", null, " new-isbn ", null));

        assertThat(response.title()).isEqualTo("New");
        assertThat(response.isbn()).isEqualTo("new-isbn");
    }

    @Test
    void deleteBookRemovesExistingBook() {
        Book existing = book(1L, "Old", "Author", "isbn-1", 2008);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(existing));

        service.deleteBook(1L);

        verify(bookRepository).delete(existing);
    }

    @Test
    void getCopiesThrowsWhenBookDoesNotExist() {
        when(bookRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.getCopies(10L));
    }

    @Test
    void addCopyThrowsWhenBookDoesNotExist() {
        when(bookRepository.findById(10L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.addCopy(10L));
    }

    @Test
    void updateCopyAvailabilityUpdatesFlag() {
        Book book = book(1L, "Book", "Author", "isbn-1", 2008);
        BookCopy existingCopy = copy(5L, book, true);
        BookCopy savedCopy = copy(5L, book, false);
        when(bookCopyRepository.findByIdAndBookId(5L, 1L)).thenReturn(Optional.of(existingCopy));
        when(bookCopyRepository.save(existingCopy)).thenReturn(savedCopy);
        when(bookCopyMapper.toResponse(savedCopy)).thenReturn(new BookCopyResponse(5L, false));

        BookCopyResponse response = service.updateCopyAvailability(1L, 5L, new BookCopyAvailabilityUpdateRequest(false));

        assertThat(response.available()).isFalse();
    }

    private Book book(Long id, String title, String author, String isbn, Integer publishedYear) {
        Book book = new Book();
        book.setId(id);
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        book.setPublishedYear(publishedYear);
        book.setCopies(new java.util.ArrayList<>());
        return book;
    }

    private BookCopy copy(Long id, Book book, boolean available) {
        BookCopy copy = new BookCopy();
        copy.setId(id);
        copy.setBook(book);
        copy.setAvailable(available);
        return copy;
    }
}
