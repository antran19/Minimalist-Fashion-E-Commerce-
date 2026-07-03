package com.uminimalist.store.controller;

import com.uminimalist.store.service.ShoppingCartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final ShoppingCartService shoppingCartService;

    public GlobalModelAttributes(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    @ModelAttribute("cartCount")
    public int cartCount(HttpSession session, Authentication authentication) {
        return shoppingCartService.getItemCount(session, customerEmail(authentication));
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
