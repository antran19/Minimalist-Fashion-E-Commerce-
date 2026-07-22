package com.uminimalist.store.model;

public record OrderItemView(
        String productName,
        String sku,
        String color,
        String size,
        int quantity,
        String unitPriceLabel,
        String lineTotalLabel,
        String imagePath,
        String productSlug
) {
    /** Convenience: stock is not tracked in OrderItemView — always returns 0. */
    public int stockQuantity() { return 0; }
}
