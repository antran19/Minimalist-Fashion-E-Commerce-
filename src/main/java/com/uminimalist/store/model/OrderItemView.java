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
    public OrderItemView(
            String productName,
            String sku,
            String color,
            String size,
            int quantity,
            String unitPriceLabel,
            String lineTotalLabel
    ) {
        this(productName, sku, color, size, quantity, unitPriceLabel, lineTotalLabel, null, null);
    }

    /** Convenience: stock is not tracked in OrderItemView — always returns 0. */
    public int stockQuantity() { return 0; }

    public OrderItemView(
            String productName,
            String sku,
            String color,
            String size,
            int quantity,
            String unitPriceLabel,
            String lineTotalLabel
    ) {
        this(productName, sku, color, size, quantity, unitPriceLabel, lineTotalLabel, "/images/product-collage.png", null);
    }
}
