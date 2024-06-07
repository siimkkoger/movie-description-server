package com.example.moviedescriptionsserver.dto;

import com.example.moviedescriptionsserver.MovieStatus;
import jakarta.validation.constraints.NotNull;

public record MovieTableRowDto(
        @NotNull String eidrCode,
        @NotNull String name,
        @NotNull Double rating,
        @NotNull Integer year,
        @NotNull MovieStatus status,
        @NotNull String categories
) {
}
