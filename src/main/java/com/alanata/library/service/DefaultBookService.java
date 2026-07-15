package com.alanata.library.service;

import com.alanata.library.dto.request.BookCopyAvailabilityUpdateRequest;
import com.alanata.library.dto.request.BookCreateRequest;
import com.alanata.library.dto.request.BookUpdateRequest;
import com.alanata.library.dto.response.BookCopyResponse;
import com.alanata.library.dto.response.BookDetailsResponse;
import com.alanata.library.dto.response.BookListItemResponse;
import com.alanata.library.dto.response.BookPageResponse;
import com.alanata.library.entity.Book;
import com.alanata.library.entity.BookCopy;
import com.alanata.library.mapper.BookCopyMapper;
import com.alanata.library.mapper.BookMapper;
import com.alanata.library.repository.BookCopyRepository;
import com.alanata.library.repository.BookRepository;
import com.alanata.library.exception.DuplicateResourceException;
import com.alanata.library.exception.InvalidRequestException;
import com.alanata.library.exception.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Year;
import java.util.List;

@Service
@Slf4j
public class DefaultBookService implements BookService {

    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final BookMapper bookMapper;
    private final BookCopyMapper bookCopyMapper;

    public DefaultBookService(BookRepository bookRepository, BookCopyRepository bookCopyRepository, BookMapper bookMapper, BookCopyMapper bookCopyMapper) {
        this.bookRepository = bookRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.bookMapper = bookMapper;
        this.bookCopyMapper = bookCopyMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public BookPageResponse getBooks(Pageable pageable) {
        log.debug("Fetching books page={}, size={}, sort={}", pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        Page<BookListItemResponse> page = bookRepository.findAll(pageable).map(bookMapper::toListItemResponse);
        return new BookPageResponse(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    @Override
    @Transactional
    public BookDetailsResponse createBook(BookCreateRequest request) {
        BookCreateRequest normalizedRequest = normalize(request);
        validatePublishedYear(normalizedRequest.publishedYear());
        ensureBookIsUnique(normalizedRequest.title(), normalizedRequest.isbn(), null);

        Book book = bookMapper.toEntity(normalizedRequest);
        book = bookRepository.save(book);
        log.info("Created book id={}, isbn={}", book.getId(), book.getIsbn());
        return bookMapper.toDetailsResponse(bookRepository.findById(book.getId()).orElseThrow());
    }

    @Override
    @Transactional(readOnly = true)
    public BookDetailsResponse getBook(Long id) {
        return bookMapper.toDetailsResponse(findBookOrThrow(id));
    }

    @Override
    @Transactional
    public BookDetailsResponse updateBook(Long id, BookUpdateRequest request) {
        Book book = findBookOrThrow(id);
        BookUpdateRequest normalizedRequest = normalize(request);

        if (normalizedRequest.publishedYear() != null) {
            validatePublishedYear(normalizedRequest.publishedYear());
        }
        validateOptionalText("title", normalizedRequest.title());
        validateOptionalText("author", normalizedRequest.author());
        validateOptionalText("isbn", normalizedRequest.isbn());
        ensureBookIsUnique(normalizedRequest.title(), normalizedRequest.isbn(), id);

        bookMapper.updateEntity(normalizedRequest, book);
        book = bookRepository.save(book);
        log.info("Updated book id={}", book.getId());
        return bookMapper.toDetailsResponse(bookRepository.findById(book.getId()).orElseThrow());
    }

    @Override
    @Transactional
    public void deleteBook(Long id) {
        Book book = findBookOrThrow(id);
        bookRepository.delete(book);
        log.info("Deleted book id={}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookCopyResponse> getCopies(Long bookId) {
        findBookOrThrow(bookId);
        return bookCopyRepository.findByBookId(bookId).stream()
                .map(bookCopyMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional
    public BookCopyResponse addCopy(Long bookId) {
        Book book = findBookOrThrow(bookId);
        BookCopy copy = bookCopyMapper.toEntity(book);
        copy = bookCopyRepository.save(copy);
        log.info("Added copy for bookId={}, copyId={}", bookId, copy.getId());
        return bookCopyMapper.toResponse(copy);
    }

    @Override
    @Transactional
    public BookCopyResponse updateCopyAvailability(Long bookId, Long copyId, BookCopyAvailabilityUpdateRequest request) {
        BookCopy copy = bookCopyRepository.findByIdAndBookId(copyId, bookId)
                .orElseThrow(() -> new ResourceNotFoundException("Book copy not found"));
        copy.setAvailable(request.available());
        BookCopy saved = bookCopyRepository.save(copy);
        log.info("Updated copy availability bookId={}, copyId={}, available={}", bookId, copyId, request.available());
        return bookCopyMapper.toResponse(saved);
    }

    private Book findBookOrThrow(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found"));
    }

    private void ensureBookIsUnique(String title, String isbn, Long currentId) {
        String normalizedTitle = normalizeIfPresent(title);
        String normalizedIsbn = normalizeIfPresent(isbn);

        if (normalizedTitle != null) {
            boolean duplicateTitle = currentId == null
                    ? bookRepository.existsByTitle(normalizedTitle)
                    : bookRepository.existsByTitleAndIdNot(normalizedTitle, currentId);
            if (duplicateTitle) {
                throw new DuplicateResourceException("Book title must be unique");
            }
        }

        if (normalizedIsbn != null) {
            boolean duplicateIsbn = currentId == null
                    ? bookRepository.existsByIsbn(normalizedIsbn)
                    : bookRepository.existsByIsbnAndIdNot(normalizedIsbn, currentId);
            if (duplicateIsbn) {
                throw new DuplicateResourceException("Book ISBN must be unique");
            }
        }
    }

    private void validatePublishedYear(Integer publishedYear) {
        int currentYear = Year.now().getValue();
        if (publishedYear < 1 || publishedYear > currentYear) {
            throw new InvalidRequestException("Published year must be a valid year");
        }
    }

    private void validateOptionalText(String fieldName, String value) {
        if (value != null && !StringUtils.hasText(value)) {
            throw new InvalidRequestException(fieldName + " must not be blank");
        }
    }

    private String normalize(String value) {
        return value.trim();
    }

    private String normalizeIfPresent(String value) {
        return value == null ? null : value.trim();
    }

    private BookCreateRequest normalize(BookCreateRequest request) {
        return new BookCreateRequest(
                normalize(request.title()),
                normalize(request.author()),
                normalize(request.isbn()),
                request.publishedYear()
        );
    }

    private BookUpdateRequest normalize(BookUpdateRequest request) {
        return new BookUpdateRequest(
                normalizeIfPresent(request.title()),
                normalizeIfPresent(request.author()),
                normalizeIfPresent(request.isbn()),
                request.publishedYear()
        );
    }
}
