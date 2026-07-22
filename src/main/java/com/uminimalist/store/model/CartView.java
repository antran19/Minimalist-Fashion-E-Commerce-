package com.uminimalist.store.model;

import java.util.List;

public record CartView(
        List<CartItemView> items,
        int itemCount,
        String subtotalLabel,
        List<String> warnings
) {
    public boolean isEmpty() {
        return items.isEmpty();
    }

    public boolean hasWarnings() {
        return warnings != null && !warnings.isEmpty();
    }

    // Convenience constructor for cases without warnings
    public CartView(List<CartItemView> items, int itemCount, String subtotalLabel) {
        this(items, itemCount, subtotalLabel, List.of());
    }
}
