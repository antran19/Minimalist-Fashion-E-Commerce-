package com.uminimalist.store.controller;

import com.uminimalist.store.entity.User;
import com.uminimalist.store.model.CartView;
import com.uminimalist.store.repository.UserRepository;
import com.uminimalist.store.service.OrderService;
import com.uminimalist.store.service.ShoppingCartService;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CustomerController {

    private final UserRepository userRepository;
    private final ShoppingCartService shoppingCartService;
    private final OrderService orderService;

    public CustomerController(UserRepository userRepository,
                              ShoppingCartService shoppingCartService,
                              OrderService orderService) {
        this.userRepository = userRepository;
        this.shoppingCartService = shoppingCartService;
        this.orderService = orderService;
    }

    @GetMapping("/account")
    public String account(Authentication authentication, HttpSession session, Model model) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        model.addAttribute("account", user);
        model.addAttribute("cart", shoppingCartService.getCart(session));
        model.addAttribute("recentOrders", orderService.findOrdersForCustomer(authentication.getName()));
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

        try {
            var order = orderService.placeOrder(authentication.getName(), cart);
            shoppingCartService.clearCart(session);
            redirectAttributes.addFlashAttribute("accountMessage",
                    "Order " + order.orderCode() + " placed successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("accountError", exception.getMessage());
            return "redirect:/cart";
        }
        return "redirect:/account";
    }
}
