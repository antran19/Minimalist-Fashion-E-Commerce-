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
        String notes,
        LocalDateTime createdAt,
        String status,
        String paymentMethod,
        String paymentStatus,
        int itemCount,
        String totalLabel,
        List<OrderItemView> items
) {
    public OrderSummaryView(
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
        this(id, orderCode, customerName, customerEmail, shippingName, shippingPhone, shippingAddressLine, shippingDistrict, shippingCity, null, createdAt, status, paymentMethod, paymentStatus, itemCount, totalLabel, items);
    }
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
        return ("PLACED".equalsIgnoreCase(status) || "PENDING_PAYMENT".equalsIgnoreCase(status))
                && !"PAID".equalsIgnoreCase(paymentStatus);
    }

    public boolean canPayAgain() {
        return "PENDING_PAYMENT".equalsIgnoreCase(status)
                && !"PAID".equalsIgnoreCase(paymentStatus)
                && "PAYPAL".equalsIgnoreCase(paymentMethod);
    }

    public String paymentMethodLabel() {
        if ("PAYPAL".equalsIgnoreCase(paymentMethod)) {
            return "PayPal";
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

    public String statusLabel() {
        if (status == null) return "Order Placed";
        switch (status.toUpperCase(java.util.Locale.ROOT)) {
            case "PLACED":
                return "Order Placed";
            case "PENDING_PAYMENT":
                return "Awaiting Payment";
            case "PROCESSING":
                return "Processing";
            case "SHIPPED":
                return "Shipping";
            case "DELIVERED":
                return "Delivered";
            case "CANCELLED":
                return "Cancelled";
            default:
                return status;
        }
    }

    public boolean isStepCompleted(String step) {
        if ("CANCELLED".equalsIgnoreCase(status)) return false;
        String normalizedStatus = status == null ? "" : status.toUpperCase(java.util.Locale.ROOT);
        if ("PENDING_PAYMENT".equals(normalizedStatus)) {
            normalizedStatus = "PLACED";
        }
        java.util.List<String> steps = java.util.List.of("PLACED", "PROCESSING", "SHIPPED", "DELIVERED");
        int currentIndex = steps.indexOf(normalizedStatus);
        int stepIndex = steps.indexOf(step == null ? "" : step.toUpperCase(java.util.Locale.ROOT));
        return currentIndex >= 0 && stepIndex >= 0 && stepIndex < currentIndex;
    }

    public String timelineStepState(String step) {
        if ("CANCELLED".equalsIgnoreCase(status)) {
            return "CANCELLED".equalsIgnoreCase(step) ? "is-cancelled" : "";
        }
        String normalizedStatus = status == null ? "" : status.toUpperCase(java.util.Locale.ROOT);
        if ("PENDING_PAYMENT".equals(normalizedStatus)) {
            normalizedStatus = "PLACED";
        }
        java.util.List<String> steps = java.util.List.of("PLACED", "PROCESSING", "SHIPPED", "DELIVERED");
        int currentIndex = steps.indexOf(normalizedStatus);
        int stepIndex = steps.indexOf(step == null ? "" : step.toUpperCase(java.util.Locale.ROOT));
        if (stepIndex < currentIndex) return "completed";
        if (stepIndex == currentIndex) return "active";
        return "";
    }

    public String timelineClass(String step) {
        return timelineStepState(step);
    }
}
