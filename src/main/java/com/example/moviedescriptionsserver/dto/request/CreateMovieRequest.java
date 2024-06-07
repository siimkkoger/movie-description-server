package com.example.moviedescriptionsserver.dto.request;

import com.example.moviedescriptionsserver.MovieStatus;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record CreateMovieRequest(
        @NotNull String eidrCode,
        @NotNull String name,
        @NotNull Double rating,
        @NotNull Integer year,
        @NotNull MovieStatus status,
        @NotNull @NotEmpty List<Long> categories
) {
}
