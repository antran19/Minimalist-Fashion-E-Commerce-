package com.uminimalist.store.controller;

import com.uminimalist.store.service.ShoppingCartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CartController {

    private final ShoppingCartService shoppingCartService;
    private final com.uminimalist.store.repository.ProductVariantRepository productVariantRepository;

    public CartController(ShoppingCartService shoppingCartService,
                          com.uminimalist.store.repository.ProductVariantRepository productVariantRepository) {
        this.shoppingCartService = shoppingCartService;
        this.productVariantRepository = productVariantRepository;
    }

    @GetMapping("/cart")
    public String cart(HttpSession session, Authentication authentication, Model model) {
        model.addAttribute("cart", shoppingCartService.getCart(session, customerEmail(authentication)));
        return "cart";
    }

    @PostMapping("/cart")
    public String addToCart(@RequestParam String productSlug,
                            @RequestParam String color,
                            @RequestParam String size,
                            @RequestParam(required = false, defaultValue = "1") Integer quantity,
                            @RequestParam(required = false, defaultValue = "add") String action,
                            HttpSession session,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        if (isAdmin(authentication)) {
            redirectAttributes.addFlashAttribute("cartError", "Administrators cannot add items to cart or place orders.");
            return "redirect:/products/" + productSlug;
        }
        try {
            int finalQuantity = (quantity == null) ? 1 : quantity;
            shoppingCartService.addItem(session, customerEmail(authentication), productSlug, color, size, finalQuantity);
            if ("buyNow".equalsIgnoreCase(action)) {
                var variantOpt = productVariantRepository
                        .findFirstByProductSlugAndColorIgnoreCaseAndSizeIgnoreCaseAndActiveTrueAndProductActiveTrue(productSlug, color, size);
                variantOpt.ifPresent(v -> session.setAttribute("checkoutSkus", java.util.List.of(v.getSku())));
                return "redirect:/checkout";
            }
            redirectAttributes.addFlashAttribute("cartMessage", "Item added to cart successfully.");
            return "redirect:/products/" + productSlug;
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("cartError", exception.getMessage());
            return "redirect:/products/" + productSlug;
        }
    }

    @PostMapping("/cart/checkout")
    public String processCartSelection(@RequestParam(required = false) java.util.List<String> selectedSkus,
                                       HttpSession session,
                                       Authentication authentication,
                                       RedirectAttributes redirectAttributes) {
        if (isAdmin(authentication)) {
            redirectAttributes.addFlashAttribute("cartError", "Administrators cannot place orders. Please log in as a customer account.");
            return "redirect:/cart";
        }
        try {
            shoppingCartService.validateAndPrepareCheckoutSkus(session, customerEmail(authentication), selectedSkus);
            session.setAttribute("checkoutSkus", selectedSkus);
            return "redirect:/checkout";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("cartError", exception.getMessage());
            return "redirect:/cart";
        }
    }

    @PostMapping("/cart/update")
    public String updateCart(@RequestParam String sku,
                             @RequestParam int quantity,
                             HttpSession session,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            shoppingCartService.updateItem(session, customerEmail(authentication), sku, quantity);
            redirectAttributes.addFlashAttribute("cartMessage", "Cart updated successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("cartError", exception.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam String sku,
                                 HttpSession session,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            shoppingCartService.removeItem(session, customerEmail(authentication), sku);
            redirectAttributes.addFlashAttribute("cartMessage", "Item removed from cart.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("cartError", exception.getMessage());
        }
        return "redirect:/cart";
    }

    private String customerEmail(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return null;
        }
        boolean isCustomer = authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_CUSTOMER"));
        return isCustomer ? authentication.getName() : null;
    }

    private boolean isAdmin(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        return authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}
