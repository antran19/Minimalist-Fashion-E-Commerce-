package com.uminimalist.store.service;

import com.uminimalist.store.model.CategoryView;
import com.uminimalist.store.model.ProductView;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LandingPageService {

    public List<CategoryView> getFeaturedCategories() {
        return List.of(
                new CategoryView("Men", "Everyday layers with clean lines.", "/products?collection=men", "crop-men"),
                new CategoryView("Women", "Soft essentials for simple routines.", "/products?collection=women", "crop-women"),
                new CategoryView("Kids", "Easy pieces for school and weekends.", "/products?collection=kids", "crop-kids")
        );
    }

    public List<ProductView> getNewArrivals() {
        return List.of(
                new ProductView("Air Cotton Tee", "T-shirt", "$19.90", "Cream, white", "S M L XL", "product-tee"),
                new ProductView("Light Utility Jacket", "Outerwear", "$59.90", "Navy", "S M L", "product-jacket"),
                new ProductView("Oxford Shirt", "Shirt", "$34.90", "White", "S M L XL", "product-shirt"),
                new ProductView("Soft Jersey Tee", "T-shirt", "$19.90", "Sage", "XS S M L", "product-sage"),
                new ProductView("Smart Ankle Pants", "Pants", "$39.90", "Black", "XS S M L XL", "product-pants"),
                new ProductView("Everyday Zip Hoodie", "Sweatshirt", "$49.90", "Grey", "S M L XL", "product-hoodie"),
                new ProductView("Linen Blend Shirt", "Shirt", "$34.90", "Natural", "S M L XL", "product-linen"),
                new ProductView("Utility Tote", "Accessories", "$14.90", "Red", "One size", "product-tote")
        );
    }

    public List<String> getEssentials() {
        return List.of(
                "Variant-first product detail pages for size and color accuracy",
                "Clear cart totals before checkout",
                "Inventory checks before every order",
                "Responsive pages built for quick browsing"
        );
    }
}
