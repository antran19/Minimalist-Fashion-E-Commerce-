package com.uminimalist.store.config;

import com.uminimalist.store.entity.User;
import com.uminimalist.store.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) throws Exception {
        // Seed Admin
        if (userRepository.findByEmail("admin@uminimalist.com").isEmpty()) {
            User admin = new User(
                    "admin@uminimalist.com",
                    passwordEncoder.encode("admin123"),
                    "Store Admin",
                    "0123456789",
                    "ADMIN");
            userRepository.save(admin);
        }

        // Seed Customer
        if (userRepository.findByEmail("customer@uminimalist.com").isEmpty()) {
            User customer = new User(
                    "customer@uminimalist.com",
                    passwordEncoder.encode("customer123"),
                    "John Doe",
                    "0987654321",
                    "CUSTOMER");
            userRepository.save(customer);
        }
    }
}
