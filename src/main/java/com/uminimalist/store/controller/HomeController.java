package com.uminimalist.store.controller;

import com.uminimalist.store.service.LandingPageService;
import com.uminimalist.store.service.WishlistService;
import com.uminimalist.store.service.ProductReviewService;
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
    private final ProductReviewService productReviewService;

    public HomeController(LandingPageService landingPageService,
                          WishlistService wishlistService,
                          ProductReviewService productReviewService) {
        this.landingPageService = landingPageService;
        this.wishlistService = wishlistService;
        this.productReviewService = productReviewService;
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
                           @RequestParam(required = false, defaultValue = "false") Boolean inStock,
                           Model model) {
        Double validatedMinPrice = minPrice;
        Double validatedMaxPrice = maxPrice;
        String filterError = null;

        if (validatedMinPrice != null && validatedMinPrice < 0) {
            filterError = "Minimum price cannot be negative. Reset to $0.";
            validatedMinPrice = 0.0;
        }
        if (validatedMaxPrice != null && validatedMaxPrice < 0) {
            filterError = "Maximum price cannot be negative. Reset to $0.";
            validatedMaxPrice = 0.0;
        }

        if (validatedMinPrice != null && validatedMaxPrice != null && validatedMinPrice > validatedMaxPrice) {
            filterError = "Min price ($" + minPrice + ") cannot be greater than Max price ($" + maxPrice + "). Filter adjusted automatically.";
            Double temp = validatedMinPrice;
            validatedMinPrice = validatedMaxPrice;
            validatedMaxPrice = temp;
        }

        model.addAttribute("products", landingPageService.getProducts(q, collection, size, color, validatedMinPrice, validatedMaxPrice, sort, inStock));
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
        model.addAttribute("inStockOnly", inStock);
        if (filterError != null) {
            model.addAttribute("filterError", filterError);
        }
        return "products";
    }

    @GetMapping("/products/{slug}")
    public String productDetail(@PathVariable String slug, Authentication authentication, Model model) {
        var product = landingPageService.getProduct(slug)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "Product not found"));
        model.addAttribute("product", product);
        model.addAttribute("variantStocks", landingPageService.getVariantStockMap(slug));
        boolean authenticated = authentication != null
                && authentication.isAuthenticated()
                && authentication.getAuthorities().stream().noneMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
        model.addAttribute("inWishlist", authenticated && wishlistService.contains(authentication.getName(), slug));
        
        String email = authenticated ? authentication.getName() : null;
        model.addAttribute("reviews", productReviewService.findReviewsForProduct(slug));
        model.addAttribute("ratingStats", productReviewService.getStatsForProduct(slug));
        model.addAttribute("canReview", authenticated && productReviewService.canUserReview(email, slug));
        model.addAttribute("reviewStatus", productReviewService.getReviewEligibilityStatus(email, slug));
        model.addAttribute("relatedProducts", landingPageService.getRelatedProducts(product.slug(), product.collection(), 4));
        
        return "product-detail";
    }
}
