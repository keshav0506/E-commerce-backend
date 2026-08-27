package com.keshav.config;

import com.keshav.entity.Category;
import com.keshav.entity.Product;
import com.keshav.entity.Review;
import com.keshav.entity.User;
import com.keshav.repository.CategoryRepository;
import com.keshav.repository.ProductRepository;
import com.keshav.repository.ReviewRepository;
import com.keshav.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
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
    private final ReviewRepository reviewRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(CategoryRepository categoryRepository,
                      ProductRepository productRepository,
                      UserRepository userRepository,
                      ReviewRepository reviewRepository,
                      PasswordEncoder passwordEncoder) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedIfEmpty();
    }

    public synchronized void seedIfEmpty() {
        seedUsersIfMissing();
        if (categoryRepository.count() < 9 || productRepository.count() < 80) {
            seedAll();
        } else {
            log.info("Database already seeded with {} categories and {} products.",
                    categoryRepository.count(), productRepository.count());
        }
        seedSampleReviewsIfMissing();
    }

    public synchronized void seedSampleReviewsIfMissing() {
        if (reviewRepository.count() == 0 && productRepository.count() > 0 && userRepository.count() > 0) {
            User user = userRepository.findByEmail("user@ecommerce.com").orElse(null);
            if (user == null) return;

            List<Product> prods = productRepository.findAll();
            if (prods.isEmpty()) return;

            Product p1 = prods.get(0);
            Review r1 = new Review(null, p1, user, 5, "Absolutely Stunning Quality!",
                    "The build quality and screen are mind-blowing. Battery lasts for days easily. Highly recommended!",
                    true, LocalDateTime.now().minusDays(2), LocalDateTime.now().minusDays(2));
            reviewRepository.save(r1);

            if (prods.size() > 1) {
                Product p2 = prods.get(1);
                Review r2 = new Review(null, p2, user, 5, "Pure Bliss & Crystal Clear Sound",
                        "Active noise cancellation is top notch. Super comfortable memory foam earcups!",
                        true, LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(1));
                reviewRepository.save(r2);
            }
            log.info("Seeded initial verified customer reviews.");
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
        log.info("Starting Full Production Catalog Seeding (10+ items per category)...");
        seedUsersIfMissing();

        // 1. Categories Mapping
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

        // 2. Comprehensive 10+ Products per Category Catalog
        List<ProductSeedData> prodData = Arrays.asList(
            // ==========================================
            // 1. ELECTRONICS (10 Products)
            // ==========================================
            new ProductSeedData("Apex Pro OLED Smartwatch", "Ultra-bright OLED smartwatch with fitness tracking, heart rate monitoring, and 7-day battery life.", 3499.0, 50, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846812/ecommerce/products/mcbgbgucqnd293rjid65.jpg", "ACTIVE", "electronics"),
            new ProductSeedData("True Wireless Earbuds Pro", "Ultra HD sound drivers with active noise cancellation and 30-hour playback case.", 2499.0, 60, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846770/ecommerce/products/yn4qovboszpxtefr7yjo.jpg", "ACTIVE", "electronics"),
            new ProductSeedData("Precision Titanium Beard Trimmer", "Self-sharpening titanium blades with 20 length settings and fast USB-C charging.", 1299.0, 45, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846350/ecommerce/products/wjcpgszt9ygl61qcag7s.jpg", "ACTIVE", "electronics"),
            new ProductSeedData("Portable 20W Bass Bluetooth Speaker", "Rugged IPX7 waterproof stereo speaker with deep punchy bass and 12-hour party playtime.", 1899.0, 40, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846812/ecommerce/products/mcbgbgucqnd293rjid65.jpg", "ACTIVE", "electronics"),
            new ProductSeedData("Magnetic Fast Wireless Power Bank 10000mAh", "Slim 20W PD fast-charging portable charger compatible with all MagSafe and Qi devices.", 1499.0, 55, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846770/ecommerce/products/yn4qovboszpxtefr7yjo.jpg", "ACTIVE", "electronics"),
            new ProductSeedData("65W GaN Multi-Port USB-C Rapid Charger", "Ultra-compact Gallium Nitride wall adapter with 2x Type-C and 1x USB-A ports.", 1699.0, 60, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846812/ecommerce/products/mcbgbgucqnd293rjid65.jpg", "ACTIVE", "electronics"),
            new ProductSeedData("RGB Backlit Mechanical Keyboard", "Hot-swappable tactile red mechanical switches with custom dynamic RGB backlighting.", 2299.0, 30, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846812/ecommerce/products/mcbgbgucqnd293rjid65.jpg", "ACTIVE", "electronics"),
            new ProductSeedData("Ergonomic Silent Wireless Mouse", "High-precision 4000 DPI multi-device bluetooth optical mouse with whisper-quiet clicks.", 799.0, 70, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846350/ecommerce/products/wjcpgszt9ygl61qcag7s.jpg", "ACTIVE", "electronics"),
            new ProductSeedData("1080P Full HD Auto-Focus Webcam", "Crystal-clear wide-angle conference webcam with dual noise-reduction microphones and privacy shutter.", 1599.0, 35, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846812/ecommerce/products/mcbgbgucqnd293rjid65.jpg", "ACTIVE", "electronics"),
            new ProductSeedData("Smart Ambient RGB WiFi Light Strip 5M", "App-controlled voice-synced smart LED strip light with 16 million colors and music sync.", 999.0, 80, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846770/ecommerce/products/yn4qovboszpxtefr7yjo.jpg", "ACTIVE", "electronics"),

            // ==========================================
            // 2. ACCESSORIES (10 Products)
            // ==========================================
            new ProductSeedData("Aethelgard Studio ANC Headphones", "Over-ear wireless studio headphones featuring hybrid noise cancellation and memory foam earcups.", 2999.0, 40, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846770/ecommerce/products/yn4qovboszpxtefr7yjo.jpg", "ACTIVE", "accessories"),
            new ProductSeedData("Stainless Steel Chrono Watch", "Water resistant silver stainless steel chronograph wrist watch with Japanese quartz movement.", 2199.0, 35, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846344/ecommerce/products/quvwpulztuthhxzw6etw.jpg", "ACTIVE", "accessories"),
            new ProductSeedData("Plush Giant Velvet Companion Bear", "Ultra-soft premium plush teddy bear companion gift with velvety fur and embroidered paws.", 899.0, 30, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846347/ecommerce/products/haozaebqas8wxykrvjqm.jpg", "ACTIVE", "accessories"),
            new ProductSeedData("Anti-Theft Crossbody Sling Bag", "Water-resistant single strap shoulder pack with hidden anti-theft zipper and USB charging port.", 1299.0, 45, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846770/ecommerce/products/yn4qovboszpxtefr7yjo.jpg", "ACTIVE", "accessories"),
            new ProductSeedData("Genuine Full-Grain Leather Wallet", "Handcrafted premium bi-fold leather wallet with 8 card slots and RFID protection.", 799.0, 65, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846344/ecommerce/products/quvwpulztuthhxzw6etw.jpg", "ACTIVE", "accessories"),
            new ProductSeedData("Classic Polarized UV400 Sunglasses", "Timeless matte black square frame shades with anti-glare scratch-resistant polarized lenses.", 1199.0, 50, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846770/ecommerce/products/yn4qovboszpxtefr7yjo.jpg", "ACTIVE", "accessories"),
            new ProductSeedData("Water-Resistant Laptop Backpack 15.6", "Ergonomic multi-compartment commuting backpack with padded airflow back panel.", 1699.0, 40, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846344/ecommerce/products/quvwpulztuthhxzw6etw.jpg", "ACTIVE", "accessories"),
            new ProductSeedData("Braided Fast-Charging USB-C Cable 2M", "Heavy-duty military-grade nylon braided 100W PD fast charge cable with reinforced connectors.", 399.0, 100, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846770/ecommerce/products/yn4qovboszpxtefr7yjo.jpg", "ACTIVE", "accessories"),
            new ProductSeedData("Adjustable Aluminum Laptop Stand", "Ergonomic foldable desktop riser made from aircraft-grade aluminum with silicone anti-slip pads.", 899.0, 45, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846812/ecommerce/products/mcbgbgucqnd293rjid65.jpg", "ACTIVE", "accessories"),
            new ProductSeedData("Minimalist RFID Cardholder Clip", "Ultra-slim carbon fiber textured front pocket money clip wallet for up to 12 cards.", 499.0, 80, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846344/ecommerce/products/quvwpulztuthhxzw6etw.jpg", "ACTIVE", "accessories"),

            // ==========================================
            // 3. CLOTHING (10 Products)
            // ==========================================
            new ProductSeedData("Royal Blue Terravibe Hoodie", "450gsm heavyweight organic French terry cotton hoodie with double-lined hood.", 1299.0, 50, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846825/ecommerce/products/hhyogdp8dbpetoqfkiie.jpg", "ACTIVE", "clothing"),
            new ProductSeedData("Traditional Cotton Festive Kurta", "Breathable handwoven pure cotton mandarin collar ethnic kurta perfect for celebrations.", 1199.0, 30, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846348/ecommerce/products/ai2oqvvi0ly2rguzvo1l.jpg", "ACTIVE", "clothing"),
            new ProductSeedData("Quick-Dry Athletic Activewear Tee", "Lightweight stretch moisture-wicking training crewneck for workouts and sports.", 799.0, 70, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846349/ecommerce/products/tc7exznhsshk3tpdjzyp.jpg", "ACTIVE", "clothing"),
            new ProductSeedData("Oversized Streetwear Graphic Tee", "Relaxed drop-shoulder heavyweight organic cotton graphic streetwear t-shirt.", 499.0, 80, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846345/ecommerce/products/lkf9m7irrwycezuvypa2.jpg", "ACTIVE", "clothing"),
            new ProductSeedData("100% Bio-Washed Classic Cotton Tee", "Super-soft 180 GSM ring-spun combed cotton everyday crewneck t-shirt.", 399.0, 90, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846825/ecommerce/products/hhyogdp8dbpetoqfkiie.jpg", "ACTIVE", "clothing"),
            new ProductSeedData("Pure Linen Casual Button-Down Shirt", "Breathable natural European linen regular-fit long sleeve casual shirt.", 1499.0, 35, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846348/ecommerce/products/ai2oqvvi0ly2rguzvo1l.jpg", "ACTIVE", "clothing"),
            new ProductSeedData("Slim-Fit Stretch Denim Jeans", "Comfort stretch premium indigo dyed cotton denim jeans with durable rivets.", 1799.0, 45, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846825/ecommerce/products/hhyogdp8dbpetoqfkiie.jpg", "ACTIVE", "clothing"),
            new ProductSeedData("Pure Cotton Printed Boxers (Pack of 3)", "Soft breathable button-fly casual everyday boxer shorts in vibrant assorted patterns.", 699.0, 60, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846356/ecommerce/products/qj7x253w0g52f78bdfj0.jpg", "ACTIVE", "clothing"),
            new ProductSeedData("Relaxed Fleece Jogger Sweatpants", "Ultra-cozy brushed fleece sweatpants with deep zipper pockets and elastic ankle cuffs.", 999.0, 50, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846349/ecommerce/products/tc7exznhsshk3tpdjzyp.jpg", "ACTIVE", "clothing"),
            new ProductSeedData("Formal Solid White Cotton Shirt", "Impeccably tailored wrinkle-resistant 100% Egyptian cotton business dress shirt.", 1249.0, 40, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846348/ecommerce/products/ai2oqvvi0ly2rguzvo1l.jpg", "ACTIVE", "clothing"),

            // ==========================================
            // 4. FOOTWEAR (10 Products)
            // ==========================================
            new ProductSeedData("Apex White Leather Sneaker", "Full-grain white calfskin leather upper sneakers with lightweight durable rubber soles.", 1899.0, 45, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846776/ecommerce/products/vmw38u1w7d7nbxbmer9m.jpg", "ACTIVE", "footwear"),
            new ProductSeedData("Urban Runner Active Mesh Trainers", "Responsive cushioned midsole training running shoes with breathable knit upper.", 2199.0, 50, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846776/ecommerce/products/vmw38u1w7d7nbxbmer9m.jpg", "ACTIVE", "footwear"),
            new ProductSeedData("Handcrafted Leather Oxford Formal Shoes", "Classic genuine leather lace-up formal dress shoes with padded orthopedic footbed.", 2899.0, 30, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846776/ecommerce/products/vmw38u1w7d7nbxbmer9m.jpg", "ACTIVE", "footwear"),
            new ProductSeedData("Memory Foam Daily Recovery Slides", "Waterproof lightweight EVA recovery slide sandals with textured arch-support footbed.", 699.0, 80, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846776/ecommerce/products/vmw38u1w7d7nbxbmer9m.jpg", "ACTIVE", "footwear"),
            new ProductSeedData("Classic Canvas Low-Top Skate Shoes", "Durable heavy canvas vulcanized rubber skate sneakers for everyday street style.", 1299.0, 55, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846776/ecommerce/products/vmw38u1w7d7nbxbmer9m.jpg", "ACTIVE", "footwear"),
            new ProductSeedData("Waterproof Trail Hiking Boots", "High-traction all-terrain ankle hiking boots with waterproof breathable membrane.", 3499.0, 25, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846776/ecommerce/products/vmw38u1w7d7nbxbmer9m.jpg", "ACTIVE", "footwear"),
            new ProductSeedData("Vegan Leather Casual Slip-On Loafers", "Flexible lightweight driving loafers with anti-skid rubber pebble sole.", 1699.0, 40, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846776/ecommerce/products/vmw38u1w7d7nbxbmer9m.jpg", "ACTIVE", "footwear"),
            new ProductSeedData("Ultra-Lightweight Marathon Shoes", "Aerodynamic race-day running shoes with energy-returning carbon fiber plate technology.", 2599.0, 35, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846776/ecommerce/products/vmw38u1w7d7nbxbmer9m.jpg", "ACTIVE", "footwear"),
            new ProductSeedData("Breathable Mesh Walking Shoes", "Feather-light slip-on walking shoes with shock-absorbing honeycomb air cushion.", 1399.0, 60, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846776/ecommerce/products/vmw38u1w7d7nbxbmer9m.jpg", "ACTIVE", "footwear"),
            new ProductSeedData("Suede Leather Chelsea Ankle Boots", "Timeless elastic side-gusset Chelsea boots crafted from velvety split suede leather.", 3299.0, 25, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846776/ecommerce/products/vmw38u1w7d7nbxbmer9m.jpg", "ACTIVE", "footwear"),

            // ==========================================
            // 5. BEVERAGES (10 Products)
            // ==========================================
            new ProductSeedData("Berry Blast Prebiotic Juice 300ml", "Cold-pressed prebiotic & probiotic juice drink infused with wild organic berries and lemon.", 199.0, 90, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846790/ecommerce/products/foq3pj2h2qmtckbuwu0o.jpg", "ACTIVE", "beverages"),
            new ProductSeedData("Zest Immunity Booster Juice 300ml", "Raw organic Valencia orange juice blended with turmeric root, black pepper, and ginger.", 189.0, 85, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846773/ecommerce/products/f9dankyiaztin6lfhl0q.jpg", "ACTIVE", "beverages"),
            new ProductSeedData("Organic Cold Brew Coffee 250ml", "Steeped for 18 hours using 100% Arabica organic single-origin roasted beans.", 249.0, 65, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846346/ecommerce/products/cbync2cj7nl96ci2r1er.jpg", "ACTIVE", "beverages"),
            new ProductSeedData("Sparkling Lemon Ginger Kombucha 330ml", "Naturally fermented probiotic sparkling green tea brewed with cold-pressed ginger and lemon.", 159.0, 70, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846790/ecommerce/products/foq3pj2h2qmtckbuwu0o.jpg", "ACTIVE", "beverages"),
            new ProductSeedData("Alphonso Mango Pure Fruit Nectar 1L", "Sun-ripened Ratnagiri Alphonso mango pulp crafted with zero concentrates or preservatives.", 140.0, 100, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846773/ecommerce/products/f9dankyiaztin6lfhl0q.jpg", "ACTIVE", "beverages"),
            new ProductSeedData("Wild Forest Honey Infused Green Tea", "Whole leaf antioxidant-rich Himalayan green tea sweetened with pure raw forest honey.", 299.0, 50, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846790/ecommerce/products/foq3pj2h2qmtckbuwu0o.jpg", "ACTIVE", "beverages"),
            new ProductSeedData("Artisan Arabica Whole Coffee Beans 250g", "Medium dark roasted specialty grade Arabica beans with notes of dark cocoa and caramel.", 499.0, 45, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846346/ecommerce/products/cbync2cj7nl96ci2r1er.jpg", "ACTIVE", "beverages"),
            new ProductSeedData("Prebiotic Apple Cider Vinegar Tonic 500ml", "Raw unfiltered apple cider vinegar with mother, infused with honey and cinnamon.", 169.0, 60, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846790/ecommerce/products/foq3pj2h2qmtckbuwu0o.jpg", "ACTIVE", "beverages"),
            new ProductSeedData("Cold-Pressed Tender Coconut Water 500ml", "100% natural electrolyte-rich pure coconut water packed at origin with zero additives.", 99.0, 120, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846773/ecommerce/products/f9dankyiaztin6lfhl0q.jpg", "ACTIVE", "beverages"),
            new ProductSeedData("Darjeeling First Flush Black Tea Tin 100g", "Rare delicate single-estate loose leaf black tea celebrated as the champagne of teas.", 389.0, 40, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846790/ecommerce/products/foq3pj2h2qmtckbuwu0o.jpg", "ACTIVE", "beverages"),

            // ==========================================
            // 6. SNACKS (10 Products)
            // ==========================================
            new ProductSeedData("Chipotle Lime Nachos 150g", "Gluten-free organic stone-ground corn chips seasoned with smoked chipotle & tangy lime.", 99.0, 100, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846783/ecommerce/products/sonwmknronpjyv4qoxdb.jpg", "ACTIVE", "snacks"),
            new ProductSeedData("Artisan Festive Sweet & Nut Hamper", "Premium festive gift box containing roasted almonds, cashews, dates, and sweets.", 699.0, 40, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846338/ecommerce/products/elq6vcvrofjtvcff01gu.jpg", "ACTIVE", "snacks"),
            new ProductSeedData("Organic Dried Figs & Dates 250g", "Sun-dried natural Mediterranean figs and Medjool dates with zero added sugar.", 349.0, 60, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846352/ecommerce/products/ap3odxzsbeeul5vk0fdr.jpg", "ACTIVE", "snacks"),
            new ProductSeedData("Roasted Himalayan Salt Almonds 200g", "Slow roasted California almonds lightly dusted with pure Himalayan pink mineral salt.", 299.0, 75, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846783/ecommerce/products/sonwmknronpjyv4qoxdb.jpg", "ACTIVE", "snacks"),
            new ProductSeedData("Jumbo Roasted Cashew Nuts 200g", "Buttery crunchy Grade W240 giant cashews dry-roasted to golden perfection.", 349.0, 70, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846352/ecommerce/products/ap3odxzsbeeul5vk0fdr.jpg", "ACTIVE", "snacks"),
            new ProductSeedData("Cheese & Jalapeno Multi-Grain Crisps", "Non-fried popped multi-grain chips seasoned with aged white cheddar and mild jalapeno.", 85.0, 90, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846783/ecommerce/products/sonwmknronpjyv4qoxdb.jpg", "ACTIVE", "snacks"),
            new ProductSeedData("Dark Chocolate Hazelnut Granola 350g", "Crunchy rolled oats clusters tossed with roasted hazelnuts and 70% dark Belgian cocoa.", 399.0, 50, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846338/ecommerce/products/elq6vcvrofjtvcff01gu.jpg", "ACTIVE", "snacks"),
            new ProductSeedData("Peri Peri Roasted Foxnuts (Makhana) 100g", "Puffed lotus seed superfood snack roasted in olive oil with zesty peri-peri herbs.", 149.0, 80, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846783/ecommerce/products/sonwmknronpjyv4qoxdb.jpg", "ACTIVE", "snacks"),
            new ProductSeedData("Oatmeal & Cranberry Artisan Cookies 200g", "Handmade butter cookies packed with rolled whole oats and tart antioxidant cranberries.", 180.0, 65, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846338/ecommerce/products/elq6vcvrofjtvcff01gu.jpg", "ACTIVE", "snacks"),
            new ProductSeedData("Belgian 70% Dark Chocolate Bar 100g", "Silky single-origin dark chocolate bar sprinkled with hand-harvested sea salt flakes.", 220.0, 85, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846352/ecommerce/products/ap3odxzsbeeul5vk0fdr.jpg", "ACTIVE", "snacks"),

            // ==========================================
            // 7. DAIRY (10 Products)
            // ==========================================
            new ProductSeedData("Farm Fresh Whole Milk 1L", "Grass-fed pasture raised organic whole milk with natural cream layer.", 89.0, 120, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846341/ecommerce/products/mobflyhllbqeejcagegn.jpg", "ACTIVE", "dairy"),
            new ProductSeedData("Organic Greek Style Thick Yogurt 400g", "High-protein strained authentic Greek yogurt made with live probiotic cultures.", 149.0, 60, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846339/ecommerce/products/qwdqaxxm1blrcvlfymgc.jpg", "ACTIVE", "dairy"),
            new ProductSeedData("Pure Bilona Cow Ghee 500ml", "Traditional hand-churned Vedic A2 cow ghee rich in natural aroma and golden granules.", 649.0, 45, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846341/ecommerce/products/mobflyhllbqeejcagegn.jpg", "ACTIVE", "dairy"),
            new ProductSeedData("Artisan Garlic & Herb Cream Cheese 200g", "Rich creamy spreadable cheese whipped with roasted garlic and fresh garden herbs.", 199.0, 50, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846339/ecommerce/products/qwdqaxxm1blrcvlfymgc.jpg", "ACTIVE", "dairy"),
            new ProductSeedData("Pasteurized Table Butter 500g", "Rich golden cream table butter churned fresh daily from pure cow's milk.", 275.0, 70, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846341/ecommerce/products/mobflyhllbqeejcagegn.jpg", "ACTIVE", "dairy"),
            new ProductSeedData("Probiotic Strawberry Drinking Kefir 300ml", "Fermented cultured milk smoothie bursting with natural strawberry puree.", 129.0, 60, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846339/ecommerce/products/qwdqaxxm1blrcvlfymgc.jpg", "ACTIVE", "dairy"),
            new ProductSeedData("Fresh Vacuum-Packed Malai Paneer 200g", "Melt-in-mouth ultra-soft cottage cheese made from fresh full cream whole milk.", 110.0, 80, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846341/ecommerce/products/mobflyhllbqeejcagegn.jpg", "ACTIVE", "dairy"),
            new ProductSeedData("Aged Sharp Cheddar Cheese Block 200g", "Naturally aged 12-month sharp cheddar with rich savory crystalline complexity.", 349.0, 40, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846339/ecommerce/products/qwdqaxxm1blrcvlfymgc.jpg", "ACTIVE", "dairy"),
            new ProductSeedData("Unsweetened Almond Milk Barista 1L", "Dairy-free plant milk formulated to steam into silky microfoam for specialty lattes.", 299.0, 55, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846341/ecommerce/products/mobflyhllbqeejcagegn.jpg", "ACTIVE", "dairy"),
            new ProductSeedData("Organic Sweetened Condensed Milk 400g", "Slow-cooked caramel-rich condensed milk perfect for desserts, puddings, and baking.", 160.0, 65, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846339/ecommerce/products/qwdqaxxm1blrcvlfymgc.jpg", "ACTIVE", "dairy"),

            // ==========================================
            // 8. PERSONAL CARE (10 Products)
            // ==========================================
            new ProductSeedData("Organic Botanical Body Lotion 300ml", "Deep hydration body moisturizer enriched with cold-pressed jojoba oil and shea butter.", 349.0, 50, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846343/ecommerce/products/x75tuhyehqywxjjyxjxc.jpg", "ACTIVE", "personal care"),
            new ProductSeedData("Activated Charcoal Face Wash 150ml", "Pollution-defense facial cleanser that draws out pore-clogging impurities and excess oil.", 249.0, 65, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846342/ecommerce/products/omgs1uts0ckmlwkewdat.jpg", "ACTIVE", "personal care"),
            new ProductSeedData("Vitamin C Radiance Face Serum 30ml", "Potent 15% Vitamin C & Ferulic acid antioxidant glow serum for luminous even-toned skin.", 599.0, 45, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846343/ecommerce/products/x75tuhyehqywxjjyxjxc.jpg", "ACTIVE", "personal care"),
            new ProductSeedData("Tea Tree Anti-Dandruff Shampoo 300ml", "Clarifying scalp treatment shampoo infused with Australian tea tree oil and ginger.", 399.0, 55, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846342/ecommerce/products/omgs1uts0ckmlwkewdat.jpg", "ACTIVE", "personal care"),
            new ProductSeedData("Moroccan Argan Oil Hair Mask 200g", "Deep conditioning restorative hair repair mask that tames frizz and restores shine.", 499.0, 40, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846343/ecommerce/products/x75tuhyehqywxjjyxjxc.jpg", "ACTIVE", "personal care"),
            new ProductSeedData("Matte Sunscreen Gel SPF 50 PA++++", "Invisible ultra-lightweight non-greasy sunscreen with broad spectrum blue-light defense.", 450.0, 70, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846342/ecommerce/products/omgs1uts0ckmlwkewdat.jpg", "ACTIVE", "personal care"),
            new ProductSeedData("Shea Butter & Cocoa Lip Butter 15g", "Nourishing natural lip balm that heals chapped lips with pure beeswax and almond oil.", 149.0, 100, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846343/ecommerce/products/x75tuhyehqywxjjyxjxc.jpg", "ACTIVE", "personal care"),
            new ProductSeedData("Exfoliating Arabica Coffee Scrub 200g", "Invigorating body polish made with freshly ground Arabica coffee and virgin coconut oil.", 380.0, 45, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846342/ecommerce/products/omgs1uts0ckmlwkewdat.jpg", "ACTIVE", "personal care"),
            new ProductSeedData("Gentle Foaming Intimate Wash 200ml", "pH-balanced 3.5 hypoallergenic cleansing foam with lactic acid and soothing chamomile.", 299.0, 50, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846343/ecommerce/products/x75tuhyehqywxjjyxjxc.jpg", "ACTIVE", "personal care"),
            new ProductSeedData("Natural Lavender Deodorant Roll-on 50ml", "Aluminum-free 24-hour odor defense deodorant stick infused with Bulgarian lavender.", 275.0, 60, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846342/ecommerce/products/omgs1uts0ckmlwkewdat.jpg", "ACTIVE", "personal care"),

            // ==========================================
            // 9. HOUSEHOLD (10 Products)
            // ==========================================
            new ProductSeedData("Organic Lavender Surface Spray 500ml", "Non-toxic multi-surface cleaner made with natural essential oils and antibacterial plant agents.", 249.0, 40, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846342/ecommerce/products/ngtg0d35hoziqkth5ycv.jpg", "ACTIVE", "household"),
            new ProductSeedData("Plant-Based Laundry Detergent 1L", "Tough on stains yet gentle on fabrics with natural plant enzymes and zero optical brighteners.", 399.0, 55, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846347/ecommerce/products/k5jjawg3uy4gamhpagie.jpg", "ACTIVE", "household"),
            new ProductSeedData("Citrus Bio-Enzyme Dishwashing Gel 750ml", "Grease-cutting natural dish soap infused with sweet orange peel and bio-enzymes.", 199.0, 80, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846342/ecommerce/products/ngtg0d35hoziqkth5ycv.jpg", "ACTIVE", "household"),
            new ProductSeedData("Antibacterial Lemongrass Floor Cleaner 1L", "Streak-free non-toxic floor wash that leaves a fresh uplifting citrus botanical scent.", 220.0, 65, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846347/ecommerce/products/k5jjawg3uy4gamhpagie.jpg", "ACTIVE", "household"),
            new ProductSeedData("Bamboo Kitchen Paper Towels (Pack of 3)", "100% tree-free unbleached super-absorbent washable and reusable bamboo paper towels.", 299.0, 70, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846342/ecommerce/products/ngtg0d35hoziqkth5ycv.jpg", "ACTIVE", "household"),
            new ProductSeedData("French Vanilla Aromatherapy Soy Candle", "Hand-poured 100% natural soy wax candle in amber glass with 45-hour clean burn time.", 449.0, 40, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846347/ecommerce/products/k5jjawg3uy4gamhpagie.jpg", "ACTIVE", "household"),
            new ProductSeedData("Natural Mosquito Repellent Diffuser 100ml", "DEET-free plant-derived citronella and eucalyptus room diffuser for peaceful sleep.", 350.0, 50, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846342/ecommerce/products/ngtg0d35hoziqkth5ycv.jpg", "ACTIVE", "household"),
            new ProductSeedData("Coconut Coir Eco Kitchen Scrubbers (4-Pack)", "100% biodegradable zero-waste dish scrub pads made from natural coconut husk fiber.", 180.0, 90, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846347/ecommerce/products/k5jjawg3uy4gamhpagie.jpg", "ACTIVE", "household"),
            new ProductSeedData("Biodegradable Cornstarch Garbage Bags 30s", "Leakproof heavy-duty compostable dustbin trash liner bags for green home disposal.", 149.0, 100, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846342/ecommerce/products/ngtg0d35hoziqkth5ycv.jpg", "ACTIVE", "household"),
            new ProductSeedData("Botanical Fabric Freshener Mist 300ml", "Eliminates stubborn odors on sofas, curtains, and linens with pure essential oils.", 260.0, 60, "https://res.cloudinary.com/oqmadwpj/image/upload/v1787846347/ecommerce/products/k5jjawg3uy4gamhpagie.jpg", "ACTIVE", "household")
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
        log.info("Full Production Database Seeding Complete! Total Categories: {}, Total Products: {}", catCount, prodCount);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Database successfully seeded with full 90+ product catalog!");
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
