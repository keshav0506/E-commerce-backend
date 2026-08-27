package com.keshav.config;

import com.keshav.entity.User;
import com.keshav.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            // Seed Admin User
            if (!userRepository.existsByEmail("admin@ecommerce.com")) {
                User admin = new User();
                admin.setName("Admin User");
                admin.setEmail("admin@ecommerce.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole("ADMIN");
                userRepository.save(admin);
                System.out.println(">>> Seeded default ADMIN account: admin@ecommerce.com / admin123");
            }

            // Seed Customer User
            if (!userRepository.existsByEmail("user@ecommerce.com")) {
                User customer = new User();
                customer.setName("Customer User");
                customer.setEmail("user@ecommerce.com");
                customer.setPassword(passwordEncoder.encode("user123"));
                customer.setRole("CUSTOMER");
                userRepository.save(customer);
                System.out.println(">>> Seeded default CUSTOMER account: user@ecommerce.com / user123");
            }
        };
    }
}
