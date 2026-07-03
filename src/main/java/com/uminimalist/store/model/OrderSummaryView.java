package com.uminimalist.store.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record OrderSummaryView(
        Long id,
        String orderCode,
        String customerName,
        String customerEmail,
        LocalDateTime createdAt,
        String status,
        int itemCount,
        String totalLabel,
        List<OrderItemView> items
) {
    private static final DateTimeFormatter DISPLAY_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm");

    public String placedAtLabel() {
        return createdAt == null ? "Just now" : createdAt.format(DISPLAY_FORMAT);
    }
}
