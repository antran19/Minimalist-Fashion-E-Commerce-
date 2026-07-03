package com.uminimalist.store.model;

public record OrderItemView(
        String productName,
        String sku,
        String color,
        String size,
        int quantity,
        String unitPriceLabel,
        String lineTotalLabel
) {
}
