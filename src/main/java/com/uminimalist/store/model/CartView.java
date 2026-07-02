package com.uminimalist.store.model;

import java.util.List;

public record CartView(
        List<CartItemView> items,
        int itemCount,
        String subtotalLabel
) {
    public boolean isEmpty() {
        return items.isEmpty();
    }
}
