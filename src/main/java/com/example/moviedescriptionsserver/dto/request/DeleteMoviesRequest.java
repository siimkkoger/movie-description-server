package com.example.moviedescriptionsserver.dto.request;

import java.util.List;

public record DeleteMoviesRequest(
        List<String> eidrCodes
) {
}
