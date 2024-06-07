package com.example.moviedescriptionsserver.dto.response;

import jakarta.validation.constraints.NotNull;

public record CategoryResponse(
        @NotNull Long id,
        @NotNull String name
) {
}
