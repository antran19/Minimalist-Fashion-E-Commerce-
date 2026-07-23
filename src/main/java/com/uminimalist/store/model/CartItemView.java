package com.uminimalist.store.model;

public record CartItemView(
        String sku,
        String productSlug,
        String productName,
        String category,
        String color,
        String size,
        String imagePath,
        String cropClass,
        int quantity,
        int stockQuantity,
        double unitPrice,
        String unitPriceLabel,
        String lineTotalLabel,
        boolean outOfStock,
        boolean stockExceeded
) {
    public boolean isSelectable() {
        return !outOfStock && !stockExceeded;
    }
}
