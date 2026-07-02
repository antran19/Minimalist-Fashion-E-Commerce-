package com.uminimalist.store.model;

public record CartItemView(
        String sku,
        String productSlug,
        String productName,
        String category,
        String color,
        String size,
        String cropClass,
        int quantity,
        int stockQuantity,
        double unitPrice,
        String unitPriceLabel,
        String lineTotalLabel
) {
}
