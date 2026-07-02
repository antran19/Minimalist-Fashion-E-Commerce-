package com.uminimalist.store.model;

public record ProductView(
        String slug,
        String name,
        String collection,
        String category,
        double price,
        String priceLabel,
        java.util.List<String> colors,
        java.util.List<String> sizes,
        String imagePath,
        String cropClass,
        int stock,
        boolean isNew,
        boolean bestSeller
) {
    public String colorLabel() {
        return String.join(", ", colors);
    }

    public String sizeLabel() {
        return String.join(" ", sizes);
    }
}
