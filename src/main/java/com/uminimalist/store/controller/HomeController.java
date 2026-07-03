package com.uminimalist.store.controller;

import com.uminimalist.store.service.LandingPageService;
import com.uminimalist.store.service.WishlistService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@Controller
public class HomeController {

    private final LandingPageService landingPageService;
    private final WishlistService wishlistService;

    public HomeController(LandingPageService landingPageService, WishlistService wishlistService) {
        this.landingPageService = landingPageService;
        this.wishlistService = wishlistService;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("categories", landingPageService.getFeaturedCategories());
        model.addAttribute("newArrivals", landingPageService.getNewArrivals());
        model.addAttribute("essentials", landingPageService.getEssentials());
        return "home";
    }

    @GetMapping("/products")
    public String products(@RequestParam(required = false) String q,
                           @RequestParam(required = false) String collection,
                           @RequestParam(required = false) String size,
                           @RequestParam(required = false) String color,
                           @RequestParam(required = false) Double minPrice,
                           @RequestParam(required = false) Double maxPrice,
                           @RequestParam(required = false, defaultValue = "new") String sort,
                           Model model) {
        model.addAttribute("products", landingPageService.getProducts(q, collection, size, color, minPrice, maxPrice, sort));
        model.addAttribute("collections", landingPageService.getCollections());
        model.addAttribute("sizes", landingPageService.getSizes());
        model.addAttribute("colors", landingPageService.getColors());
        model.addAttribute("q", q);
        model.addAttribute("selectedCollection", collection);
        model.addAttribute("selectedSize", size);
        model.addAttribute("selectedColor", color);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("selectedSort", sort);
        return "products";
    }

    @GetMapping("/products/{slug}")
    public String productDetail(@PathVariable String slug, Authentication authentication, Model model) {
        var product = landingPageService.getProduct(slug)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product not found"));
        model.addAttribute("product", product);
        boolean authenticated = authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream().noneMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("inWishlist", authenticated && wishlistService.contains(authentication.getName(), slug));
        return "product-detail";
    }
}
