package com.uminimalist.store.model;

import java.time.LocalDateTime;

public record ProductView(
        Long id,
        String slug,
        String name,
        String collection,
        String category,
        String description,
        double price,
        String priceLabel,
        java.util.List<String> colors,
        java.util.List<String> sizes,
        String imagePath,
        String cropClass,
        int stock,
        boolean isNew,
        boolean bestSeller,
        LocalDateTime createdAt
) {
    public ProductView(
            String slug,
            String name,
            String collection,
            String category,
            String description,
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
        this(null, slug, name, collection, category, description, price, priceLabel, colors, sizes, imagePath, cropClass, stock, isNew, bestSeller, null);
    }
    public String colorLabel() {
        return String.join(", ", colors);
    }

    public String sizeLabel() {
        return String.join(" ", sizes);
    }

    public String colorHex(String colorName) {
        return getColorHex(colorName);
    }

    public static String getColorHex(String colorName) {
        if (colorName == null || colorName.isBlank()) {
            return "#cccccc";
        }
        return switch (colorName.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "white" -> "#ffffff";
            case "black" -> "#18181b";
            case "navy" -> "#1e293b";
            case "beige" -> "#e5d5c0";
            case "cream" -> "#fef08a";
            case "sage" -> "#9caf88";
            case "natural" -> "#e3d5ca";
            case "khaki" -> "#c3b091";
            case "grey", "gray" -> "#808080";
            case "red" -> "#dc2626";
            case "blue" -> "#2563eb";
            case "green" -> "#16a34a";
            case "olive" -> "#556b2f";
            case "brown" -> "#78350f";
            case "charcoal" -> "#36454f";
            case "slate" -> "#64748b";
            case "pink" -> "#ec4899";
            case "yellow" -> "#eab308";
            case "orange" -> "#f97316";
            case "purple" -> "#9333ea";
            case "tan" -> "#d2b48c";
            case "burgundy" -> "#800020";
            case "maroon" -> "#800000";
            case "teal" -> "#008080";
            case "indigo" -> "#4b0082";
            default -> "#cccccc";
        };
    }
}
