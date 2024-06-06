package com.example.moviedescriptionsserver.dto;

import java.util.List;

public record MovieDTO(
        String eidrCode,
        String name,
        Double rating,
        Integer year,
        Boolean status
) {
}
