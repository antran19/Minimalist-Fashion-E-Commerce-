package com.uminimalist.store.controller;

import com.uminimalist.store.entity.User;
import com.uminimalist.store.model.CartView;
import com.uminimalist.store.repository.UserRepository;
import com.uminimalist.store.service.CustomerAddressService;
import com.uminimalist.store.service.OrderService;
import com.uminimalist.store.service.ShoppingCartService;
import com.uminimalist.store.service.WishlistService;
import com.uminimalist.store.service.ProductReviewService;
import com.uminimalist.store.service.VNPayService;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.util.Map;
import java.util.HashMap;
import java.util.Enumeration;

@Controller
public class CustomerController {

    private final UserRepository userRepository;
    private final ShoppingCartService shoppingCartService;
    private final OrderService orderService;
    private final CustomerAddressService customerAddressService;
    private final WishlistService wishlistService;
    private final ProductReviewService productReviewService;
    private final VNPayService vnPayService;

    public CustomerController(UserRepository userRepository,
                              ShoppingCartService shoppingCartService,
                              OrderService orderService,
                              CustomerAddressService customerAddressService,
                              WishlistService wishlistService,
                              ProductReviewService productReviewService,
                              VNPayService vnPayService) {
        this.userRepository = userRepository;
        this.shoppingCartService = shoppingCartService;
        this.orderService = orderService;
        this.customerAddressService = customerAddressService;
        this.wishlistService = wishlistService;
        this.productReviewService = productReviewService;
        this.vnPayService = vnPayService;
    }

    @GetMapping("/account")
    public String account(Authentication authentication,
                          HttpSession session,
                          @RequestParam(defaultValue = "ALL") String status,
                          @RequestParam(defaultValue = "1") int page,
                          Model model) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        model.addAttribute("account", user);
        model.addAttribute("cart", shoppingCartService.getCart(session, authentication.getName()));
        model.addAttribute("shippingAddress", customerAddressService.findDefaultAddress(authentication.getName()));
        model.addAttribute("wishlist", wishlistService.findForCustomer(authentication.getName()));
        
        var paginatedOrders = orderService.findOrdersForCustomerPaginated(authentication.getName(), status, page, 5);
        model.addAttribute("paginatedOrders", paginatedOrders);
        model.addAttribute("recentOrders", paginatedOrders.orders());
        return "account";
    }

    @PostMapping("/account/profile")
    public String updateProfile(Authentication authentication,
                                @RequestParam String fullName,
                                @RequestParam String phone,
                                RedirectAttributes redirectAttributes) {
        String normalizedName = fullName == null ? "" : fullName.trim().replaceAll("\\s+", " ");
        String normalizedPhone = phone == null ? "" : phone.trim();

        if (normalizedName.length() < 2 || normalizedName.length() > 120) {
            redirectAttributes.addFlashAttribute("accountError", "Full name must be between 2 and 120 characters.");
            return "redirect:/account#profile";
        }
        if (normalizedPhone.length() < 8 || normalizedPhone.length() > 20) {
            redirectAttributes.addFlashAttribute("accountError", "Phone number must be between 8 and 20 characters.");
            return "redirect:/account#profile";
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        user.setFullName(normalizedName);
        user.setPhone(normalizedPhone);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("accountMessage", "Profile updated.");
        return "redirect:/account#profile";
    }

    @PostMapping("/account/address")
    public String updateAddress(Authentication authentication,
                                @RequestParam String recipientName,
                                @RequestParam String shippingPhone,
                                @RequestParam String addressLine,
                                @RequestParam String district,
                                @RequestParam String city,
                                RedirectAttributes redirectAttributes) {
        try {
            customerAddressService.saveDefaultAddress(authentication.getName(), recipientName, shippingPhone, addressLine, district, city);
            redirectAttributes.addFlashAttribute("accountMessage", "Shipping address saved.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("accountError", exception.getMessage());
        }
        return "redirect:/account#shipping";
    }

    @GetMapping("/account/orders/{orderCode}")
    public String orderDetail(@PathVariable String orderCode,
                              Authentication authentication,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        var order = orderService.findOrderForCustomer(authentication.getName(), orderCode);
        if (order.isEmpty()) {
            redirectAttributes.addFlashAttribute("accountError", "Order not found.");
            return "redirect:/account#orders";
        }
        model.addAttribute("account", user);
        model.addAttribute("order", order.get());
        return "order-detail";
    }

    @PostMapping("/account/orders/{orderCode}/cancel")
    public String cancelOrder(@PathVariable String orderCode,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            orderService.cancelOrderForCustomer(authentication.getName(), orderCode);
            redirectAttributes.addFlashAttribute("accountMessage", "Order " + orderCode + " cancelled.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("accountError", exception.getMessage());
        }
        return "redirect:/account/orders/" + orderCode;
    }

    @PostMapping("/account/orders/{orderCode}/reorder")
    public String reorder(@PathVariable String orderCode,
                          Authentication authentication,
                          HttpSession session,
                          RedirectAttributes redirectAttributes) {
        var order = orderService.findOrderForCustomer(authentication.getName(), orderCode);
        if (order.isEmpty()) {
            redirectAttributes.addFlashAttribute("accountError", "Order not found.");
            return "redirect:/account#orders";
        }

        int addedItems = 0;
        for (var item : order.get().items()) {
            try {
                shoppingCartService.addSku(session, authentication.getName(), item.sku(), item.quantity());
                addedItems += item.quantity();
            } catch (IllegalArgumentException ignored) {
                // Skip unavailable variants so one discontinued item does not block the rest of the reorder.
            }
        }

        if (addedItems == 0) {
            redirectAttributes.addFlashAttribute("accountError", "No items from this order are currently available.");
            return "redirect:/account/orders/" + orderCode;
        }

        redirectAttributes.addFlashAttribute("cartMessage", "Added " + addedItems + " item(s) from " + orderCode + " to cart.");
        return "redirect:/cart";
    }

    @PostMapping("/wishlist")
    public String addWishlist(@RequestParam String productSlug,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        wishlistService.add(authentication.getName(), productSlug);
        redirectAttributes.addFlashAttribute("cartMessage", "Saved to wishlist.");
        return "redirect:/products/" + productSlug;
    }

    @PostMapping("/wishlist/remove")
    public String removeWishlist(@RequestParam String productSlug,
                                 @RequestParam(defaultValue = "/account#wishlist") String redirectTo,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        wishlistService.remove(authentication.getName(), productSlug);
        redirectAttributes.addFlashAttribute("accountMessage", "Removed from wishlist.");
        return "redirect:" + redirectTo;
    }

    @GetMapping("/checkout")
    public String checkoutReview(Authentication authentication,
                                 HttpSession session,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        @SuppressWarnings("unchecked")
        java.util.List<String> checkoutSkus = (java.util.List<String>) session.getAttribute("checkoutSkus");
        CartView cart = shoppingCartService.getCartFiltered(session, authentication.getName(), checkoutSkus);
        if (cart.isEmpty()) {
            session.removeAttribute("checkoutSkus");
            redirectAttributes.addFlashAttribute("accountError", "No items selected for checkout.");
            return "redirect:/cart";
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        model.addAttribute("account", user);
        model.addAttribute("cart", cart);
        model.addAttribute("shippingAddress", customerAddressService.findDefaultAddress(authentication.getName()));
        return "checkout";
    }

    @PostMapping("/checkout")
    public String checkoutLegacyRedirect() {
        return "redirect:/checkout";
    }

    @PostMapping("/checkout/place")
    public String placeOrder(Authentication authentication,
                              HttpSession session,
                             @RequestParam String recipientName,
                             @RequestParam String shippingPhone,
                             @RequestParam String addressLine,
                             @RequestParam String district,
                             @RequestParam String city,
                             @RequestParam(defaultValue = "COD") String paymentMethod,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {
        @SuppressWarnings("unchecked")
        java.util.List<String> checkoutSkus = (java.util.List<String>) session.getAttribute("checkoutSkus");
        CartView cart = shoppingCartService.getCartFiltered(session, authentication.getName(), checkoutSkus);
        if (cart.isEmpty()) {
            session.removeAttribute("checkoutSkus");
            redirectAttributes.addFlashAttribute("accountError", "No items selected for checkout.");
            return "redirect:/cart";
        }

        String normalizedPaymentMethod = paymentMethod == null ? "COD" : paymentMethod.trim().toUpperCase(java.util.Locale.ROOT);
        if (!"COD".equals(normalizedPaymentMethod) && !"VNPAY".equals(normalizedPaymentMethod)) {
            redirectAttributes.addFlashAttribute("accountError", "Invalid payment method selected. Please choose Cash on Delivery (COD) or VNPay Demo.");
            return "redirect:/checkout";
        }

        try {
            var shippingAddress = customerAddressService.saveDefaultAddress(
                    authentication.getName(), recipientName, shippingPhone, addressLine, district, city);
            if ("VNPAY".equalsIgnoreCase(normalizedPaymentMethod)) {
                var pendingOrder = orderService.placePendingOrder(authentication.getName(), cart, shippingAddress);
                double usdAmount = Double.parseDouble(pendingOrder.totalLabel().replaceAll("[^0-9.]", ""));
                String paymentUrl = vnPayService.createPaymentUrl(pendingOrder.orderCode(), usdAmount, request);
                return "redirect:" + paymentUrl;
            }

            var order = orderService.placeOrder(authentication.getName(), cart, shippingAddress, normalizedPaymentMethod);
            
            // Clear only purchased items from the shopping cart
            java.util.List<String> purchasedSkus = cart.items().stream().map(com.uminimalist.store.model.CartItemView::sku).toList();
            shoppingCartService.removeItems(session, authentication.getName(), purchasedSkus);
            session.removeAttribute("checkoutSkus");

            return "redirect:/checkout/success?orderCode=" + order.orderCode();
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("accountError", exception.getMessage());
            return "redirect:/checkout";
        }
    }

    @GetMapping("/checkout/success")
    public String checkoutSuccess(@RequestParam String orderCode,
                                 Authentication authentication,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        var orderOpt = orderService.findOrderForCustomer(authentication.getName(), orderCode);
        if (orderOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("accountError", "Order not found.");
            return "redirect:/account";
        }
        model.addAttribute("order", orderOpt.get());
        model.addAttribute("cartCount", 0);
        return "checkout-success";
    }

    @GetMapping("/checkout/payment-return")
    public String paymentReturn(HttpServletRequest request,
                                Authentication authentication,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        Map<String, String> fields = new HashMap<>();
        for (Enumeration<String> names = request.getParameterNames(); names.hasMoreElements();) {
            String name = names.nextElement();
            fields.put(name, request.getParameter(name));
        }

        String orderCode = fields.get("vnp_TxnRef");
        String responseCode = fields.get("vnp_ResponseCode");

        if (orderCode == null || orderCode.isBlank()) {
            redirectAttributes.addFlashAttribute("accountError", "Invalid VNPay response: missing order reference.");
            return "redirect:/account#orders";
        }

        String username = authentication != null ? authentication.getName() : null;
        if (username == null && request.getUserPrincipal() != null) {
            username = request.getUserPrincipal().getName();
        }
        String customerEmail = username;

        boolean verified = vnPayService.verifyCallback(fields);
        if (verified && "00".equals(responseCode)) {
            try {
                var order = orderService.confirmPaidOrder(customerEmail != null ? customerEmail : username, orderCode);
                shoppingCartService.clearCart(session, customerEmail != null ? customerEmail : username);
                model.addAttribute("order", order);
                model.addAttribute("cartCount", 0);
                return "checkout-success";
            } catch (Exception exception) {
                redirectAttributes.addFlashAttribute("accountError", exception.getMessage());
            }
        } else {
            try {
                orderService.markPaymentFailed(customerEmail != null ? customerEmail : username, orderCode);
            } catch (Exception ignored) {
            }
            redirectAttributes.addFlashAttribute("accountError", "Payment failed or canceled for Order " + orderCode);
        }

        return "redirect:/account#orders";
    }

    @PostMapping("/products/{slug}/reviews")
    public String addReview(@PathVariable String slug,
                            Authentication authentication,
                            @RequestParam int rating,
                            @RequestParam String comment,
                            RedirectAttributes redirectAttributes) {
        try {
            productReviewService.addReview(authentication.getName(), slug, rating, comment);
            redirectAttributes.addFlashAttribute("cartMessage", "Review submitted successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("cartError", exception.getMessage());
        }
        return "redirect:/products/" + slug;
    }
}
