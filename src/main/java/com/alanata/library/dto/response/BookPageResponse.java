package com.alanata.library.dto.response;

import java.util.List;

public record BookPageResponse(
        List<BookListItemResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
}
