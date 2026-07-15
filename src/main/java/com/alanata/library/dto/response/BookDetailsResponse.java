package com.alanata.library.dto.response;

import java.util.List;

public record BookDetailsResponse(
        Long id,
        String title,
        String author,
        String isbn,
        Integer publishedYear,
        List<BookCopyResponse> copies
) {
}
