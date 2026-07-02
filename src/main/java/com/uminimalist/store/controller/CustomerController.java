package com.uminimalist.store.controller;

import com.uminimalist.store.entity.User;
import com.uminimalist.store.model.CartView;
import com.uminimalist.store.repository.UserRepository;
import com.uminimalist.store.service.ShoppingCartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;

@Controller
public class CustomerController {

    private final UserRepository userRepository;
    private final ShoppingCartService shoppingCartService;

    public CustomerController(UserRepository userRepository, ShoppingCartService shoppingCartService) {
        this.userRepository = userRepository;
        this.shoppingCartService = shoppingCartService;
    }

    @GetMapping("/account")
    public String account(Authentication authentication, HttpSession session, Model model) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        model.addAttribute("account", user);
        model.addAttribute("cart", shoppingCartService.getCart(session));
        model.addAttribute("recentOrders", demoOrders());
        return "account";
    }

    @PostMapping("/checkout")
    public String checkout(Authentication authentication,
                           HttpSession session,
                           RedirectAttributes redirectAttributes) {
        CartView cart = shoppingCartService.getCart(session);
        if (cart.isEmpty()) {
            redirectAttributes.addFlashAttribute("accountError", "Your cart is empty.");
            return "redirect:/cart";
        }

        String orderCode = "UM-" + LocalDate.now().toString().replace("-", "") + "-" + Math.max(1000, cart.itemCount() * 137);
        shoppingCartService.clearCart(session);
        redirectAttributes.addFlashAttribute("accountMessage",
                "Demo order " + orderCode + " placed for " + authentication.getName() + ".");
        return "redirect:/account";
    }

    private List<String> demoOrders() {
        return List.of(
                "No paid order history yet. Cart checkout currently creates a demo confirmation.",
                "Next phase: persist orders, payment state, shipping address, and order items."
        );
    }
}
