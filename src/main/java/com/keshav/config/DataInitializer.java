package com.keshav.config;

import com.keshav.entity.*;
import com.keshav.repository.PurchaseOrderRepository;
import com.keshav.repository.SupplierProfileRepository;
import com.keshav.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initData(
            UserRepository userRepository,
            SupplierProfileRepository supplierProfileRepository,
            PurchaseOrderRepository purchaseOrderRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            // Seed Admin User
            if (!userRepository.existsByEmail("admin@ecommerce.com")) {
                User admin = new User();
                admin.setName("Admin User");
                admin.setEmail("admin@ecommerce.com");
                admin.setPassword(passwordEncoder.encode("admin123"));
                admin.setRole(Role.ADMIN);
                admin.setEnabled(true);
                userRepository.save(admin);
                System.out.println(">>> Seeded default ADMIN account: admin@ecommerce.com / admin123");
            }

            // Seed Customer User
            if (!userRepository.existsByEmail("user@ecommerce.com")) {
                User customer = new User();
                customer.setName("Customer User");
                customer.setEmail("user@ecommerce.com");
                customer.setPassword(passwordEncoder.encode("user123"));
                customer.setRole(Role.CUSTOMER);
                customer.setEnabled(true);
                userRepository.save(customer);
                System.out.println(">>> Seeded default CUSTOMER account: user@ecommerce.com / user123");
            }

            // Seed Demo Approved Supplier
            if (!userRepository.existsByEmail("supplier@ecommerce.com")) {
                User supplierUser = new User();
                supplierUser.setName("Rajesh Sharma");
                supplierUser.setEmail("supplier@ecommerce.com");
                supplierUser.setPassword(passwordEncoder.encode("supplier123"));
                supplierUser.setRole(Role.SUPPLIER);
                supplierUser.setEnabled(true);
                User savedSupplierUser = userRepository.save(supplierUser);

                SupplierProfile profile = new SupplierProfile();
                profile.setUser(savedSupplierUser);
                profile.setBusinessName("Apex Wholesale Logistics Pvt Ltd");
                profile.setBusinessEmail("contact@apexlogistics.in");
                profile.setPhone("9876543210");
                profile.setBusinessAddress("Plot 42, Okhla Industrial Area Phase III");
                profile.setCity("New Delhi");
                profile.setState("Delhi");
                profile.setPostalCode("110020");
                profile.setCountry("India");
                profile.setTaxIdentifier("07AAAAA0000A1Z5");
                profile.setCategory("Beverages & FMCG");
                profile.setStatus(SupplierStatus.APPROVED);
                SupplierProfile savedProfile = supplierProfileRepository.save(profile);

                // Seed sample initial Purchase Order
                if (purchaseOrderRepository.countBySupplier(savedProfile) == 0) {
                    PurchaseOrder po = new PurchaseOrder();
                    po.setPoNumber("PO-2026-00101");
                    po.setSupplier(savedProfile);
                    po.setStatus(PurchaseOrderStatus.PENDING);
                    po.setOrderDate(LocalDateTime.now().minusDays(1));
                    po.setExpectedDeliveryDate(LocalDateTime.now().plusDays(5));
                    po.setTotalAmount(BigDecimal.valueOf(45200.00));
                    po.setSupplierNotes("Priority procurement for seasonal inventory restocking.");
                    purchaseOrderRepository.save(po);
                }

                System.out.println(">>> Seeded default APPROVED SUPPLIER: supplier@ecommerce.com / supplier123");
            }
        };
    }
}
