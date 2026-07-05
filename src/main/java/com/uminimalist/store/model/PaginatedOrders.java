package com.uminimalist.store.model;

import java.util.List;

public record PaginatedOrders(
        List<OrderSummaryView> orders,
        int currentPage,
        int totalPages,
        long totalItems,
        String selectedStatus
) {
    public boolean hasPrevious() {
        return currentPage > 1;
    }
    
    public boolean hasNext() {
        return currentPage < totalPages;
    }
}
