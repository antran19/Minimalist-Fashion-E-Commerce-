package com.uminimalist.store.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public record ReviewView(
        Long id,
        String reviewerName,
        String reviewerEmail,
        int rating,
        String comment,
        LocalDateTime createdAt
) {
    public String createdAtLabel() {
        if (createdAt == null) return "";
        return createdAt.format(DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm"));
    }
}
