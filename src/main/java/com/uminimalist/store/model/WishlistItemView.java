package com.uminimalist.store.model;

public record WishlistItemView(
        String slug,
        String name,
        String category,
        String priceLabel,
        String imagePath,
        boolean onSale,
        int discountPercentage,
        String salePriceLabel
) {
}
