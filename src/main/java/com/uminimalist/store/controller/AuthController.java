package com.uminimalist.store.controller;

import com.uminimalist.store.entity.User;
import com.uminimalist.store.model.UserRegistrationDto;
import com.uminimalist.store.repository.UserRepository;
import com.uminimalist.store.service.ShoppingCartService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ShoppingCartService shoppingCartService;

    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          ShoppingCartService shoppingCartService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.shoppingCartService = shoppingCartService;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "registered", required = false) String registered,
                        @RequestParam(value = "expired", required = false) String expired,
                        @RequestParam(value = "session_limit", required = false) String sessionLimit,
                        Authentication authentication,
                        Model model) {
        if (logout == null && expired == null && sessionLimit == null
                && authentication != null && authentication.isAuthenticated()
                && !(authentication instanceof org.springframework.security.authentication.AnonymousAuthenticationToken)) {
            boolean isAdmin = authentication.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
            return isAdmin ? "redirect:/admin/dashboard" : "redirect:/";
        }

        if (error != null) {
            model.addAttribute("errorMessage", "Invalid email or password.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully.");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "Account created successfully! Please sign in.");
        }
        if (expired != null) {
            model.addAttribute("errorMessage", "Your session has expired because your account was logged in on another device.");
        }
        if (sessionLimit != null) {
            model.addAttribute("errorMessage", "This account is currently logged in on another browser or device. Concurrent login is not allowed.");
        }
        return "login";
    }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        return "register";
    }

    @PostMapping("/register")
    public String registerSubmit(@Valid @ModelAttribute("user") UserRegistrationDto registrationDto,
                                 BindingResult bindingResult,
                                 HttpSession session,
                                 Model model) {
        normalizeRegistration(registrationDto);

        if (bindingResult.hasErrors()) {
            return "register";
        }
        if (userRepository.existsByEmail(registrationDto.getEmail())) {
            model.addAttribute("errorMessage", "Email is already in use.");
            model.addAttribute("user", registrationDto);
            return "register";
        }
        if (!registrationDto.getPassword().equals(registrationDto.getConfirmPassword())) {
            model.addAttribute("errorMessage", "Passwords do not match.");
            model.addAttribute("user", registrationDto);
            return "register";
        }

        User user = new User(
            registrationDto.getEmail(),
            passwordEncoder.encode(registrationDto.getPassword()),
            registrationDto.getFullName(),
            registrationDto.getPhone(),
            "CUSTOMER"
        );

        userRepository.save(user);
        shoppingCartService.mergeSessionCartToCustomerAfterRegister(session, registrationDto.getEmail());
        return "redirect:/login?registered=true";
    }

    private void normalizeRegistration(UserRegistrationDto registrationDto) {
        registrationDto.setEmail(normalizeLower(registrationDto.getEmail()));
        registrationDto.setFullName(normalizeSpaces(registrationDto.getFullName()));
        registrationDto.setPhone(normalizeSpaces(registrationDto.getPhone()));
    }

    private String normalizeLower(String value) {
        String normalized = normalizeSpaces(value);
        return normalized == null ? null : normalized.toLowerCase();
    }

    private String normalizeSpaces(String value) {
        return value == null ? null : value.trim().replaceAll("\\s+", " ");
    }
}
