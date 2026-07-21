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
        String paymentMethod,
        String paymentStatus,
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
        return "PLACED".equalsIgnoreCase(status) || "PENDING_PAYMENT".equalsIgnoreCase(status);
    }

    public String paymentMethodLabel() {
        if ("VNPAY".equalsIgnoreCase(paymentMethod)) {
            return "VNPay Demo";
        }
        return "Cash on Delivery (COD)";
    }

    public String paymentStatusLabel() {
        if ("PAID".equalsIgnoreCase(paymentStatus)) {
            return "Paid";
        } else if ("FAILED".equalsIgnoreCase(paymentStatus)) {
            return "Failed";
        } else if ("PENDING".equalsIgnoreCase(paymentStatus)) {
            return "Pending";
        }
        return "Unpaid";
    }

    public String paymentStatusBadgeClass() {
        if ("PAID".equalsIgnoreCase(paymentStatus)) {
            return "payment-badge paid";
        } else if ("FAILED".equalsIgnoreCase(paymentStatus)) {
            return "payment-badge failed";
        } else if ("PENDING".equalsIgnoreCase(paymentStatus)) {
            return "payment-badge pending";
        }
        return "payment-badge unpaid";
    }

    public boolean isStepCompleted(String step) {
        if ("CANCELLED".equalsIgnoreCase(status)) return false;
        java.util.List<String> steps = java.util.List.of("PLACED", "PROCESSING", "SHIPPED", "DELIVERED");
        int currentIndex = steps.indexOf(status == null ? "" : status.toUpperCase(java.util.Locale.ROOT));
        int stepIndex = steps.indexOf(step == null ? "" : step.toUpperCase(java.util.Locale.ROOT));
        return currentIndex >= 0 && stepIndex >= 0 && stepIndex < currentIndex;
    }

    public String timelineStepState(String step) {
        if ("CANCELLED".equalsIgnoreCase(status)) {
            return "CANCELLED".equalsIgnoreCase(step) ? "is-cancelled" : "";
        }
        java.util.List<String> steps = java.util.List.of("PLACED", "PROCESSING", "SHIPPED", "DELIVERED");
        int currentIndex = steps.indexOf(status == null ? "" : status.toUpperCase(java.util.Locale.ROOT));
        int stepIndex = steps.indexOf(step == null ? "" : step.toUpperCase(java.util.Locale.ROOT));
        if (stepIndex < currentIndex) return "completed";
        if (stepIndex == currentIndex) return "active";
        return "";
    }

    public String timelineClass(String step) {
        return timelineStepState(step);
    }
}
