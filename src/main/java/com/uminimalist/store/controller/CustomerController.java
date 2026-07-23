package com.uminimalist.store.controller;

import com.uminimalist.store.entity.ProductVariant;
import com.uminimalist.store.entity.User;
import com.uminimalist.store.model.CartView;
import com.uminimalist.store.repository.ProductVariantRepository;
import com.uminimalist.store.repository.UserRepository;
import com.uminimalist.store.service.CustomerAddressService;
import com.uminimalist.store.service.OrderService;
import com.uminimalist.store.service.ShoppingCartService;
import com.uminimalist.store.service.WishlistService;
import com.uminimalist.store.service.ProductReviewService;
import com.uminimalist.store.service.PayPalService;
import com.uminimalist.store.util.PhoneValidator;
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
    private final ProductVariantRepository productVariantRepository;
    private final ShoppingCartService shoppingCartService;
    private final OrderService orderService;
    private final CustomerAddressService customerAddressService;
    private final WishlistService wishlistService;
    private final ProductReviewService productReviewService;
    private final PayPalService payPalService;

    public CustomerController(UserRepository userRepository,
                              ProductVariantRepository productVariantRepository,
                              ShoppingCartService shoppingCartService,
                              OrderService orderService,
                              CustomerAddressService customerAddressService,
                              WishlistService wishlistService,
                              ProductReviewService productReviewService,
                              PayPalService payPalService) {
        this.userRepository = userRepository;
        this.productVariantRepository = productVariantRepository;
        this.shoppingCartService = shoppingCartService;
        this.orderService = orderService;
        this.customerAddressService = customerAddressService;
        this.wishlistService = wishlistService;
        this.productReviewService = productReviewService;
        this.payPalService = payPalService;
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

        String normalizedName = fullName == null
                ? ""
                : fullName.trim().replaceAll("\\s+", " ");

        if (normalizedName.length() < 2 || normalizedName.length() > 120 || !normalizedName.matches(".*\\p{L}.*")) {
            redirectAttributes.addFlashAttribute(
                    "accountError",
                    "Full name must be between 2 and 120 characters and contain at least one letter."
            );

            return "redirect:/account#profile";
        }

        String normalizedPhone;

        try {
            normalizedPhone = PhoneValidator.normalizeVietnameseMobile(phone);
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute(
                    "accountError",
                    exception.getMessage()
            );

            return "redirect:/account#profile";
        }

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new IllegalArgumentException("User not found."));

        user.setFullName(normalizedName);
        user.setPhone(normalizedPhone);
        userRepository.save(user);

        redirectAttributes.addFlashAttribute(
                "accountMessage",
                "Profile updated."
        );

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
        java.util.List<String> warnings = new java.util.ArrayList<>();
        for (var item : order.get().items()) {
            try {
                shoppingCartService.addSku(session, authentication.getName(), item.sku(), item.quantity());
                addedItems += item.quantity();
            } catch (IllegalArgumentException ex) {
                // If requested quantity exceeds stock, attempt to add available stock quantity
                    int availableStock = productVariantRepository.findBySkuIgnoreCase(item.sku())
                            .map(ProductVariant::getStockQuantity)
                            .orElse(0);
                    if (availableStock > 0) {
                        shoppingCartService.addSku(session, authentication.getName(), item.sku(), availableStock);
                        addedItems += availableStock;
                        warnings.add("Added " + availableStock + " item(s) of " + item.productName() + " (only " + availableStock + " in stock).");
                    }
                } catch (IllegalArgumentException ignored) {
                    // Skip completely unavailable or out-of-stock variants
                }
            }
        }

        if (addedItems == 0) {
            redirectAttributes.addFlashAttribute("accountError", "No items from this order are currently available.");
            return "redirect:/account/orders/" + orderCode;
        }

        String msg = "Added " + addedItems + " item(s) from " + orderCode + " to cart.";
        if (!warnings.isEmpty()) {
            msg += " " + String.join(" ", warnings);
        }
        redirectAttributes.addFlashAttribute("cartMessage", msg);
        return "redirect:/cart";
    }

    @PostMapping("/wishlist")
    public String addWishlist(@RequestParam String productSlug,
                              Authentication authentication,
                              RedirectAttributes redirectAttributes) {
        try {
            wishlistService.add(authentication.getName(), productSlug);
            redirectAttributes.addFlashAttribute("cartMessage", "Saved to wishlist.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("cartError", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("cartError", "This product is no longer available.");
        }
        return "redirect:/products/" + productSlug;
    }

    @PostMapping("/wishlist/remove")
    public String removeWishlist(@RequestParam String productSlug,
                                 @RequestParam(defaultValue = "/account#wishlist") String redirectTo,
                                 Authentication authentication,
                                 RedirectAttributes redirectAttributes) {
        try {
            wishlistService.remove(authentication.getName(), productSlug);
            redirectAttributes.addFlashAttribute("accountMessage", "Removed from wishlist.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("accountError", ex.getMessage());
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("accountError", "This product is no longer available.");
        }
        String safeRedirect = sanitizeInternalRedirect(redirectTo, "/account#wishlist");
        return "redirect:" + safeRedirect;
    }

    private String sanitizeInternalRedirect(String redirectTo, String fallback) {
        if (redirectTo == null || !redirectTo.startsWith("/")) {
            return fallback;
        }
        if (redirectTo.startsWith("//") || redirectTo.startsWith("/\\")) {
            return fallback;
        }
        if (redirectTo.startsWith("/account") || redirectTo.startsWith("/products") || redirectTo.equals("/cart") || redirectTo.equals("/")) {
            return redirectTo;
        }
        return fallback;
    }

    @GetMapping("/checkout")
    public String checkoutReview(Authentication authentication,
                                 HttpSession session,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        if (isAdmin(authentication)) {
            session.removeAttribute("checkoutSkus");
            redirectAttributes.addFlashAttribute("cartError", "Administrator accounts cannot place orders. Please sign in with a customer account to shop.");
            return "redirect:/cart";
        }
        @SuppressWarnings("unchecked")
        java.util.List<String> checkoutSkus = (java.util.List<String>) session.getAttribute("checkoutSkus");
        try {
            shoppingCartService.validateAndPrepareCheckoutSkus(session, authentication.getName(), checkoutSkus);
        } catch (IllegalArgumentException exception) {
            session.removeAttribute("checkoutSkus");
            redirectAttributes.addFlashAttribute("cartError", exception.getMessage());
            return "redirect:/cart";
        }

        CartView cart = shoppingCartService.getCartFiltered(session, authentication.getName(), checkoutSkus);
        if (cart.isEmpty()) {
            session.removeAttribute("checkoutSkus");
            redirectAttributes.addFlashAttribute("cartError", "No items selected for checkout.");
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
                             @RequestParam(required = false) String notes,
                             @RequestParam(defaultValue = "COD") String paymentMethod,
                             HttpServletRequest request,
                             RedirectAttributes redirectAttributes) {
        if (isAdmin(authentication)) {
            session.removeAttribute("checkoutSkus");
            redirectAttributes.addFlashAttribute("cartError", "Administrator accounts cannot place orders. Please sign in with a customer account to shop.");
            return "redirect:/cart";
        }
        @SuppressWarnings("unchecked")
        java.util.List<String> checkoutSkus = (java.util.List<String>) session.getAttribute("checkoutSkus");
        try {
            shoppingCartService.validateAndPrepareCheckoutSkus(session, authentication.getName(), checkoutSkus);
        } catch (IllegalArgumentException exception) {
            session.removeAttribute("checkoutSkus");
            redirectAttributes.addFlashAttribute("cartError", exception.getMessage());
            return "redirect:/cart";
        }

        CartView cart = shoppingCartService.getCartFiltered(session, authentication.getName(), checkoutSkus);
        if (cart.isEmpty()) {
            session.removeAttribute("checkoutSkus");
            redirectAttributes.addFlashAttribute("cartError", "No items selected for checkout.");
            return "redirect:/cart";
        }

        String normalizedPaymentMethod = paymentMethod == null ? "COD" : paymentMethod.trim().toUpperCase(java.util.Locale.ROOT);
        if (!"COD".equals(normalizedPaymentMethod) && !"PAYPAL".equals(normalizedPaymentMethod)) {
            redirectAttributes.addFlashAttribute("accountError", "Invalid payment method selected. Please choose Cash on Delivery (COD) or PayPal.");
            return "redirect:/checkout";
        }

        try {
            var shippingAddress = customerAddressService.saveDefaultAddress(
                    authentication.getName(), recipientName, shippingPhone, addressLine, district, city);
            if ("PAYPAL".equalsIgnoreCase(normalizedPaymentMethod)) {
                var pendingOrder = orderService.placePendingOrder(authentication.getName(), cart, shippingAddress, notes);
                double usdAmount = Double.parseDouble(pendingOrder.totalLabel().replaceAll("[^0-9.]", ""));
                
                try {
                    com.paypal.api.payments.Payment payment = payPalService.createPayment(
                            usdAmount * 25000, // Pass VND amount since createPayment handles conversion
                            "sale",
                            "Order " + pendingOrder.orderCode(),
                            "http://localhost:9090/paypal/cancel",
                            "http://localhost:9090/paypal/success"
                    );
                    for (com.paypal.api.payments.Links link : payment.getLinks()) {
                        if (link.getRel().equals("approval_url")) {
                            return "redirect:" + link.getHref();
                        }
                    }
                } catch (Exception e) {
                    throw new IllegalArgumentException("Error redirecting to PayPal: " + e.getMessage());
                }
            }

            var order = orderService.placeOrder(authentication.getName(), cart, shippingAddress, normalizedPaymentMethod, notes);
            
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
        return "checkout-success";
    }

    @GetMapping("/paypal/success")
    public String paypalSuccess(@RequestParam("paymentId") String paymentId,
                                @RequestParam("PayerID") String payerId,
                                Authentication authentication,
                                HttpSession session,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            com.paypal.api.payments.Payment payment = payPalService.executePayment(paymentId, payerId);
            if (payment.getState().equals("approved")) {
                String description = payment.getTransactions().get(0).getDescription();
                String orderCode = description.replace("Order ", "");

                String username = authentication != null ? authentication.getName() : null;
                var order = orderService.confirmPaidOrder(username, orderCode);
                
                java.util.List<String> orderSkus = orderService.getOrderSkus(orderCode);
                shoppingCartService.removeItems(session, username, orderSkus);
                session.removeAttribute("checkoutSkus");

                model.addAttribute("cartCount", shoppingCartService.getItemCount(session, username));
                model.addAttribute("order", order);
                return "checkout-success";
            }
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("accountError", "Payment failed: " + e.getMessage());
        }
        return "redirect:/account#orders";
    }

    @GetMapping("/paypal/cancel")
    public String paypalCancel(RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("accountError", "Payment was canceled.");
        return "redirect:/account#orders";
    }

    @PostMapping("/account/orders/{orderCode}/pay-again")
    public String payAgain(@PathVariable String orderCode,
                           Authentication authentication,
                           HttpServletRequest request,
                           RedirectAttributes redirectAttributes) {
        try {
            String paymentUrl = orderService.preparePayAgainUrl(authentication.getName(), orderCode, payPalService);
            return "redirect:" + paymentUrl;
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("accountError", exception.getMessage());
            return "redirect:/account#orders";
        }
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

    private boolean isAdmin(Authentication authentication) {
        return authentication != null
                && authentication.getAuthorities()
                .stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));
    }
}
