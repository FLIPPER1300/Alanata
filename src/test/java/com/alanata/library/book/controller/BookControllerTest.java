package com.alanata.library.book.controller;

import com.alanata.library.controller.BookController;
import com.alanata.library.dto.response.BookCopyResponse;
import com.alanata.library.dto.response.BookDetailsResponse;
import com.alanata.library.dto.response.BookListItemResponse;
import com.alanata.library.dto.response.BookPageResponse;
import com.alanata.library.exception.ResourceNotFoundException;
import com.alanata.library.service.BookService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    MockMvc mockMvc;

    @MockBean
    BookService bookService;

    @Test
    void listsBooks() throws Exception {
        when(bookService.getBooks(org.mockito.ArgumentMatchers.any())).thenReturn(
                new BookPageResponse(
                        List.of(new BookListItemResponse(1L, "Clean Code", "Robert C. Martin", "978-0132350884", 2008)),
                        0,
                        20,
                        1,
                        1,
                        true,
                        true
                )
        );

        mockMvc.perform(get("/api/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Clean Code"));
    }

    @Test
    void createsBook() throws Exception {
        when(bookService.createBook(org.mockito.ArgumentMatchers.any())).thenReturn(
                new BookDetailsResponse(1L, "Clean Code", "Robert C. Martin", "978-0132350884", 2008, List.of())
        );

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Clean Code",
                                  "author": "Robert C. Martin",
                                  "isbn": "978-0132350884",
                                  "publishedYear": 2008
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void getsBookById() throws Exception {
        when(bookService.getBook(1L)).thenReturn(
                new BookDetailsResponse(1L, "Clean Code", "Robert C. Martin", "978-0132350884", 2008, List.of())
        );

        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Clean Code"));
    }

    @Test
    void updatesBookById() throws Exception {
        when(bookService.updateBook(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.any())).thenReturn(
                new BookDetailsResponse(1L, "Updated", "Robert C. Martin", "978-0132350884", 2008, List.of())
        );

        mockMvc.perform(put("/api/books/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "Updated"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated"));
    }

    @Test
    void deletesBookById() throws Exception {
        mockMvc.perform(delete("/api/books/5"))
                .andExpect(status().isNoContent());

        verify(bookService).deleteBook(5L);
    }

    @Test
    void getsCopies() throws Exception {
        when(bookService.getCopies(1L)).thenReturn(List.of(
                new BookCopyResponse(10L, true),
                new BookCopyResponse(11L, false)
        ));

        mockMvc.perform(get("/api/books/1/copies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[1].available").value(false));
    }

    @Test
    void addsCopy() throws Exception {
        when(bookService.addCopy(1L)).thenReturn(new BookCopyResponse(12L, true));

        mockMvc.perform(post("/api/books/1/copies"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(12));
    }

    @Test
    void updatesCopyAvailability() throws Exception {
        when(bookService.updateCopyAvailability(org.mockito.ArgumentMatchers.eq(1L), org.mockito.ArgumentMatchers.eq(12L), org.mockito.ArgumentMatchers.any()))
                .thenReturn(new BookCopyResponse(12L, false));

        mockMvc.perform(put("/api/books/1/copies/12")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "available": false
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.available").value(false));
    }

    @Test
    void returns404WhenBookDoesNotExist() throws Exception {
        when(bookService.getBook(99L)).thenThrow(new ResourceNotFoundException("Book not found"));

        mockMvc.perform(get("/api/books/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Book not found"));
    }

    @Test
    void returns400ForInvalidCreateRequest() throws Exception {
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "title": "",
                                  "author": "Author",
                                  "isbn": "bad",
                                  "publishedYear": 0
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Validation failed"));
    }
}
