package com.uminimalist.store.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public record OrderSummaryView(
        Long id,
        String orderCode,
        String customerName,
        String customerEmail,
        String shippingName,
        String shippingPhone,
        String shippingAddressLine,
        String shippingDistrict,
        String shippingCity,
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

    public String fullShippingAddress() {
        return String.join(", ", java.util.stream.Stream.of(shippingAddressLine, shippingDistrict, shippingCity)
                .filter(value -> value != null && !value.isBlank())
                .toList());
    }

    public boolean canCancel() {
        return "PLACED".equalsIgnoreCase(status);
    }

    public String timelineClass(String step) {
        if ("CANCELLED".equalsIgnoreCase(status)) {
            return "CANCELLED".equalsIgnoreCase(step) ? "active" : "";
        }

        java.util.List<String> steps = java.util.List.of("PLACED", "PROCESSING", "SHIPPED", "DELIVERED");
        int currentIndex = steps.indexOf(status == null ? "" : status.toUpperCase(java.util.Locale.ROOT));
        int stepIndex = steps.indexOf(step == null ? "" : step.toUpperCase(java.util.Locale.ROOT));
        return currentIndex >= 0 && stepIndex >= 0 && stepIndex <= currentIndex ? "active" : "";
    }
}
