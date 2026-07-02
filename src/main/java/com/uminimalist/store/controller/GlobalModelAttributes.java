package com.uminimalist.store.controller;

import com.uminimalist.store.service.ShoppingCartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class GlobalModelAttributes {

    private final ShoppingCartService shoppingCartService;

    public GlobalModelAttributes(ShoppingCartService shoppingCartService) {
        this.shoppingCartService = shoppingCartService;
    }

    @ModelAttribute("cartCount")
    public int cartCount(HttpSession session) {
        return shoppingCartService.getItemCount(session);
    }
}
