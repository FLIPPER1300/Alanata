package com.alanata.library.mapper;

import com.alanata.library.dto.request.BookCreateRequest;
import com.alanata.library.dto.request.BookUpdateRequest;
import com.alanata.library.entity.Book;
import com.alanata.library.entity.BookCopy;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class BookMapperImplTest {

    @Test
    void mapsCreateAndReadModels() throws Exception {
        BookMapperImpl mapper = mapper();
        BookCreateRequest createRequest = new BookCreateRequest("Title", "Author", "ISBN", 2001);

        Book created = mapper.toEntity(createRequest);
        assertThat(created.getTitle()).isEqualTo("Title");
        assertThat(created.getAuthor()).isEqualTo("Author");
        assertThat(created.getIsbn()).isEqualTo("ISBN");
        assertThat(created.getPublishedYear()).isEqualTo(2001);

        Book source = new Book();
        source.setId(10L);
        source.setTitle("Title");
        source.setAuthor("Author");
        source.setIsbn("ISBN");
        source.setPublishedYear(2001);
        BookCopy copy = new BookCopy();
        copy.setId(99L);
        copy.setBook(source);
        copy.setAvailable(true);
        source.setCopies(List.of(copy));

        var listItem = mapper.toListItemResponse(source);
        assertThat(listItem.id()).isEqualTo(10L);
        assertThat(listItem.title()).isEqualTo("Title");

        var details = mapper.toDetailsResponse(source);
        assertThat(details.id()).isEqualTo(10L);
        assertThat(details.copies()).hasSize(1);
        assertThat(details.copies().getFirst().id()).isEqualTo(99L);
    }

    @Test
    void updatesOnlyNonNullFields() throws Exception {
        BookMapperImpl mapper = mapper();
        Book target = new Book();
        target.setTitle("Old");
        target.setAuthor("Author");
        target.setIsbn("OldISBN");
        target.setPublishedYear(2000);
        target.setId(1L);

        mapper.updateEntity(new BookUpdateRequest("New", null, "NewISBN", null), target);

        assertThat(target.getTitle()).isEqualTo("New");
        assertThat(target.getAuthor()).isEqualTo("Author");
        assertThat(target.getIsbn()).isEqualTo("NewISBN");
        assertThat(target.getPublishedYear()).isEqualTo(2000);
    }

    @Test
    void handlesNulls() throws Exception {
        BookMapperImpl mapper = mapper();

        assertThat(mapper.toEntity(null)).isNull();
        assertThat(mapper.toListItemResponse(null)).isNull();
        assertThat(mapper.toDetailsResponse(null)).isNull();
    }

    @Test
    void mapsBookCopyInBothDirections() {
        BookCopyMapperImpl mapper = new BookCopyMapperImpl();
        Book book = new Book();
        book.setTitle("Title");
        book.setAuthor("Author");
        book.setIsbn("ISBN");
        book.setPublishedYear(2001);
        book.setId(7L);

        BookCopy created = mapper.toEntity(book);
        assertThat(created.getBook()).isSameAs(book);
        assertThat(created.isAvailable()).isTrue();

        created.setId(70L);
        var response = mapper.toResponse(created);
        assertThat(response.id()).isEqualTo(70L);
        assertThat(response.available()).isTrue();
        assertThat(mapper.toResponse(null)).isNull();
        assertThat(mapper.toEntity(null)).isNull();
    }

    private BookMapperImpl mapper() throws Exception {
        BookMapperImpl mapper = new BookMapperImpl();
        Field field = BookMapperImpl.class.getDeclaredField("bookCopyMapper");
        field.setAccessible(true);
        field.set(mapper, new BookCopyMapperImpl());
        return mapper;
    }
}
