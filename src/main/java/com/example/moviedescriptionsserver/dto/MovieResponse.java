package com.example.moviedescriptionsserver.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record MovieResponse(
        @NotNull String eidrCode,
        @NotNull String name,
        @NotNull List<String> categories,
        @NotNull Double rating,
        @NotNull Integer year,
        @NotNull Boolean status
) {
}
