package com.alanata.library.dto.response;

public record BookListItemResponse(
        Long id,
        String title,
        String author,
        String isbn,
        Integer publishedYear
) {
}
