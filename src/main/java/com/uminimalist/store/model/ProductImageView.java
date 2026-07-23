package com.uminimalist.store.model;

public record ProductImageView(
        Long id,
        String imageUrl,
        String publicId,
        String color,
        boolean isPrimary,
        int displayOrder
) {
}
