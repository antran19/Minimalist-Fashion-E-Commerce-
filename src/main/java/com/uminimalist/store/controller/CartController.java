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

    public CartController(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
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
                            @RequestParam(defaultValue = "1") int quantity,
                            HttpSession session,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {
        try {
            shoppingCartService.addItem(session, customerEmail(authentication), productSlug, color, size, quantity);
            redirectAttributes.addFlashAttribute("cartMessage", "Added to cart.");
            return "redirect:/cart";
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("cartError", exception.getMessage());
            return "redirect:/products/" + productSlug;
        }
    }

    @PostMapping("/cart/update")
    public String updateCart(@RequestParam String sku,
                             @RequestParam int quantity,
                             HttpSession session,
                             Authentication authentication) {
        shoppingCartService.updateItem(session, customerEmail(authentication), sku, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam String sku,
                                 HttpSession session,
                                 Authentication authentication) {
        shoppingCartService.removeItem(session, customerEmail(authentication), sku);
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
}
