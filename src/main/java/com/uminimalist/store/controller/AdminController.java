package com.uminimalist.store.controller;

import com.uminimalist.store.entity.Category;
import com.uminimalist.store.entity.Product;
import com.uminimalist.store.entity.User;
import com.uminimalist.store.service.AdminCatalogService;
import com.uminimalist.store.service.OrderService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

@Controller
public class AdminController {

    private final AdminCatalogService adminCatalogService;
    private final OrderService orderService;

    public AdminController(AdminCatalogService adminCatalogService, OrderService orderService) {
        this.adminCatalogService = adminCatalogService;
        this.orderService = orderService;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(@RequestParam(required = false, defaultValue = "ALL") String orderStatus, Model model) {
        List<User> users = adminCatalogService.getUsers();
        List<Product> products = adminCatalogService.getProducts();
        List<Category> categories = adminCatalogService.getCategories();

        int activeProducts = (int) products.stream().filter(Product::isActive).count();
        int totalVariants = products.stream().mapToInt(product -> product.getVariants().size()).sum();
        int lowStockVariants = products.stream()
                .flatMap(product -> product.getVariants().stream())
                .mapToInt(variant -> variant.getStockQuantity() <= 5 ? 1 : 0)
                .sum();

        model.addAttribute("users", users);
        model.addAttribute("products", products);
        model.addAttribute("categories", categories);
        model.addAttribute("productTypes", adminCatalogService.getProductTypes());
        model.addAttribute("activeProducts", activeProducts);
        model.addAttribute("totalVariants", totalVariants);
        model.addAttribute("lowStockVariants", lowStockVariants);
        model.addAttribute("orders", orderService.findOrdersByStatus(orderStatus));
        model.addAttribute("selectedStatus", orderStatus);
        model.addAttribute("orderCount", orderService.countOrders());
        model.addAttribute("totalRevenue", orderService.totalRevenueLabel());
        return "admin/dashboard";
    }

    @PostMapping("/admin/categories/create")
    public String createCategory(@RequestParam String name,
                                 @RequestParam(required = false) String description,
                                 RedirectAttributes redirectAttributes) {
        try {
            adminCatalogService.createCategory(name, description);
            redirectAttributes.addFlashAttribute("adminMessage", "Category created successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("adminError", exception.getMessage());
        }
        return "redirect:/admin/dashboard#categories";
    }

    @PostMapping("/admin/categories/{id}/toggle")
    public String toggleCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        adminCatalogService.toggleCategory(id);
        redirectAttributes.addFlashAttribute("adminMessage", "Category status updated.");
        return "redirect:/admin/dashboard#categories";
    }

    @PostMapping("/admin/products/create")
    public String createProduct(@RequestParam Long categoryId,
                                @RequestParam String name,
                                @RequestParam String basePrice,
                                @RequestParam(required = false) String productType,
                                @RequestParam(required = false) String description,
                                RedirectAttributes redirectAttributes) {
        try {
            adminCatalogService.createProduct(categoryId, name, basePrice, productType, description);
            redirectAttributes.addFlashAttribute("adminMessage", "Product created successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("adminError", exception.getMessage());
        }
        return "redirect:/admin/dashboard#catalog";
    }

    @PostMapping("/admin/products/{id}/edit")
    public String updateProduct(@PathVariable Long id,
                                @RequestParam Long categoryId,
                                @RequestParam String name,
                                @RequestParam String basePrice,
                                @RequestParam(required = false) String productType,
                                @RequestParam(required = false) String description,
                                RedirectAttributes redirectAttributes) {
        try {
            adminCatalogService.updateProduct(id, categoryId, name, basePrice, productType, description);
            redirectAttributes.addFlashAttribute("adminMessage", "Product updated successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("adminError", exception.getMessage());
        }
        return "redirect:/admin/dashboard#catalog";
    }

    @PostMapping("/admin/variants/create")
    public String createVariant(@RequestParam Long productId,
                                @RequestParam String color,
                                @RequestParam String size,
                                @RequestParam String sku,
                                @RequestParam int stockQuantity,
                                RedirectAttributes redirectAttributes) {
        try {
            adminCatalogService.createVariant(productId, color, size, sku, stockQuantity);
            redirectAttributes.addFlashAttribute("adminMessage", "Variant created successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("adminError", exception.getMessage());
        }
        return "redirect:/admin/dashboard#catalog";
    }

    @PostMapping("/admin/products/{id}/toggle")
    public String toggleProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        adminCatalogService.toggleProduct(id);
        redirectAttributes.addFlashAttribute("adminMessage", "Product status updated.");
        return "redirect:/admin/dashboard#catalog";
    }

    @PostMapping("/admin/variants/{id}/stock")
    public String updateVariantStock(@PathVariable Long id,
                                     @RequestParam int stockQuantity,
                                     RedirectAttributes redirectAttributes) {
        adminCatalogService.updateVariantStock(id, stockQuantity);
        redirectAttributes.addFlashAttribute("adminMessage", "Variant stock updated.");
        return "redirect:/admin/dashboard#catalog";
    }

    @PostMapping("/admin/variants/{id}/toggle")
    public String toggleVariant(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        adminCatalogService.toggleVariant(id);
        redirectAttributes.addFlashAttribute("adminMessage", "Variant status updated.");
        return "redirect:/admin/dashboard#catalog";
    }

    @PostMapping("/admin/users/{id}/toggle")
    public String toggleUser(@PathVariable Long id,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            adminCatalogService.toggleUser(id, authentication.getName());
            redirectAttributes.addFlashAttribute("adminMessage", "User status updated.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("adminError", exception.getMessage());
        }
        return "redirect:/admin/dashboard#users";
    }

    @PostMapping("/admin/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                    @RequestParam String status,
                                    RedirectAttributes redirectAttributes) {
        try {
            orderService.updateOrderStatus(id, status);
            redirectAttributes.addFlashAttribute("adminMessage", "Order status updated to " + status + ".");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("adminError", exception.getMessage());
        }
        return "redirect:/admin/dashboard#orders";
    }
}
