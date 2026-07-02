package com.uminimalist.store.controller;

import com.uminimalist.store.entity.User;
import com.uminimalist.store.model.UserRegistrationDto;
import com.uminimalist.store.repository.UserRepository;
import jakarta.validation.Valid;
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

    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        @RequestParam(value = "registered", required = false) String registered,
                        Model model) {
        if (error != null) {
            model.addAttribute("errorMessage", "Invalid email or password.");
        }
        if (logout != null) {
            model.addAttribute("successMessage", "You have been logged out successfully.");
        }
        if (registered != null) {
            model.addAttribute("successMessage", "Account created successfully! Please sign in.");
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
