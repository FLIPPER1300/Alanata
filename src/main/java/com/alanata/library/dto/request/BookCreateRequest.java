package com.alanata.library.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record BookCreateRequest(
        @NotBlank String title,
        @NotBlank String author,
        @NotBlank
        @Pattern(
                regexp = "^(97(8|9))?[- ]?\\d{1,5}[- ]?\\d{1,7}[- ]?\\d{1,7}[- ]?[\\dX]$",
                message = "must be a valid ISBN-10 or ISBN-13 format"
        )
        String isbn,
        @NotNull @Min(1) Integer publishedYear
) {
}
