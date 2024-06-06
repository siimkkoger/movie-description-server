package com.example.moviedescriptionsserver.dto;

import jakarta.validation.constraints.NotNull;

public record GetMovieResponse(
        @NotNull String eidrCode,
        @NotNull String name,
        @NotNull Double rating,
        @NotNull Integer year,
        @NotNull Boolean status
) {
}
