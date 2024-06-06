package com.example.moviedescriptionsserver.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record GetMovieTableResult(
        @NotNull List<MovieDto> movies,
        @NotNull Integer page,
        @NotNull Integer pageSize,
        @NotNull Long totalItems,
        @NotNull Integer totalPages
) {
}
