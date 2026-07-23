package com.uminimalist.store.controller;

import com.uminimalist.store.entity.Category;
import com.uminimalist.store.entity.Product;
import com.uminimalist.store.entity.ProductVariant;
import com.uminimalist.store.entity.User;
import com.uminimalist.store.model.PaginatedOrders;
import com.uminimalist.store.service.AdminCatalogService;
import com.uminimalist.store.service.OrderService;
import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

@Controller
public class AdminController {

    private final AdminCatalogService adminCatalogService;
    private final OrderService orderService;

    public AdminController(AdminCatalogService adminCatalogService, OrderService orderService) {
        this.adminCatalogService = adminCatalogService;
        this.orderService = orderService;
    }

    @GetMapping("/admin")
    public String adminRoot() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/admin/dashboard")
    public String dashboard(@RequestParam(required = false, defaultValue = "0") int catalogPage,
                            @RequestParam(required = false) String catalogQuery,
                            @RequestParam(required = false, defaultValue = "all") String catalogFilter,
                            @RequestParam(required = false, defaultValue = "0") int userPage,
                            @RequestParam(required = false) String userQuery,
                            @RequestParam(required = false, defaultValue = "0") int orderPage,
                            @RequestParam(required = false) String orderQuery,
                            @RequestParam(required = false, defaultValue = "ALL") String orderStatus,
                            @RequestParam(required = false, defaultValue = "0") int categoryPage,
                            @RequestParam(required = false) String categoryQuery,
                            Model model) {
        // System-wide statistics for top stat cards
        List<Product> allProducts = adminCatalogService.getProducts();
        int activeProducts = (int) allProducts.stream().filter(Product::isActive).count();
        int totalVariants = allProducts.stream().mapToInt(product -> product.getVariants().size()).sum();
        
        List<ProductVariant> allLowStockList = allProducts.stream()
                .flatMap(product -> product.getVariants().stream())
                .filter(variant -> variant.getStockQuantity() <= 5)
                .sorted(Comparator.comparingInt(ProductVariant::getStockQuantity))
                .toList();
        int lowStockVariants = allLowStockList.size();
        List<ProductVariant> urgentLowStockVariants = allLowStockList.stream().limit(5).toList();

        // Paginated Backend Data Sets
        Page<Product> productsPage = adminCatalogService.getProductsPaged(catalogPage, 5, catalogQuery, catalogFilter);
        Page<User> usersPage = adminCatalogService.getUsersPaged(userPage, 10, userQuery);
        Page<Category> categoriesPage = adminCatalogService.getCategoriesPaged(categoryPage, 10, categoryQuery);
        PaginatedOrders paginatedOrders = orderService.findPaginatedOrders(orderPage, 10, orderQuery, orderStatus);

        model.addAttribute("productsPage", productsPage);
        model.addAttribute("products", productsPage.getContent());
        model.addAttribute("allProducts", allProducts);

        model.addAttribute("usersPage", usersPage);
        model.addAttribute("users", usersPage.getContent());

        model.addAttribute("categoriesPage", categoriesPage);
        model.addAttribute("categories", categoriesPage.getContent());

        model.addAttribute("ordersPage", paginatedOrders);
        model.addAttribute("orders", paginatedOrders.orders());

        model.addAttribute("productTypes", adminCatalogService.getProductTypes());
        model.addAttribute("activeProducts", activeProducts);
        model.addAttribute("totalVariants", totalVariants);
        model.addAttribute("lowStockVariants", lowStockVariants);
        model.addAttribute("urgentLowStockVariants", urgentLowStockVariants);
        model.addAttribute("orderCount", orderService.countOrders());
        model.addAttribute("totalRevenue", orderService.totalRevenueLabel());

        // Keep current search & filter state params for Thymeleaf pagination links
        model.addAttribute("catalogPage", catalogPage);
        model.addAttribute("catalogQuery", catalogQuery != null ? catalogQuery : "");
        model.addAttribute("catalogFilter", catalogFilter != null ? catalogFilter : "all");

        model.addAttribute("userPage", userPage);
        model.addAttribute("userQuery", userQuery != null ? userQuery : "");

        model.addAttribute("orderPage", orderPage);
        model.addAttribute("orderQuery", orderQuery != null ? orderQuery : "");
        model.addAttribute("selectedStatus", orderStatus != null ? orderStatus : "ALL");

        model.addAttribute("categoryPage", categoryPage);
        model.addAttribute("categoryQuery", categoryQuery != null ? categoryQuery : "");

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
            redirectAttributes.addFlashAttribute("adminErrorModal", "#addCategoryModal");
        }
        return "redirect:/admin/dashboard#categories";
    }

    @PostMapping("/admin/categories/{id}/edit")
    public String updateCategory(@PathVariable Long id,
                                 @RequestParam String name,
                                 @RequestParam(required = false) String description,
                                 RedirectAttributes redirectAttributes) {
        try {
            adminCatalogService.updateCategory(id, name, description);
            redirectAttributes.addFlashAttribute("adminMessage", "Category updated successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("adminError", exception.getMessage());
            redirectAttributes.addFlashAttribute("adminErrorModal", "#editCategoryModal");
        }
        return "redirect:/admin/dashboard#categories";
    }

    @PostMapping("/admin/categories/{id}/toggle")
    public String toggleCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        adminCatalogService.toggleCategory(id);
        redirectAttributes.addFlashAttribute("adminMessage", "Category status updated.");
        return "redirect:/admin/dashboard#categories";
    }

    @PostMapping("/admin/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminCatalogService.deleteCategory(id);
            redirectAttributes.addFlashAttribute("adminMessage", "Category deleted successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("adminError", exception.getMessage());
        }
        return "redirect:/admin/dashboard#categories";
    }

    @PostMapping("/admin/products/create")
    public String createProduct(@RequestParam Long categoryId,
                                @RequestParam String name,
                                @RequestParam String basePrice,
                                @RequestParam(required = false) String productType,
                                @RequestParam(required = false) String description,
                                @RequestParam(name = "onSale", defaultValue = "false") boolean onSale,
                                @RequestParam(required = false) Integer discountPercentage,
                                RedirectAttributes redirectAttributes) {
        try {
            adminCatalogService.createProduct(categoryId, name, basePrice, productType, description, onSale, discountPercentage);
            redirectAttributes.addFlashAttribute("adminMessage", "Product created successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("adminError", exception.getMessage());
            redirectAttributes.addFlashAttribute("adminErrorModal", "#addProductModal");
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
                                @RequestParam(name = "onSale", defaultValue = "false") boolean onSale,
                                @RequestParam(required = false) Integer discountPercentage,
                                RedirectAttributes redirectAttributes) {
        try {
            adminCatalogService.updateProduct(id, categoryId, name, basePrice, productType, description, onSale, discountPercentage);
            redirectAttributes.addFlashAttribute("adminMessage", "Product updated successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("adminError", exception.getMessage());
            redirectAttributes.addFlashAttribute("adminErrorModal", "#editProductModal");
        }
        return "redirect:/admin/dashboard#catalog";
    }

    @PostMapping("/admin/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminCatalogService.deleteProduct(id);
            redirectAttributes.addFlashAttribute("adminMessage", "Product deleted successfully.");
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
                                @RequestParam(required = false) MultipartFile imageFile,
                                RedirectAttributes redirectAttributes) {
        try {
            adminCatalogService.createVariant(productId, color, size, sku, stockQuantity, imageFile);
            redirectAttributes.addFlashAttribute("adminMessage", "Variant created successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("adminError", exception.getMessage());
            redirectAttributes.addFlashAttribute("adminErrorModal", "#addVariantModal");
            redirectAttributes.addFlashAttribute("adminErrorProductId", productId);
        }
        return "redirect:/admin/dashboard#catalog";
    }

    @PostMapping("/admin/variants/{id}/edit")
    public String updateVariant(@PathVariable Long id,
                                @RequestParam String color,
                                @RequestParam String size,
                                @RequestParam String sku,
                                @RequestParam int stockQuantity,
                                @RequestParam(required = false) MultipartFile imageFile,
                                RedirectAttributes redirectAttributes) {
        try {
            adminCatalogService.updateVariant(id, color, size, sku, stockQuantity, imageFile);
            redirectAttributes.addFlashAttribute("adminMessage", "Variant updated successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("adminError", exception.getMessage());
            redirectAttributes.addFlashAttribute("adminErrorModal", "#editVariantModal");
        }
        return "redirect:/admin/dashboard#catalog";
    }

    @PostMapping("/admin/variants/{id}/image")
    public String replaceVariantImage(@PathVariable Long id,
                                      @RequestParam("imageFile") MultipartFile imageFile,
                                      RedirectAttributes redirectAttributes) {
        try {
            adminCatalogService.replaceVariantImage(id, imageFile);
            redirectAttributes.addFlashAttribute("adminMessage", "Variant image updated successfully.");
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

    @PostMapping("/admin/variants/{id}/delete")
    public String deleteVariant(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            adminCatalogService.deleteVariant(id);
            redirectAttributes.addFlashAttribute("adminMessage", "Variant deleted successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("adminError", exception.getMessage());
        }
        return "redirect:/admin/dashboard#catalog";
    }

    @PostMapping("/admin/products/{id}/images/upload")
    public String uploadProductImage(@PathVariable Long id,
                                     @RequestParam("imageFile") MultipartFile imageFile,
                                     @RequestParam(required = false) String color,
                                     @RequestParam(required = false, defaultValue = "false") boolean isPrimary,
                                     RedirectAttributes redirectAttributes) {
        try {
            adminCatalogService.uploadProductImage(id, imageFile, color, isPrimary);
            redirectAttributes.addFlashAttribute("adminMessage", "Product image uploaded successfully to Cloudinary.");
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("adminError", exception.getMessage());
        }
        return "redirect:/admin/dashboard#catalog";
    }

    @PostMapping("/admin/products/{productId}/images/{imageId}/primary")
    public String setPrimaryProductImage(@PathVariable Long productId,
                                         @PathVariable Long imageId,
                                         RedirectAttributes redirectAttributes) {
        try {
            adminCatalogService.setPrimaryProductImage(productId, imageId);
            redirectAttributes.addFlashAttribute("adminMessage", "Primary product image updated.");
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("adminError", exception.getMessage());
        }
        return "redirect:/admin/dashboard#catalog";
    }

    @PostMapping("/admin/products/{productId}/images/primary-by-url")
    public String setPrimaryProductImageByUrl(@PathVariable Long productId,
                                              @RequestParam String imageUrl,
                                              @RequestParam(required = false) String color,
                                              RedirectAttributes redirectAttributes) {
        try {
            adminCatalogService.setPrimaryProductImageByUrl(productId, imageUrl, color);
            redirectAttributes.addFlashAttribute("adminMessage", "Primary product representative image updated successfully.");
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("adminError", exception.getMessage());
        }
        return "redirect:/admin/dashboard#catalog";
    }

    @PostMapping("/admin/products/images/{imageId}/delete")
    public String deleteProductImage(@PathVariable Long imageId, RedirectAttributes redirectAttributes) {
        try {
            adminCatalogService.deleteProductImage(imageId);
            redirectAttributes.addFlashAttribute("adminMessage", "Product image deleted successfully.");
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("adminError", exception.getMessage());
        }
        return "redirect:/admin/dashboard#catalog";
    }

    @PostMapping("/admin/users/create")
    public String createUser(@RequestParam String fullName,
                             @RequestParam String email,
                             @RequestParam String phone,
                             @RequestParam String password,
                             @RequestParam(defaultValue = "CUSTOMER") String role,
                             @RequestParam(defaultValue = "false") boolean active,
                             RedirectAttributes redirectAttributes) {
        try {
            adminCatalogService.createUser(fullName, email, phone, password, role, active);
            redirectAttributes.addFlashAttribute("adminMessage", "User account created successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("adminError", exception.getMessage());
            redirectAttributes.addFlashAttribute("adminErrorModal", "#addUserModal");
        }
        return "redirect:/admin/dashboard#users";
    }

    @PostMapping("/admin/users/{id}/edit")
    public String updateUser(@PathVariable Long id,
                             @RequestParam String fullName,
                             @RequestParam String email,
                             @RequestParam String phone,
                             @RequestParam(required = false) String newPassword,
                             @RequestParam(defaultValue = "CUSTOMER") String role,
                             @RequestParam(defaultValue = "false") boolean active,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            adminCatalogService.updateUser(id, fullName, email, phone, newPassword, role, active, authentication.getName());
            redirectAttributes.addFlashAttribute("adminMessage", "User account updated successfully.");
        } catch (IllegalArgumentException exception) {
            redirectAttributes.addFlashAttribute("adminError", exception.getMessage());
            redirectAttributes.addFlashAttribute("adminErrorModal", "#editUserModal");
        }
        return "redirect:/admin/dashboard#users";
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

    @PostMapping("/admin/users/{id}/delete")
    public String deleteUser(@PathVariable Long id,
                             Authentication authentication,
                             RedirectAttributes redirectAttributes) {
        try {
            adminCatalogService.deleteUser(id, authentication.getName());
            redirectAttributes.addFlashAttribute("adminMessage", "User account deleted successfully.");
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

    @GetMapping("/admin/orders/{orderCode}")
    public String orderDetail(@PathVariable String orderCode,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        var order = orderService.findOrderByCode(orderCode);
        if (order.isEmpty()) {
            redirectAttributes.addFlashAttribute("adminError", "Order not found.");
            return "redirect:/admin/dashboard#orders";
        }
        model.addAttribute("order", order.get());
        return "admin/order-detail";
    }
}
