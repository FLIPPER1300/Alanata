package com.alanata.library.dto.request;

import jakarta.validation.constraints.NotNull;

public record BookCopyAvailabilityUpdateRequest(@NotNull Boolean available) {
}
