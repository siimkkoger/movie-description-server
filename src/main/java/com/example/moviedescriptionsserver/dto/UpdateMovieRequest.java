package com.example.moviedescriptionsserver.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record UpdateMovieRequest(
        @NotNull String eidrCode,
        @NotNull String name,
        @NotNull Double rating,
        @NotNull Integer year,
        @NotNull Boolean status,
        @NotNull @NotEmpty List<String> categories
) {
}
