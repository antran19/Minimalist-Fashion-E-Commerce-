package com.uminimalist.store.model;

import java.util.List;

public record CartView(
        List<CartItemView> items,
        int itemCount,
        String subtotalLabel,
        List<String> warnings
) {
    public CartView(List<CartItemView> items, int itemCount, String subtotalLabel) {
        this(items, itemCount, subtotalLabel, List.of());
    }

    public boolean isEmpty() {
        return items.isEmpty();
    }

    // Convenience constructor for cases without warnings
    public CartView(List<CartItemView> items, int itemCount, String subtotalLabel) {
        this(items, itemCount, subtotalLabel, List.of());
    }
}
