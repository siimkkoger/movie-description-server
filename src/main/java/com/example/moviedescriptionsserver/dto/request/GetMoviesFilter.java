package com.example.moviedescriptionsserver.dto.request;

import com.example.moviedescriptionsserver.MoviesOrderBy;
import com.querydsl.core.types.Order;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record GetMoviesFilter(
        List<Long> categoryIds,
        String eidrCode,
        String name,
        @NotNull @Min(1) Integer page,   // Page number (1-based)
        @NotNull @Positive Integer pageSize,
        @NotNull MoviesOrderBy orderBy,
        @NotNull Order direction
) {
    public GetMoviesFilter {
        if (page == null) {
            page = 1;
        }
        if (pageSize == null) {
            pageSize = 5;
        }
        if (direction == null) {
            direction = Order.ASC;
        }
        if (orderBy == null) {
            orderBy = MoviesOrderBy.RATING;
        }
    }
}
