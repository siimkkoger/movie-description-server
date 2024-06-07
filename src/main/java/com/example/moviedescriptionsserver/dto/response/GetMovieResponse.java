package com.example.moviedescriptionsserver.dto.response;

import com.example.moviedescriptionsserver.dto.MovieDto;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record GetMovieResponse(
        @NotNull MovieDto movie,
        @NotNull List<CategoryResponse> categories
) {
}
