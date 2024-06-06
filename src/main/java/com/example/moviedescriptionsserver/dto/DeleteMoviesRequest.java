package com.example.moviedescriptionsserver.dto;

import java.util.List;

public record DeleteMoviesRequest(
        List<String> eidrCodes
) {
}
