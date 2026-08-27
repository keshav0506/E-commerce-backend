package com.keshav.config;

import com.keshav.entity.Category;
import com.keshav.entity.Product;
import com.keshav.entity.User;
import com.keshav.repository.CategoryRepository;
import com.keshav.repository.ProductRepository;
import com.keshav.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(CategoryRepository categoryRepository,
                      ProductRepository productRepository,
                      UserRepository userRepository,
                      PasswordEncoder passwordEncoder) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedIfEmpty();
    }

    public synchronized void seedIfEmpty() {
        seedUsersIfMissing();
        if (categoryRepository.count() == 0 || productRepository.count() == 0) {
            seedAll();
        } else {
            log.info("Database already seeded with {} categories and {} products.",
                    categoryRepository.count(), productRepository.count());
        }
    }

    public synchronized void seedUsersIfMissing() {
        if (!userRepository.existsByEmail("admin@ecommerce.com")) {
            User admin = new User();
            admin.setName("Admin User");
            admin.setEmail("admin@ecommerce.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setRole("ADMIN");
            userRepository.save(admin);
            log.info("Seeded default Admin user: admin@ecommerce.com");
        }

        if (!userRepository.existsByEmail("user@ecommerce.com")) {
            User user = new User();
            user.setName("Customer User");
            user.setEmail("user@ecommerce.com");
            user.setPassword(passwordEncoder.encode("user123"));
            user.setRole("CUSTOMER");
            userRepository.save(user);
            log.info("Seeded default Customer user: user@ecommerce.com");
        }
    }

    public synchronized Map<String, Object> seedAll() {
        log.info("Starting Mock Data Seeding for E-Commerce Database...");
        seedUsersIfMissing();

        // Ensure categories exist
        Map<String, Category> catMap = new HashMap<>();

        List<CategorySeedData> catData = Arrays.asList(
            new CategorySeedData("Beverages", "Refreshing organic juices, cold brew elixirs, and prebiotic drinks.", "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846790/ecommerce/products/foq3pj2h2qmtckbuwu0o.jpg", "ACTIVE"),
            new CategorySeedData("Snacks", "Artisan corn chips, roasted nuts, organic dried fruits, and healthy bites.", "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846783/ecommerce/products/sonwmknronpjyv4qoxdb.jpg", "ACTIVE"),
            new CategorySeedData("Dairy", "Farm-fresh milk, organic Greek yogurt, artisan cheeses, and butter.", "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846339/ecommerce/products/qwdqaxxm1blrcvlfymgc.jpg", "ACTIVE"),
            new CategorySeedData("Personal Care", "Botanical shampoos, body washes, organic lotions, and gentle skincare.", "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846342/ecommerce/products/omgs1uts0ckmlwkewdat.jpg", "ACTIVE"),
            new CategorySeedData("Household", "Plant-derived cleaners, eco surface sprays, and eco-friendly home care.", "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846347/ecommerce/products/k5jjawg3uy4gamhpagie.jpg", "ACTIVE"),
            new CategorySeedData("Accessories", "Studio noise-canceling headphones, smart wearables, and tech accessories.", "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846770/ecommerce/products/yn4qovboszpxtefr7yjo.jpg", "ACTIVE"),
            new CategorySeedData("Clothing", "Sustainable cotton hoodies, minimal streetwear tees, and casual wear.", "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846825/ecommerce/products/hhyogdp8dbpetoqfkiie.jpg", "ACTIVE"),
            new CategorySeedData("Footwear", "Minimalist leather sneakers, ergonomic trainers, and comfortable slides.", "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846776/ecommerce/products/vmw38u1w7d7nbxbmer9m.jpg", "ACTIVE"),
            new CategorySeedData("Electronics", "Smart watches, noise cancelling headphones, speakers, and gadgets.", "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846812/ecommerce/products/mcbgbgucqnd293rjid65.jpg", "ACTIVE")
        );

        for (CategorySeedData c : catData) {
            Category cat = categoryRepository.findAll().stream()
                    .filter(existing -> existing.getName().equalsIgnoreCase(c.name))
                    .findFirst()
                    .orElseGet(() -> {
                        Category newCat = new Category();
                        newCat.setName(c.name);
                        newCat.setDescription(c.description);
                        newCat.setImage(c.image);
                        newCat.setStatus(c.status);
                        return categoryRepository.save(newCat);
                    });
            catMap.put(c.name.toLowerCase(), cat);
        }

        // Product seed data
        List<ProductSeedData> prodData = Arrays.asList(
            // Electronics & Accessories
            new ProductSeedData("Apex Pro OLED Smartwatch", "Ultra-bright OLED smartwatch with fitness tracking, heart rate monitoring, and 7-day battery life.", 3499.0, 50, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846812/ecommerce/products/mcbgbgucqnd293rjid65.jpg", "ACTIVE", "electronics"),
            new ProductSeedData("Aethelgard Studio Headphones", "Over-ear wireless headphones featuring hybrid active noise cancellation and plush memory foam earcups.", 2999.0, 40, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846770/ecommerce/products/yn4qovboszpxtefr7yjo.jpg", "ACTIVE", "accessories"),
            new ProductSeedData("True Wireless Earbuds Pro", "Ultra HD sound drivers with active noise cancellation and 30-hour playback case.", 2499.0, 60, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846770/ecommerce/products/yn4qovboszpxtefr7yjo.jpg", "ACTIVE", "electronics"),
            new ProductSeedData("Precision Beard & Hair Trimmer", "Self-sharpening titanium blades with 20 length settings and fast USB-C charging.", 1299.0, 35, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846350/ecommerce/products/wjcpgszt9ygl61qcag7s.jpg", "ACTIVE", "electronics"),
            new ProductSeedData("Stainless Steel Chrono Watch", "Water resistant silver stainless steel chronograph wrist watch with Japanese quartz movement.", 2199.0, 25, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846344/ecommerce/products/quvwpulztuthhxzw6etw.jpg", "ACTIVE", "accessories"),

            // Clothing & Footwear
            new ProductSeedData("Apex White Leather Sneaker", "Full-grain white calfskin leather upper sneakers with lightweight durable rubber soles.", 1899.0, 45, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846776/ecommerce/products/vmw38u1w7d7nbxbmer9m.jpg", "ACTIVE", "footwear"),
            new ProductSeedData("Royal Blue Terravibe Hoodie", "450gsm heavyweight organic French terry cotton hoodie with double-lined hood.", 1299.0, 50, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846825/ecommerce/products/hhyogdp8dbpetoqfkiie.jpg", "ACTIVE", "clothing"),
            new ProductSeedData("Traditional Cotton Festive Kurta", "Breathable handwoven pure cotton mandarin collar ethnic kurta perfect for Rakhi & celebrations.", 1199.0, 30, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846348/ecommerce/products/ai2oqvvi0ly2rguzvo1l.jpg", "ACTIVE", "clothing"),
            new ProductSeedData("Quick-Dry Athletic Activewear T-Shirt", "Lightweight stretch moisture-wicking training crewneck for workouts and sports.", 799.0, 70, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846349/ecommerce/products/tc7exznhsshk3tpdjzyp.jpg", "ACTIVE", "clothing"),
            new ProductSeedData("Oversized Streetwear Graphic Tee", "Relaxed drop-shoulder heavyweight organic cotton graphic streetwear t-shirt.", 499.0, 80, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846345/ecommerce/products/lkf9m7irrwycezuvypa2.jpg", "ACTIVE", "clothing"),

            // Snacks & Festive
            new ProductSeedData("Artisan Festive Sweet & Nut Hamper", "Premium Rakshabandhan & festive gift box containing roasted nuts, dates, and sweets.", 699.0, 40, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846338/ecommerce/products/elq6vcvrofjtvcff01gu.jpg", "ACTIVE", "snacks"),
            new ProductSeedData("Organic Dried Figs & Dates", "Sun-dried natural Mediterranean figs and Medjool dates with zero added sugar.", 349.0, 60, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846352/ecommerce/products/ap3odxzsbeeul5vk0fdr.jpg", "ACTIVE", "snacks"),
            new ProductSeedData("Chipotle Lime Nachos", "Gluten-free organic stone-ground corn chips seasoned with smoked chipotle & tangy lime.", 99.0, 100, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846783/ecommerce/products/sonwmknronpjyv4qoxdb.jpg", "ACTIVE", "snacks"),
            new ProductSeedData("Roasted Salted Almonds 200g", "Slow roasted California almonds lightly dusted with Himalayan pink salt.", 299.0, 75, "https://images.unsplash.com/photo-1508061252425-f3832161b462?auto=format&fit=crop&w=600&q=80", "ACTIVE", "snacks"),

            // Beverages
            new ProductSeedData("Berry Blast Juice", "Cold-pressed prebiotic & probiotic juice drink infused with wild organic berries and organic lemon.", 199.0, 90, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846790/ecommerce/products/foq3pj2h2qmtckbuwu0o.jpg", "ACTIVE", "beverages"),
            new ProductSeedData("Zest Immunity Booster", "Raw organic Valencia orange juice blended with organic turmeric root, black pepper, and ginger.", 189.0, 85, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846773/ecommerce/products/f9dankyiaztin6lfhl0q.jpg", "ACTIVE", "beverages"),
            new ProductSeedData("Organic Cold Brew Coffee", "Steeped for 18 hours using 100% Arabica organic single-origin beans.", 249.0, 65, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846346/ecommerce/products/cbync2cj7nl96ci2r1er.jpg", "ACTIVE", "beverages"),

            // Personal Care, Household, Dairy
            new ProductSeedData("Organic Botanical Body Lotion", "Deep hydration body moisturizer enriched with cold-pressed jojoba oil and shea butter.", 349.0, 50, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846343/ecommerce/products/x75tuhyehqywxjjyxjxc.jpg", "ACTIVE", "personal care"),
            new ProductSeedData("Organic Lavender Surface Spray", "Non-toxic multi-surface cleaner made with natural essential oils and antibacterial plant agents.", 249.0, 40, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846342/ecommerce/products/ngtg0d35hoziqkth5ycv.jpg", "ACTIVE", "household"),
            new ProductSeedData("Plush Giant Bear Companion", "Ultra-soft premium plush teddy bear toy gift with velvety fur and embroidered paws.", 899.0, 30, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846347/ecommerce/products/haozaebqas8wxykrvjqm.jpg", "ACTIVE", "accessories"),
            new ProductSeedData("Farm Fresh Whole Milk 1L", "Grass-fed pasture raised organic whole milk with natural cream layer.", 89.0, 120, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846341/ecommerce/products/mobflyhllbqeejcagegn.jpg", "ACTIVE", "dairy")
        );

        for (ProductSeedData p : prodData) {
            boolean exists = productRepository.findAll().stream()
                    .anyMatch(existing -> existing.getName().equalsIgnoreCase(p.name));
            if (!exists) {
                Category cat = catMap.get(p.categoryKey.toLowerCase());
                if (cat != null) {
                    Product prod = new Product();
                    prod.setName(p.name);
                    prod.setDescription(p.description);
                    prod.setPrice(p.price);
                    prod.setStock(p.stock);
                    prod.setImage(p.image);
                    prod.setStatus(p.status);
                    prod.setCategory(cat);
                    productRepository.save(prod);
                }
            }
        }

        long catCount = categoryRepository.count();
        long prodCount = productRepository.count();
        log.info("Mock Data Seeding Complete! Total Categories: {}, Total Products: {}", catCount, prodCount);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Database successfully seeded with mock data!");
        response.put("categoriesCount", catCount);
        response.put("productsCount", prodCount);
        return response;
    }

    private static class CategorySeedData {
        String name;
        String description;
        String image;
        String status;

        public CategorySeedData(String name, String description, String image, String status) {
            this.name = name;
            this.description = description;
            this.image = image;
            this.status = status;
        }
    }

    private static class ProductSeedData {
        String name;
        String description;
        double price;
        int stock;
        String image;
        String status;
        String categoryKey;

        public ProductSeedData(String name, String description, double price, int stock, String image, String status, String categoryKey) {
            this.name = name;
            this.description = description;
            this.price = price;
            this.stock = stock;
            this.image = image;
            this.status = status;
            this.categoryKey = categoryKey;
        }
    }
}
