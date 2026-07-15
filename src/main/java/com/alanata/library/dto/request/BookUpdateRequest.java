package com.alanata.library.dto.request;

import jakarta.validation.constraints.Pattern;

public record BookUpdateRequest(
        String title,
        String author,
        @Pattern(
                regexp = "^(97(8|9))?[- ]?\\d{1,5}[- ]?\\d{1,7}[- ]?\\d{1,7}[- ]?[\\dX]$",
                message = "must be a valid ISBN-10 or ISBN-13 format"
        )
        String isbn,
        Integer publishedYear
) {
}
