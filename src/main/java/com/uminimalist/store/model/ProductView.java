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
        boolean onSale,
        int discountPercentage,
        double salePrice,
        String salePriceLabel,
        java.util.List<ProductImageView> images,
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
            boolean bestSeller,
            boolean onSale,
            int discountPercentage,
            double salePrice,
            String salePriceLabel
    ) {
        this(null, slug, name, collection, category, description, price, priceLabel, colors, sizes, imagePath, cropClass, stock, isNew, bestSeller, onSale, discountPercentage, salePrice, salePriceLabel, java.util.List.of(), null);
    }

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
            boolean bestSeller,
            boolean onSale,
            int discountPercentage,
            double salePrice,
            String salePriceLabel,
            java.util.List<ProductImageView> images
    ) {
        this(null, slug, name, collection, category, description, price, priceLabel, colors, sizes, imagePath, cropClass, stock, isNew, bestSeller, onSale, discountPercentage, salePrice, salePriceLabel, images, null);
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

    public String imageForColor(String colorName) {
        if (colorName == null || colorName.isBlank()) {
            return imagePath;
        }
        if (images != null && !images.isEmpty()) {
            for (ProductImageView img : images) {
                if (img.color() != null && img.color().equalsIgnoreCase(colorName.trim())) {
                    return img.imageUrl();
                }
            }
        }
        return resolveStaticColorImage(slug, colorName);
    }

    public static String resolveStaticColorImage(String productSlug, String colorName) {
        if (productSlug == null || colorName == null) return "/images/product-collage.png";
        String normalizedColor = colorName.trim().toLowerCase(java.util.Locale.ROOT).replace(" ", "-");
        return switch (productSlug) {
            case "air-cotton-tee" -> switch (normalizedColor) {
                case "light-blue" -> "/images/products/air-cotton-tee-light-blue.png";
                case "pink" -> "/images/products/air-cotton-tee-pink.png";
                case "red" -> "/images/products/soft-jersey-tee-red.jpg";
                case "white" -> "/images/products/soft-jersey-tee-white.jpg";
                case "brown" -> "/images/products/soft-jersey-tee-brown.png";
                default -> "/images/products/air-cotton-tee-cream.png";
            };
            case "light-utility-jacket" -> switch (normalizedColor) {
                case "navy", "blue" -> "/images/products/light-utility-jacket-navy.png";
                default -> "/images/products/light-utility-jacket-gray.png";
            };
            case "oxford-shirt" -> switch (normalizedColor) {
                case "light-blue" -> "/images/products/oxford-shirt-light-blue.jpg";
                case "white" -> "/images/products/oxford-shirt-white.png";
                default -> "/images/products/oxford-shirt-brown.png";
            };
            case "soft-jersey-tee" -> switch (normalizedColor) {
                case "red" -> "/images/products/soft-jersey-tee-red.jpg";
                case "white" -> "/images/products/soft-jersey-tee-white.jpg";
                case "pink" -> "/images/products/air-cotton-tee-pink.png";
                case "light-blue" -> "/images/products/air-cotton-tee-light-blue.png";
                case "sage", "grey", "gray" -> "/images/products/air-cotton-tee-cream.png";
                default -> "/images/products/soft-jersey-tee-brown.png";
            };
            case "smart-ankle-pants" -> switch (normalizedColor) {
                case "brown" -> "/images/products/smart-ankle-pants-brown.jpg";
                case "white" -> "/images/products/smart-ankle-pants-white.jpg";
                default -> "/images/products/smart-ankle-pants-black.png";
            };
            case "everyday-zip-hoodie" -> switch (normalizedColor) {
                case "cream" -> "/images/products/everyday-zip-hoodie-cream.jpg";
                case "red" -> "/images/products/everyday-zip-hoodie-red.jpg";
                case "yellow" -> "/images/products/utility-tote-yellow.jpg";
                default -> "/images/products/everyday-zip-hoodie-black.png";
            };
            case "linen-blend-shirt" -> switch (normalizedColor) {
                case "orange" -> "/images/products/linen-blend-shirt-orange.jpg";
                case "white" -> "/images/products/linen-blend-shirt-white.jpg";
                default -> "/images/products/linen-blend-shirt-cream.png";
            };
            case "utility-tote" -> switch (normalizedColor) {
                case "red" -> "/images/products/utility-tote-red.png";
                case "yellow" -> "/images/products/utility-tote-yellow.jpg";
                default -> "/images/products/utility-tote-pink.jpg";
            };
            case "school-day-cardigan" -> switch (normalizedColor) {
                case "dark-green" -> "/images/products/school-day-cardigan-dark-green.jpg";
                case "navy", "blue" -> "/images/products/school-day-cardigan-navy.png";
                default -> "/images/products/school-day-cardigan-black.jpg";
            };
            case "easy-cotton-shorts" -> switch (normalizedColor) {
                case "gray", "grey" -> "/images/products/easy-cotton-shorts-gray.png";
                default -> "/images/products/easy-cotton-shorts-blue.png";
            };
            default -> "/images/product-collage.png";
        };
    }
}
