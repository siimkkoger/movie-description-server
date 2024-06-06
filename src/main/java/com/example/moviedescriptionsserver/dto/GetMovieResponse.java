package com.example.moviedescriptionsserver.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record GetMovieResponse(
        @NotNull MovieDto movie,
        @NotNull List<CategoryResponse> categories
) {
}
