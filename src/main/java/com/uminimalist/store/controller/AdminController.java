package com.uminimalist.store.controller;

import com.uminimalist.store.entity.Product;
import com.uminimalist.store.entity.User;
import com.uminimalist.store.service.AdminCatalogService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class AdminController {

    private final AdminCatalogService adminCatalogService;

    public AdminController(AdminCatalogService adminCatalogService) {
        this.adminCatalogService = adminCatalogService;
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(Model model) {
        List<User> users = adminCatalogService.getUsers();
        List<Product> products = adminCatalogService.getProducts();

        int activeProducts = (int) products.stream().filter(Product::isActive).count();
        int totalVariants = products.stream().mapToInt(product -> product.getVariants().size()).sum();
        int lowStockVariants = products.stream()
                .flatMap(product -> product.getVariants().stream())
                .mapToInt(variant -> variant.getStockQuantity() <= 5 ? 1 : 0)
                .sum();

        model.addAttribute("users", users);
        model.addAttribute("products", products);
        model.addAttribute("activeProducts", activeProducts);
        model.addAttribute("totalVariants", totalVariants);
        model.addAttribute("lowStockVariants", lowStockVariants);
        return "admin/dashboard";
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
}
