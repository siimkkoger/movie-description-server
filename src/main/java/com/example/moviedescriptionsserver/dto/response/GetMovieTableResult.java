package com.example.moviedescriptionsserver.dto.response;

import com.example.moviedescriptionsserver.dto.MovieTableRowDto;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record GetMovieTableResult(
        @NotNull List<MovieTableRowDto> movies,
        @NotNull Integer page,
        @NotNull Integer pageSize,
        @NotNull Long totalItems,
        @NotNull Integer totalPages
) {
}
