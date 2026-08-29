package com.keshav.config;

import com.keshav.entity.Category;
import com.keshav.entity.Product;
import com.keshav.entity.Review;
import com.keshav.entity.User;
import com.keshav.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;
    private final WishlistItemRepository wishlistItemRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderItemRepository orderItemRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(CategoryRepository categoryRepository,
                      ProductRepository productRepository,
                      UserRepository userRepository,
                      ReviewRepository reviewRepository,
                      WishlistItemRepository wishlistItemRepository,
                      CartItemRepository cartItemRepository,
                      OrderItemRepository orderItemRepository,
                      PasswordEncoder passwordEncoder) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.reviewRepository = reviewRepository;
        this.wishlistItemRepository = wishlistItemRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderItemRepository = orderItemRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedIfEmpty();
    }

    public synchronized void seedIfEmpty() {
        try {
            if (productRepository.count() > 0 && userRepository.count() > 0) {
                log.info("Database is already seeded with {} products. Skipping seeding.", productRepository.count());
                return;
            }
            seedUsersIfMissing();
            seedAll();
            seedSampleReviewsIfMissing();
        } catch (Exception e) {
            log.warn("Seeding check/execution encountered warning: {}", e.getMessage());
        }
    }

    public synchronized void seedSampleReviewsIfMissing() {
        if (reviewRepository.count() == 0 && productRepository.count() > 0 && userRepository.count() > 0) {
            User user = userRepository.findByEmail("user@ecommerce.com").orElse(null);
            if (user == null) return;

            Product p1 = productRepository.findBySku("SKU-BEV-001").orElse(null);
            if (p1 != null) {
                Review r1 = new Review();
                r1.setProduct(p1);
                r1.setUser(user);
                r1.setRating(5);
                r1.setTitle("Outstanding Quality & Fast Delivery");
                r1.setComment("Received genuine fresh product in perfect condition. Packaging was super secure. Will definitely buy again!");
                r1.setVerifiedPurchase(true);
                r1.setImages(null);
                r1.setCreatedAt(LocalDateTime.now().minusDays(2));
                r1.setUpdatedAt(LocalDateTime.now().minusDays(2));
                reviewRepository.save(r1);
            }

            Product p2 = productRepository.findBySku("SKU-DRY-006").orElse(null);
            if (p2 != null) {
                Review r2 = new Review();
                r2.setProduct(p2);
                r2.setUser(user);
                r2.setRating(5);
                r2.setTitle("100% Authentic Product");
                r2.setComment("Great value for money. Very satisfied with the prompt delivery and quality.");
                r2.setVerifiedPurchase(true);
                r2.setImages(null);
                r2.setCreatedAt(LocalDateTime.now().minusDays(1));
                r2.setUpdatedAt(LocalDateTime.now().minusDays(1));
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

    @Transactional
    public synchronized Map<String, Object> seedAll() {
        log.info("Ensuring ONLY unique 100-product production catalog with 100 UNIQUE IMAGES...");
        seedUsersIfMissing();

        // 1. Categories
        Map<String, Category> catMap = new HashMap<>();

        List<CategorySeedData> catData = Arrays.asList(
            new CategorySeedData("Beverages", "Refreshing organic juices, cold brew elixirs, and soft drinks.", "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865875/ecommerce/products/ymv0vuegouz9zjxlih2m.jpg", "ACTIVE"),
            new CategorySeedData("Snacks", "Artisan chips, crunchy bites, biscuits, and protein bars.", "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865913/ecommerce/products/altpau7doc1o1dos8sss.jpg", "ACTIVE"),
            new CategorySeedData("Dairy", "Farm-fresh milk, butter, cheese slices, dahi, and paneer.", "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865918/ecommerce/products/zr0vyh4mbisfnxjd4cm1.jpg", "ACTIVE"),
            new CategorySeedData("Personal Care", "Botanical shampoos, face washes, lotions, and grooming kits.", "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865896/ecommerce/products/juyqtzw8ra73aqvensj0.jpg", "ACTIVE"),
            new CategorySeedData("Household", "Detergents, floor cleaners, dishwash, and home essentials.", "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865871/ecommerce/products/wollvn6bwjncj6cfwcqz.jpg", "ACTIVE"),
            new CategorySeedData("Accessories", "Backpacks, leather wallets, sunglasses, and travel gear.", "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865921/ecommerce/products/mnd2dbhcvr6vesr970di.jpg", "ACTIVE"),
            new CategorySeedData("Clothing", "Cotton t-shirts, shirts, denim jackets, jeans, and hoodies.", "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865893/ecommerce/products/eicgcsjixe1hjzfupxte.jpg", "ACTIVE"),
            new CategorySeedData("Footwear", "Casual sneakers, running shoes, flat sandals, and slides.", "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865917/ecommerce/products/v8ckwvdhspi9xdrwmddw.jpg", "ACTIVE"),
            new CategorySeedData("Electronics", "Wireless earbuds, bluetooth speakers, power banks, and tech gadgets.", "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865894/ecommerce/products/yanghglzahgvni2f3dwp.jpg", "ACTIVE")
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

        // 2. Define the EXACT 100 UNIQUE PRODUCTS with 100 UNIQUE IMAGES
        List<CatalogProductSeed> catalog = Arrays.asList(
            // ==========================================
            // GROUP 1: FOR YOU (10 Unique Kits & Bundles)
            // ==========================================
            new CatalogProductSeed("SKU-FORYOU-001", "Daily Essentials Combo Pack", "daily-essentials-combo-pack",
                    "Comprehensive daily essentials bundle featuring personal care, beverages, and healthy snacks for everyday convenience.",
                    "All-in-one curated daily essentials pack for home and office.", "Shoply Select", 999.0, 1299.0, 45, 5,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865935/ecommerce/products/en1wphvnpecpcxpj0kgt.jpg",
                    "ACTIVE", 4.9, 128, true, "household"),

            new CatalogProductSeed("SKU-FORYOU-002", "Premium Morning Starter Kit", "premium-morning-starter-kit",
                    "Kickstart your day with specialty roasted coffee beans, organic green tea, and wholesome honey almonds.",
                    "Artisan morning breakfast and caffeine energy booster kit.", "Shoply Select", 799.0, 1099.0, 40, 5,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865860/ecommerce/products/r0ltroxqk6gjgjhpj3lp.jpg",
                    "ACTIVE", 4.8, 95, true, "beverages"),

            new CatalogProductSeed("SKU-FORYOU-003", "Healthy Snack Box", "healthy-snack-box",
                    "Nutritious assortment of roasted foxnuts, multigrain crisps, protein bars, and dried Medjool dates.",
                    "Guilt-free snacking box packed with clean protein and fiber.", "Shoply Select", 649.0, 899.0, 50, 5,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865928/ecommerce/products/sumx27vle0yqrxqtydxi.jpg",
                    "ACTIVE", 4.7, 110, true, "snacks"),

            new CatalogProductSeed("SKU-FORYOU-004", "Personal Care Essentials Kit", "personal-care-essentials-kit",
                    "Complete daily grooming regimen including botanical face wash, hydrating body lotion, and lip butter.",
                    "Gentle everyday skincare and hygiene essentials pack.", "Shoply Select", 899.0, 1199.0, 35, 5,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865956/ecommerce/products/qdgoxculzobhnm1yzh5w.jpg",
                    "ACTIVE", 4.8, 84, true, "personal care"),

            new CatalogProductSeed("SKU-FORYOU-005", "Home Cleaning Starter Kit", "home-cleaning-starter-kit",
                    "Eco-friendly home care set containing surface cleaner spray, dishwashing liquid, floor wash, and scrub pads.",
                    "Non-toxic plant-powered complete home sanitation kit.", "Shoply Select", 749.0, 999.0, 40, 5,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865871/ecommerce/products/wollvn6bwjncj6cfwcqz.jpg",
                    "ACTIVE", 4.7, 72, true, "household"),

            new CatalogProductSeed("SKU-FORYOU-006", "Work From Home Essentials Kit", "work-from-home-essentials-kit",
                    "Ergonomic aluminum laptop riser, fast-charging braided USB-C cable, and silent optical mouse.",
                    "Productivity powerhouse bundle for modern remote workers.", "Shoply Select", 1499.0, 1999.0, 30, 5,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865901/ecommerce/products/ofozjziabsegnupvxupc.jpg",
                    "ACTIVE", 4.9, 142, true, "electronics"),

            new CatalogProductSeed("SKU-FORYOU-007", "Travel Convenience Kit", "travel-convenience-kit",
                    "Water-resistant crossbody shoulder pack, compact travel umbrella, and 10,000mAh fast power bank.",
                    "Compact lightweight survival pack for flights and weekend getaways.", "Shoply Select", 1199.0, 1599.0, 25, 5,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865925/ecommerce/products/aahjme4lkb5ceswdoxfi.jpg",
                    "ACTIVE", 4.8, 68, true, "accessories"),

            new CatalogProductSeed("SKU-FORYOU-008", "Weekend Refresh Kit", "weekend-refresh-kit",
                    "Cold-pressed fruit juice bottles, gourmet nacho chips, and relaxing aromatherapy soy candle.",
                    "Relax and recharge with refreshing drinks and artisan snacks.", "Shoply Select", 849.0, 1149.0, 35, 5,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865962/ecommerce/products/jkryactpyto1m31rs3va.jpg",
                    "ACTIVE", 4.6, 56, true, "beverages"),

            new CatalogProductSeed("SKU-FORYOU-009", "Student Essentials Bundle", "student-essentials-bundle",
                    "Heavy-duty water-resistant campus backpack, stainless steel water bottle, and cardholder clip.",
                    "Durable daily campus carry gear for students.", "Shoply Select", 999.0, 1399.0, 45, 5,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865864/ecommerce/products/qpvdd5ezudbyfjrgnf1j.jpg",
                    "ACTIVE", 4.8, 92, true, "accessories"),

            new CatalogProductSeed("SKU-FORYOU-010", "Smart Everyday Utility Pack", "smart-everyday-utility-pack",
                    "True wireless ANC earbuds, braided USB-C cable, and multi-surface microfiber towels.",
                    "Smart gadgets and home utility pack for tech-savvy homes.", "Shoply Select", 1299.0, 1799.0, 30, 5,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865891/ecommerce/products/muwwkth8ae7jx2teiwct.jpg",
                    "ACTIVE", 4.9, 115, true, "electronics"),

            // ==========================================
            // GROUP 2: BEVERAGES (10 Unique Products)
            // ==========================================
            new CatalogProductSeed("SKU-BEV-001", "Coca-Cola Original Taste 750ml", "coca-cola-original-taste-750ml",
                    "Crisp, refreshing, iconic sparkling soft drink with signature refreshing taste.",
                    "Iconic 750ml Coca-Cola sparkling soda.", "Coca-Cola", 45.0, 50.0, 120, 10,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865875/ecommerce/products/ymv0vuegouz9zjxlih2m.jpg",
                    "ACTIVE", 4.8, 310, false, "beverages"),

            new CatalogProductSeed("SKU-BEV-002", "Pepsi Black 500ml", "pepsi-black-500ml",
                    "Zero-calorie bold cola taste with maximum refreshing carbonation.",
                    "Zero sugar 500ml Pepsi Black cola.", "Pepsi", 40.0, 45.0, 100, 10,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865887/ecommerce/products/f1df9wetxjzy1c7ahkmw.jpg",
                    "ACTIVE", 4.6, 215, false, "beverages"),

            new CatalogProductSeed("SKU-BEV-003", "Paper Boat Aamras 600ml", "paper-boat-aamras-600ml",
                    "Authentic thick mango drink crafted from ripe Indian mangoes and memory-evoking spices.",
                    "Rich Indian mango pulp beverage.", "Paper Boat", 75.0, 85.0, 90, 8,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865937/ecommerce/products/mg0treqbi7z98hvqunud.jpg",
                    "ACTIVE", 4.9, 420, true, "beverages"),

            new CatalogProductSeed("SKU-BEV-004", "Real Mixed Fruit Juice 1L", "real-mixed-fruit-juice-1l",
                    "Rich blend of 9 handpicked fruits loaded with natural vitamins and antioxidants.",
                    "100% goodness 1-litre mixed fruit juice.", "Real", 130.0, 150.0, 80, 8,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865930/ecommerce/products/fqzkzgwxpdy736fn3jni.jpg",
                    "ACTIVE", 4.7, 190, false, "beverages"),

            new CatalogProductSeed("SKU-BEV-005", "Tropicana Orange Juice 1L", "tropicana-orange-juice-1l",
                    "100% pure squeezed citrus orange juice bursting with Vitamin C sunshine.",
                    "Refreshing 1-litre pure orange juice.", "Tropicana", 135.0, 155.0, 75, 8,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865895/ecommerce/products/rblocsvllrj63duetmi6.jpg",
                    "ACTIVE", 4.7, 180, false, "beverages"),

            new CatalogProductSeed("SKU-BEV-006", "Red Bull Energy Drink 250ml", "red-bull-energy-drink-250ml",
                    "Functional energy beverage formulated with caffeine, taurine, and B-group vitamins.",
                    "Vitalizes body and mind 250ml can.", "Red Bull", 125.0, 130.0, 110, 10,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865851/ecommerce/products/f8unegmuqqqdm8offp2m.jpg",
                    "ACTIVE", 4.8, 280, true, "beverages"),

            new CatalogProductSeed("SKU-BEV-007", "Sting Energy Drink 250ml", "sting-energy-drink-250ml",
                    "Sweet carbonated energy booster with ginseng extract and invigorating flavor.",
                    "Quick energy booster 250ml drink.", "Sting", 20.0, 25.0, 200, 15,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865856/ecommerce/products/oy1rikwza5z4uqbqt1rk.jpg",
                    "ACTIVE", 4.5, 340, false, "beverages"),

            new CatalogProductSeed("SKU-BEV-008", "Nescafé Cold Coffee 180ml", "nescafe-cold-coffee-180ml",
                    "Rich smooth iced coffee drink brewed with premium roasted coffee beans and milk.",
                    "Creamy refreshing iced coffee can.", "Nescafé", 40.0, 45.0, 95, 10,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865947/ecommerce/products/bonrov1cnpaeydmr8vhs.jpg",
                    "ACTIVE", 4.7, 160, false, "beverages"),

            new CatalogProductSeed("SKU-BEV-009", "Himalayan Sparkling Water 750ml", "himalayan-sparkling-water-750ml",
                    "Natural mineral water sourced from pristine Himalayan foothills with gentle carbonation.",
                    "Pristine sparkling spring mineral water.", "Himalayan", 65.0, 75.0, 60, 6,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865982/ecommerce/products/d8guwnielpzogtpxk5im.jpg",
                    "ACTIVE", 4.6, 85, false, "beverages"),

            new CatalogProductSeed("SKU-BEV-010", "Tetley Green Tea Bags 25 Bags", "tetley-green-tea-bags-25-bags",
                    "Pure whole leaf green tea rich in natural antioxidants to boost metabolism and vitality.",
                    "Antioxidant metabolism green tea pack.", "Tetley", 180.0, 210.0, 85, 8,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865936/ecommerce/products/et9g0f3sup2gje7xccu7.jpg",
                    "ACTIVE", 4.7, 140, false, "beverages"),

            // ==========================================
            // GROUP 3: SNACKS (10 Unique Products)
            // ==========================================
            new CatalogProductSeed("SKU-SNK-001", "Lay's Classic Salted Potato Chips 52g", "lays-classic-salted-potato-chips-52g",
                    "Golden farm-grown crispy potato chips seasoned with pure salt.",
                    "Crispy salted potato wafers.", "Lay's", 20.0, 20.0, 150, 15,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865913/ecommerce/products/altpau7doc1o1dos8sss.jpg",
                    "ACTIVE", 4.8, 410, false, "snacks"),

            new CatalogProductSeed("SKU-SNK-002", "Kurkure Masala Munch 90g", "kurkure-masala-munch-90g",
                    "Crunchy puffed rice-corn snack seasoned with zesty Indian chatpata spices.",
                    "Chatpata crunchy masala munchies.", "Kurkure", 20.0, 20.0, 140, 15,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865983/ecommerce/products/rawn2eqkhsacfkustpvt.jpg",
                    "ACTIVE", 4.7, 390, false, "snacks"),

            new CatalogProductSeed("SKU-SNK-003", "Bingo Mad Angles 90g", "bingo-mad-angles-90g",
                    "Triangular crunchy corn snacks loaded with tangy achari seasoning.",
                    "Achari triangle corn chips.", "Bingo", 20.0, 20.0, 130, 15,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865888/ecommerce/products/khpdwmhefauckptgjtcn.jpg",
                    "ACTIVE", 4.6, 250, false, "snacks"),

            new CatalogProductSeed("SKU-SNK-004", "Haldiram's Aloo Bhujia 200g", "haldirams-aloo-bhujia-200g",
                    "Classic Indian crispy spiced potato noodles snack crafted with moth bean flour and mint.",
                    "Authentic crunchy aloo bhujia pack.", "Haldiram's", 55.0, 60.0, 110, 10,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865880/ecommerce/products/jwoez3vngc6wcqsymtzl.jpg",
                    "ACTIVE", 4.9, 480, true, "snacks"),

            new CatalogProductSeed("SKU-SNK-005", "Too Yumm! Multigrain Chips 75g", "too-yumm-multigrain-chips-75g",
                    "Baked non-fried multigrain chips with 40% less fat and tangy tomato flavor.",
                    "Smart baked healthy multigrain chips.", "Too Yumm!", 35.0, 40.0, 90, 10,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865900/ecommerce/products/ecrs0exxawckrouqe2kh.jpg",
                    "ACTIVE", 4.6, 175, false, "snacks"),

            new CatalogProductSeed("SKU-SNK-006", "Pringles Original 107g", "pringles-original-107g",
                    "Iconic hyperbolic paraboloid curved potato crisps with irresistible uniform crunch.",
                    "Stackable original potato crisps.", "Pringles", 115.0, 130.0, 80, 8,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865853/ecommerce/products/ctxdfakpyqkreo8f1tk3.jpg",
                    "ACTIVE", 4.8, 320, false, "snacks"),

            new CatalogProductSeed("SKU-SNK-007", "Doritos Nacho Cheese 100g", "doritos-nacho-cheese-100g",
                    "Bold triangular tortilla corn chips smothered in intense melted cheese flavor.",
                    "Bold nacho cheese tortilla chips.", "Doritos", 50.0, 55.0, 100, 10,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865984/ecommerce/products/cum30d73jww8deobnwxu.jpg",
                    "ACTIVE", 4.8, 290, true, "snacks"),

            new CatalogProductSeed("SKU-SNK-008", "Parle-G Biscuits 800g", "parle-g-biscuits-800g",
                    "India's favorite glucose biscuits packed with wheat milk goodness.",
                    "Wholesome iconic glucose biscuits.", "Parle", 80.0, 90.0, 120, 12,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865857/ecommerce/products/m7iseabo3p9hvjivetyi.jpg",
                    "ACTIVE", 4.9, 520, false, "snacks"),

            new CatalogProductSeed("SKU-SNK-009", "Oreo Original Cookies 120g", "oreo-original-cookies-120g",
                    "Rich dark chocolate sandwich cookies filled with smooth sweet vanilla cream.",
                    "Chocolate cookies with vanilla cream.", "Oreo", 35.0, 40.0, 110, 10,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865938/ecommerce/products/cyneis41xmpdt7awsyj8.jpg",
                    "ACTIVE", 4.8, 380, false, "snacks"),

            new CatalogProductSeed("SKU-SNK-010", "Yoga Bar Protein Bar 50g", "yoga-bar-protein-bar-50g",
                    "Clean plant protein bar loaded with almonds, whey, flaxseeds, and dark cocoa.",
                    "20g protein energy workout bar.", "Yoga Bar", 120.0, 140.0, 75, 8,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865997/ecommerce/products/t86m86dhr0ngshljller.jpg",
                    "ACTIVE", 4.7, 165, false, "snacks"),

            // ==========================================
            // GROUP 4: DAIRY (10 Unique Products & 10 UNIQUE IMAGES)
            // ==========================================
            new CatalogProductSeed("SKU-DRY-001", "Amul Taaza Toned Milk 1L", "amul-taaza-toned-milk-1l",
                    "Homogenized toned milk with 3.0% fat and 8.5% SNF packed in aseptic pouch.",
                    "Pasteurized 1-litre toned milk.", "Amul", 56.0, 58.0, 90, 10,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865958/ecommerce/products/gevtj55cp8zc3zve10qd.jpg",
                    "ACTIVE", 4.9, 490, false, "dairy"),

            new CatalogProductSeed("SKU-DRY-002", "Amul Gold Full Cream Milk 1L", "amul-gold-full-cream-milk-1l",
                    "Nutrient-dense full cream milk with 6.0% fat for rich malai and tea.",
                    "Full cream pasteurized milk 1L.", "Amul", 68.0, 70.0, 85, 10,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865918/ecommerce/products/zr0vyh4mbisfnxjd4cm1.jpg",
                    "ACTIVE", 4.9, 460, true, "dairy"),

            new CatalogProductSeed("SKU-DRY-003", "Amul Masti Buttermilk 1L", "amul-masti-buttermilk-1l",
                    "Refreshing spiced chaas infused with roasted cumin, mint, and rock salt.",
                    "Cooling spiced buttermilk pouch.", "Amul", 35.0, 40.0, 70, 8,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865919/ecommerce/products/ushzcpd00zhif9dvtwz0.jpg",
                    "ACTIVE", 4.8, 310, false, "dairy"),

            new CatalogProductSeed("SKU-DRY-004", "Amul Plain Dahi 400g", "amul-plain-dahi-400g",
                    "Thick creamy cultured curd with live probiotic cultures for digestive health.",
                    "Creamy traditional curd tub.", "Amul", 35.0, 40.0, 80, 8,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865953/ecommerce/products/ke42stfntokytsg5vtxb.jpg",
                    "ACTIVE", 4.8, 290, false, "dairy"),

            new CatalogProductSeed("SKU-DRY-005", "Mother Dairy Classic Curd 400g", "mother-dairy-classic-curd-400g",
                    "Set curd prepared from pasteurized milk with authentic homemade taste.",
                    "Homestyle fresh curd 400g.", "Mother Dairy", 35.0, 40.0, 75, 8,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865858/ecommerce/products/glflvcgohxhwzfasdtee.jpg",
                    "ACTIVE", 4.7, 220, false, "dairy"),

            new CatalogProductSeed("SKU-DRY-006", "Amul Butter 500g", "amul-butter-500g",
                    "Pure golden salted butter churned fresh from cow and buffalo milk.",
                    "Utterly butterly delicious butter.", "Amul", 275.0, 290.0, 65, 8,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865865/ecommerce/products/sgcw1jlfzawnxjrgogc2.jpg",
                    "ACTIVE", 4.9, 580, true, "dairy"),

            new CatalogProductSeed("SKU-DRY-007", "Amul Cheese Slices 200g", "amul-cheese-slices-200g",
                    "Individually wrapped creamy processed cheddar cheese slices for sandwiches.",
                    "Processed cheddar cheese slices.", "Amul", 145.0, 160.0, 60, 6,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865855/ecommerce/products/nihzbiwvhydate6ctmrr.jpg",
                    "ACTIVE", 4.8, 340, false, "dairy"),

            new CatalogProductSeed("SKU-DRY-008", "Amul Fresh Cream 250ml", "amul-fresh-cream-250ml",
                    "Low fat sterilised table cream ideal for fruit salads, soups, and desserts.",
                    "Versatile rich fresh cream carton.", "Amul", 65.0, 70.0, 55, 6,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865866/ecommerce/products/thywdgps9y6usjckl3bq.jpg",
                    "ACTIVE", 4.7, 195, false, "dairy"),

            new CatalogProductSeed("SKU-DRY-009", "Amul Paneer 200g", "amul-paneer-200g",
                    "Soft spongy vacuum-sealed fresh cottage cheese rich in protein.",
                    "Vacuum-packed fresh paneer block.", "Amul", 90.0, 95.0, 80, 8,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865942/ecommerce/products/mvr64sfyjuailbqhqsml.jpg",
                    "ACTIVE", 4.8, 370, false, "dairy"),

            new CatalogProductSeed("SKU-DRY-010", "Mother Dairy Lassi 200ml", "mother-dairy-lassi-200ml",
                    "Traditional sweet cultured yogurt drink flavored with rose and cardamom.",
                    "Sweet authentic Punjabi lassi.", "Mother Dairy", 25.0, 30.0, 100, 10,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865902/ecommerce/products/oq3u2uzfivu9kycluni4.jpg",
                    "ACTIVE", 4.6, 180, false, "dairy"),

            // ==========================================
            // GROUP 5: PERSONAL CARE (10 Unique Products)
            // ==========================================
            new CatalogProductSeed("SKU-PC-001", "Dove Daily Shine Shampoo 340ml", "dove-daily-shine-shampoo-340ml",
                    "Nutritive serum shampoo that protects hair against daily wear and tear.",
                    "Damage repair shiny hair shampoo.", "Dove", 265.0, 320.0, 60, 6,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865862/ecommerce/products/h6zzb73ny8sxolkfac6c.jpg",
                    "ACTIVE", 4.8, 310, false, "personal care"),

            new CatalogProductSeed("SKU-PC-002", "Head & Shoulders Anti-Dandruff Shampoo 340ml", "head-shoulders-anti-dandruff-shampoo-340ml",
                    "Clinically proven zinc pyrithione formula that fights dandruff flakes and itchiness.",
                    "Cool menthol anti-dandruff shampoo.", "Head & Shoulders", 290.0, 350.0, 55, 6,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865932/ecommerce/products/hm122yixvrvzdrpg2mpi.jpg",
                    "ACTIVE", 4.8, 295, true, "personal care"),

            new CatalogProductSeed("SKU-PC-003", "Nivea Men Face Wash 100ml", "nivea-men-face-wash-100ml",
                    "Deep cleaning face cleanser enriched with black carbon to absorb oil and dirt.",
                    "Dark spot reduction charcoal face wash.", "Nivea", 175.0, 210.0, 70, 8,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865985/ecommerce/products/dne5ahgdqgqadwefsbat.jpg",
                    "ACTIVE", 4.7, 240, false, "personal care"),

            new CatalogProductSeed("SKU-PC-004", "Himalaya Neem Face Wash 150ml", "himalaya-neem-face-wash-150ml",
                    "Soap-free herbal formulation that clears impurities and prevents pimples with neem and turmeric.",
                    "Herbal purifying neem face wash.", "Himalaya", 160.0, 195.0, 80, 8,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865896/ecommerce/products/juyqtzw8ra73aqvensj0.jpg",
                    "ACTIVE", 4.9, 490, true, "personal care"),

            new CatalogProductSeed("SKU-PC-005", "Dove Beauty Bar 100g", "dove-beauty-bar-100g",
                    "Classic moisturizing beauty bathing bar with 1/4 moisturizing cream.",
                    "Gentle nourishing cleansing bar.", "Dove", 65.0, 75.0, 110, 10,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865986/ecommerce/products/xaimlmthpk8dws8jju0o.jpg",
                    "ACTIVE", 4.8, 380, false, "personal care"),

            new CatalogProductSeed("SKU-PC-006", "Nivea Soft Moisturizing Cream 100g", "nivea-soft-moisturizing-cream-100g",
                    "Light refreshing moisturizer infused with Vitamin E and Jojoba oil.",
                    "Quick-absorbing soft daily cream.", "Nivea", 185.0, 220.0, 65, 8,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865986/ecommerce/products/bgprwkz7ych4iudtrtlw.jpg",
                    "ACTIVE", 4.8, 290, false, "personal care"),

            new CatalogProductSeed("SKU-PC-007", "Vaseline Intensive Care Lotion 400ml", "vaseline-intensive-care-lotion-400ml",
                    "Deep restore body lotion formulated with micro-droplets of Vaseline jelly.",
                    "Non-greasy deep skin hydration.", "Vaseline", 310.0, 375.0, 50, 6,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865957/ecommerce/products/yev3osskwogmqxofiuuu.jpg",
                    "ACTIVE", 4.7, 310, false, "personal care"),

            new CatalogProductSeed("SKU-PC-008", "Colgate MaxFresh Toothpaste 150g", "colgate-maxfresh-toothpaste-150g",
                    "Cooling crystal gel toothpaste with intense spicy fresh breath power.",
                    "Spicy red cooling crystal gel.", "Colgate", 115.0, 130.0, 90, 10,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865987/ecommerce/products/dnwv55m2zl6i7sbdvfvv.jpg",
                    "ACTIVE", 4.8, 410, false, "personal care"),

            new CatalogProductSeed("SKU-PC-009", "Oral-B Soft Toothbrush 2 Pack", "oral-b-soft-toothbrush-2-pack",
                    "CrissCross multi-angle bristles that remove up to 90% of plaque in hard-to-reach areas.",
                    "Soft enamel-safe toothbrush twin pack.", "Oral-B", 70.0, 85.0, 95, 10,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865914/ecommerce/products/hhkbaegg9ckh2hqgd42a.jpg",
                    "ACTIVE", 4.7, 185, false, "personal care"),

            new CatalogProductSeed("SKU-PC-010", "Gillette Mach3 Razor 1 Handle + 2 Blades", "gillette-mach3-razor-1-handle-2-blades",
                    "3 DuraComfort blades engineered for a classic smooth shave with lubrication strip.",
                    "Precision 3-blade shaving razor kit.", "Gillette", 349.0, 399.0, 45, 5,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865945/ecommerce/products/asjs2icdvvhd7jw2lgtz.jpg",
                    "ACTIVE", 4.9, 360, true, "personal care"),

            // ==========================================
            // GROUP 6: HOUSEHOLD (10 Unique Products)
            // ==========================================
            new CatalogProductSeed("SKU-HH-001", "Surf Excel Matic Detergent 2kg", "surf-excel-matic-detergent-2kg",
                    "Advanced stain-removing washing powder designed specifically for top-load washing machines.",
                    "Tough stain removal 2kg detergent.", "Surf Excel", 440.0, 499.0, 50, 5,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865943/ecommerce/products/lsf62ayv2yzivdq0lr5e.jpg",
                    "ACTIVE", 4.9, 450, true, "household"),

            new CatalogProductSeed("SKU-HH-002", "Ariel Matic Liquid Detergent 2L", "ariel-matic-liquid-detergent-2l",
                    "Concentrated liquid laundry gel that delivers bright whites and vibrant colors with 1 cap.",
                    "Deep cleaning 2L liquid detergent.", "Ariel", 430.0, 490.0, 45, 5,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865863/ecommerce/products/bittxxoxmnncudckudhl.jpg",
                    "ACTIVE", 4.8, 380, false, "household"),

            new CatalogProductSeed("SKU-HH-003", "Harpic Power Plus Toilet Cleaner 1L", "harpic-power-plus-toilet-cleaner-1l",
                    "10x better stain removal power that kills 99.9% of germs and deodorizes.",
                    "Disinfectant toilet bowl cleaner 1L.", "Harpic", 195.0, 225.0, 75, 8,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865886/ecommerce/products/gmpih75ynivvp0f7pjqc.jpg",
                    "ACTIVE", 4.9, 520, false, "household"),

            new CatalogProductSeed("SKU-HH-004", "Vim Dishwash Liquid 750ml", "vim-dishwash-liquid-750ml",
                    "Degreasing dish gel infused with the power of 100 lemons.",
                    "Grease-cutting lemon dish gel 750ml.", "Vim", 145.0, 170.0, 85, 8,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865957/ecommerce/products/sidrawnciayurdkop9sm.jpg",
                    "ACTIVE", 4.8, 410, false, "household"),

            new CatalogProductSeed("SKU-HH-005", "Lizol Disinfectant Floor Cleaner 1L", "lizol-disinfectant-floor-cleaner-1l",
                    "Citrus floor wash that kills 99.99% illness-causing germs and leaves gleaming shine.",
                    "Anti-bacterial citrus floor cleaner 1L.", "Lizol", 215.0, 245.0, 70, 8,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865912/ecommerce/products/rkd1vt9lg8nysfluu59d.jpg",
                    "ACTIVE", 4.8, 360, false, "household"),

            new CatalogProductSeed("SKU-HH-006", "Colin Glass Cleaner 500ml", "colin-glass-cleaner-500ml",
                    "Shine booster formula for mirrors, glass tabletops, windows, and car windshields.",
                    "Streak-free glass cleaner spray.", "Colin", 99.0, 115.0, 80, 8,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865922/ecommerce/products/oey2xnqd8ak0qykwbvpq.jpg",
                    "ACTIVE", 4.7, 270, false, "household"),

            new CatalogProductSeed("SKU-HH-007", "Comfort Fabric Conditioner 860ml", "comfort-fabric-conditioner-860ml",
                    "After-wash fabric softener that provides all-day floral fragrance and softness.",
                    "Morning fresh fabric conditioner.", "Comfort", 220.0, 260.0, 60, 6,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865923/ecommerce/products/tmlsf8gy1qds0db18lby.jpg",
                    "ACTIVE", 4.8, 230, false, "household"),

            new CatalogProductSeed("SKU-HH-008", "Good Knight Mosquito Repellent 45 Nights", "good-knight-mosquito-repellent-45-nights",
                    "Advanced activator liquid refill with active power mode for peaceful mosquito-free sleep.",
                    "45-night electric mosquito repellent refill.", "Good Knight", 85.0, 100.0, 90, 10,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865988/ecommerce/products/flvpjd3tglfscel8yuli.jpg",
                    "ACTIVE", 4.7, 340, false, "household"),

            new CatalogProductSeed("SKU-HH-009", "Scotch-Brite Kitchen Scrub Pad 3 Pack", "scotch-brite-kitchen-scrub-pad-3-pack",
                    "Uniform abrasive mineral grain scrub sponge for spotless stain removal on cookware.",
                    "Heavy-duty dish scrub sponge 3-pack.", "Scotch-Brite", 50.0, 60.0, 120, 12,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865927/ecommerce/products/rog46ijrcvlbnsmqf2wl.jpg",
                    "ACTIVE", 4.8, 390, false, "household"),

            new CatalogProductSeed("SKU-HH-010", "Garbage Bags Large 30 Bags", "garbage-bags-large-30-bags",
                    "Heavy duty tear-resistant biodegradable dustbin liner bags with tie string.",
                    "Leak-proof large 30 dustbin garbage bags.", "CleanPlus", 120.0, 150.0, 100, 10,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865856/ecommerce/products/d8dobhhcy68trk56vcfj.jpg",
                    "ACTIVE", 4.7, 210, false, "household"),

            // ==========================================
            // GROUP 7: ACCESSORIES (10 Unique Products)
            // ==========================================
            new CatalogProductSeed("SKU-ACC-001", "Urban Traveller Laptop Backpack 25L", "urban-traveller-laptop-backpack-25l",
                    "Multi-compartment water-resistant backpack with padded 15.6 inch laptop sleeve.",
                    "Ergonomic commuting laptop backpack.", "Urban Gear", 1499.0, 1999.0, 35, 4,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865921/ecommerce/products/mnd2dbhcvr6vesr970di.jpg",
                    "ACTIVE", 4.8, 195, true, "accessories"),

            new CatalogProductSeed("SKU-ACC-002", "Minimalist Leather Wallet", "minimalist-leather-wallet",
                    "Genuine top-grain leather bi-fold wallet featuring RFID blocking security shield.",
                    "Slim leather wallet with 8 card slots.", "Wildhorn", 699.0, 999.0, 50, 5,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865874/ecommerce/products/hc2dwqgylcjyj7dn9en1.jpg",
                    "ACTIVE", 4.7, 230, false, "accessories"),

            new CatalogProductSeed("SKU-ACC-003", "RFID Card Holder", "rfid-card-holder",
                    "Pop-up aluminum card case with tactile ejector lever and carbon fiber texture.",
                    "Tactile pop-up RFID card case.", "Securo", 449.0, 699.0, 65, 6,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865919/ecommerce/products/pncysm1lbcwyraoasjtj.jpg",
                    "ACTIVE", 4.6, 140, false, "accessories"),

            new CatalogProductSeed("SKU-ACC-004", "Canvas Tote Bag", "canvas-tote-bag",
                    "100% natural organic cotton canvas heavy duty shoulder tote bag.",
                    "Minimalist aesthetic daily shopping tote.", "EcoVibe", 399.0, 599.0, 70, 8,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865909/ecommerce/products/lv17bup6ftf1qfrtm92n.jpg",
                    "ACTIVE", 4.6, 120, false, "accessories"),

            new CatalogProductSeed("SKU-ACC-005", "Classic Analog Watch", "classic-analog-watch",
                    "Water-resistant stainless steel link watch with Japanese quartz movement.",
                    "Timeless silver dress analog watch.", "Titan", 2199.0, 2799.0, 25, 3,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865939/ecommerce/products/ncl44zxsqgjgo5zto3rc.jpg",
                    "ACTIVE", 4.9, 280, true, "accessories"),

            new CatalogProductSeed("SKU-ACC-006", "Polarized Sunglasses", "polarized-sunglasses",
                    "Matte black lightweight square sunglasses with 100% UV400 anti-glare protection.",
                    "Anti-glare UV400 square shades.", "Fastrack", 999.0, 1499.0, 45, 5,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865881/ecommerce/products/nxu0hb4bkanixonasl3g.jpg",
                    "ACTIVE", 4.7, 190, false, "accessories"),

            new CatalogProductSeed("SKU-ACC-007", "Stainless Steel Water Bottle 750ml", "stainless-steel-water-bottle-750ml",
                    "Double-wall vacuum insulated flask keeping drinks cold for 24h and hot for 12h.",
                    "Vacuum insulated thermal bottle.", "Milton", 549.0, 749.0, 60, 6,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865940/ecommerce/products/udyhqpxxisqp5csisj6h.jpg",
                    "ACTIVE", 4.8, 310, false, "accessories"),

            new CatalogProductSeed("SKU-ACC-008", "Compact Travel Umbrella", "compact-travel-umbrella",
                    "Windproof 8-rib automatic open-close folding umbrella with Teflon canopy.",
                    "Wind-resistant automatic folding umbrella.", "Destiny", 499.0, 699.0, 55, 5,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865954/ecommerce/products/uqkkawcqghppqmy6gqou.jpg",
                    "ACTIVE", 4.6, 95, false, "accessories"),

            new CatalogProductSeed("SKU-ACC-009", "Reusable Shopping Bag Large", "reusable-shopping-bag-large",
                    "Foldable ripstop nylon grocery tote with reinforced handles supporting up to 25kg.",
                    "Foldable heavy-duty grocery tote.", "EcoPlus", 199.0, 299.0, 90, 10,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865883/ecommerce/products/ny0uaazmvzysijagl2tj.jpg",
                    "ACTIVE", 4.5, 80, false, "accessories"),

            new CatalogProductSeed("SKU-ACC-010", "Premium Key Organizer", "premium-key-organizer",
                    "Aircraft aluminum compact key holder eliminating pocket jingle for up to 8 keys.",
                    "Jingle-free slim key holder.", "KeySmart", 599.0, 899.0, 40, 4,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865946/ecommerce/products/ctmu8tvwhszbnpuoveif.jpg",
                    "ACTIVE", 4.7, 110, false, "accessories"),

            // ==========================================
            // GROUP 8: CLOTHING (10 Unique Products)
            // ==========================================
            new CatalogProductSeed("SKU-CLO-001", "Men's Regular Fit Cotton T-Shirt Black", "mens-regular-fit-cotton-t-shirt-black",
                    "100% combed bio-washed breathable cotton t-shirt with ribbed crew neck.",
                    "Essential solid black cotton tee.", "Van Heusen", 499.0, 799.0, 65, 8,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865893/ecommerce/products/eicgcsjixe1hjzfupxte.jpg",
                    "ACTIVE", 4.8, 260, false, "clothing"),

            new CatalogProductSeed("SKU-CLO-002", "Men's Oversized Graphic T-Shirt White", "mens-oversized-graphic-t-shirt-white",
                    "Relaxed drop-shoulder heavyweight organic cotton streetwear graphic tee.",
                    "Streetwear oversized white graphic tee.", "Bewakoof", 599.0, 899.0, 55, 6,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865890/ecommerce/products/axfvakykylxwk7925xex.jpg",
                    "ACTIVE", 4.8, 310, true, "clothing"),

            new CatalogProductSeed("SKU-CLO-003", "Men's Slim Fit Casual Shirt Navy", "mens-slim-fit-casual-shirt-navy",
                    "Tailored pure cotton button-down long-sleeve shirt with curved hemline.",
                    "Smart casual navy button-down shirt.", "Peter England", 999.0, 1499.0, 45, 5,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865892/ecommerce/products/zkge7fmwsixpkp9moar9.jpg",
                    "ACTIVE", 4.7, 190, false, "clothing"),

            new CatalogProductSeed("SKU-CLO-004", "Women's Relaxed Fit T-Shirt Beige", "womens-relaxed-fit-t-shirt-beige",
                    "Super soft slub cotton relaxed fit round neck t-shirt for everyday comfort.",
                    "Minimalist beige daily cotton tee.", "H&M", 499.0, 699.0, 50, 5,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865934/ecommerce/products/bpbbdlfugdyltarekvqa.jpg",
                    "ACTIVE", 4.7, 145, false, "clothing"),

            new CatalogProductSeed("SKU-CLO-005", "Women's Oversized Hoodie Grey", "womens-oversized-hoodie-grey",
                    "Brushed fleece cozy pullover hoodie with kangaroo pocket and drawstring hood.",
                    "Cozy heather grey oversized hoodie.", "Roadster", 1199.0, 1799.0, 40, 4,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865884/ecommerce/products/b8a66g0jxewg7hvmphyp.jpg",
                    "ACTIVE", 4.9, 280, true, "clothing"),

            new CatalogProductSeed("SKU-CLO-006", "Women's Casual Denim Jacket Blue", "womens-casual-denim-jacket-blue",
                    "Classic authentic trucker denim jacket with button flap chest pockets.",
                    "Authentic blue trucker denim jacket.", "Levi's", 1999.0, 2899.0, 30, 4,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865877/ecommerce/products/ppqzjrauvldamtsh3lr6.jpg",
                    "ACTIVE", 4.8, 175, false, "clothing"),

            new CatalogProductSeed("SKU-CLO-007", "Men's Regular Fit Jeans Dark Blue", "mens-regular-fit-jeans-dark-blue",
                    "Comfort stretch 5-pocket denim jeans in versatile dark indigo wash.",
                    "Classic dark indigo 5-pocket jeans.", "Spykar", 1499.0, 2199.0, 40, 5,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865988/ecommerce/products/qakpm0hhq6wooivqbjui.jpg",
                    "ACTIVE", 4.7, 210, false, "clothing"),

            new CatalogProductSeed("SKU-CLO-008", "Women's Straight Fit Jeans Light Blue", "womens-straight-fit-jeans-light-blue",
                    "High-rise vintage light wash straight-leg cotton jeans.",
                    "Vintage light blue straight fit jeans.", "Only", 1599.0, 2299.0, 35, 4,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865898/ecommerce/products/w34xsfu31vumcvglyzyh.jpg",
                    "ACTIVE", 4.8, 160, false, "clothing"),

            new CatalogProductSeed("SKU-CLO-009", "Unisex Cotton Sweatshirt Black", "unisex-cotton-sweatshirt-black",
                    "Heavyweight 320 GSM French terry crewneck sweatshirt with ribbed cuffs.",
                    "Classic black French terry crewneck.", "Puma", 1299.0, 1899.0, 45, 5,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865885/ecommerce/products/nz5jg5ngfzk72xn7zuxh.jpg",
                    "ACTIVE", 4.8, 230, false, "clothing"),

            new CatalogProductSeed("SKU-CLO-010", "Unisex Lightweight Track Pants Charcoal", "unisex-lightweight-track-pants-charcoal",
                    "Moisture-wicking athletic training joggers with secure zipper pockets.",
                    "Athletic training charcoal joggers.", "Nike", 1199.0, 1699.0, 50, 5,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865894/ecommerce/products/ix9uaoej6kkyswdpzc7s.jpg",
                    "ACTIVE", 4.8, 190, false, "clothing"),

            // ==========================================
            // GROUP 9: FOOTWEAR (10 Unique Products)
            // ==========================================
            new CatalogProductSeed("SKU-FTW-001", "Men's Casual Sneakers White", "mens-casual-sneakers-white",
                    "Full-grain white leather upper sneakers with cushioned cupsole.",
                    "Classic white low-top casual sneakers.", "Puma", 1999.0, 2999.0, 40, 4,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865872/ecommerce/products/dzvjgvwsclxnyjojueq5.jpg",
                    "ACTIVE", 4.9, 390, true, "footwear"),

            new CatalogProductSeed("SKU-FTW-002", "Men's Running Shoes Black", "mens-running-shoes-black",
                    "Breathable engineered air-mesh running shoes with responsive foam cushioning.",
                    "Lightweight breathable performance runners.", "Nike", 2499.0, 3499.0, 35, 4,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865917/ecommerce/products/v8ckwvdhspi9xdrwmddw.jpg",
                    "ACTIVE", 4.8, 340, true, "footwear"),

            new CatalogProductSeed("SKU-FTW-003", "Men's Canvas Sneakers Navy", "mens-canvas-sneakers-navy",
                    "Durable vulcanized canvas low-top skate shoes with rubber toe bumper.",
                    "Timeless navy canvas low-tops.", "Converse", 1699.0, 2299.0, 45, 5,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865904/ecommerce/products/nl8ixcatrvhnajp4qmdx.jpg",
                    "ACTIVE", 4.7, 180, false, "footwear"),

            new CatalogProductSeed("SKU-FTW-004", "Men's Comfort Slides Grey", "mens-comfort-slides-grey",
                    "Molded lightweight Croslite foam recovery slides with textured footbed.",
                    "Arch support daily recovery slides.", "Crocs", 899.0, 1299.0, 60, 6,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865906/ecommerce/products/bdz3dwv8r2f06utidh7o.jpg",
                    "ACTIVE", 4.8, 220, false, "footwear"),

            new CatalogProductSeed("SKU-FTW-005", "Women's Casual Sneakers White", "womens-casual-sneakers-white",
                    "Sleek minimalist white tennis sneakers with memory foam insole.",
                    "Clean white platform lifestyle sneakers.", "Adidas", 2199.0, 3199.0, 35, 4,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865948/ecommerce/products/wojbdbpwhhyhgub7eaej.jpg",
                    "ACTIVE", 4.9, 290, true, "footwear"),

            new CatalogProductSeed("SKU-FTW-006", "Women's Running Shoes Pink", "womens-running-shoes-pink",
                    "Shock-absorbing mesh athletic trainers with air-cooled memory foam.",
                    "Cushioned pink athletic running shoes.", "Skechers", 2599.0, 3799.0, 30, 4,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865952/ecommerce/products/wazfbx6qg6chdd7iczqv.jpg",
                    "ACTIVE", 4.8, 210, false, "footwear"),

            new CatalogProductSeed("SKU-FTW-007", "Women's Flat Sandals Tan", "womens-flat-sandals-tan",
                    "Comfortable faux-leather slip-on open-toe flats with cushioned sole.",
                    "Tan slip-on summer flat sandals.", "Bata", 799.0, 1099.0, 50, 5,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865916/ecommerce/products/ybltraeaxqwwzdcnnwdo.jpg",
                    "ACTIVE", 4.6, 140, false, "footwear"),

            new CatalogProductSeed("SKU-FTW-008", "Women's Everyday Slides Black", "womens-everyday-slides-black",
                    "Soft padded synthetic strap slide sandals with contoured footbed.",
                    "Quick-dry black everyday slides.", "Puma", 899.0, 1199.0, 55, 5,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865926/ecommerce/products/zdqrzw2ch9eksyqkyxjy.jpg",
                    "ACTIVE", 4.7, 185, false, "footwear"),

            new CatalogProductSeed("SKU-FTW-009", "Unisex Walking Shoes Grey", "unisex-walking-shoes-grey",
                    "Slip-on featherlight walking shoes with breathable knitted upper.",
                    "Slip-on ergonomic walking shoes.", "Campus", 1199.0, 1599.0, 50, 5,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865944/ecommerce/products/mdq9kjwhamkk5i9juqbd.jpg",
                    "ACTIVE", 4.7, 210, false, "footwear"),

            new CatalogProductSeed("SKU-FTW-010", "Unisex Sports Sandals Black", "unisex-sports-sandals-black",
                    "Adjustable velcro strap all-weather outdoor sandals with rugged grip sole.",
                    "Rugged outdoor grip sports sandals.", "Sparx", 849.0, 1149.0, 60, 6,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865941/ecommerce/products/e1yvbmrytvznpy4tez8w.jpg",
                    "ACTIVE", 4.6, 175, false, "footwear"),

            // ==========================================
            // GROUP 10: ELECTRONICS (10 Unique Products)
            // ==========================================
            new CatalogProductSeed("SKU-ELE-001", "boAt Airdopes Wireless Earbuds", "boat-airdopes-wireless-earbuds",
                    "True wireless Bluetooth earbuds with 13mm audio drivers, ENx noise cancellation, and 42H playtime.",
                    "42H battery life ANC wireless earbuds.", "boAt", 1499.0, 2990.0, 80, 8,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865894/ecommerce/products/yanghglzahgvni2f3dwp.jpg",
                    "ACTIVE", 4.8, 560, true, "electronics"),

            new CatalogProductSeed("SKU-ELE-002", "JBL Go Portable Bluetooth Speaker", "jbl-go-portable-bluetooth-speaker",
                    "Ultra-compact IP67 waterproof wireless speaker delivering punchy JBL Pro Sound.",
                    "Pocket-sized waterproof bluetooth speaker.", "JBL", 1999.0, 2999.0, 60, 6,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865869/ecommerce/products/lf1u5mmqeimpgxhyoagx.jpg",
                    "ACTIVE", 4.9, 420, true, "electronics"),

            new CatalogProductSeed("SKU-ELE-003", "Logitech Wireless Mouse", "logitech-wireless-mouse",
                    "2.4GHz wireless optical mouse with 1000 DPI sensor and 12-month battery life.",
                    "Ergonomic plug-and-play wireless mouse.", "Logitech", 699.0, 995.0, 90, 10,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865915/ecommerce/products/mscdc6dfns9zcbldr3wi.jpg",
                    "ACTIVE", 4.8, 380, false, "electronics"),

            new CatalogProductSeed("SKU-ELE-004", "HP Wireless Keyboard", "hp-wireless-keyboard",
                    "Full-size slim membrane keyboard with low-profile quiet keys and spill resistance.",
                    "Slim quiet wireless desktop keyboard.", "HP", 1199.0, 1699.0, 50, 5,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865901/ecommerce/products/dhumv3edxhtvqywm03ez.jpg",
                    "ACTIVE", 4.7, 210, false, "electronics"),

            new CatalogProductSeed("SKU-ELE-005", "Anker 20W USB-C Charger", "anker-20w-usb-c-charger",
                    "High-speed PowerPort III 20W Power Delivery wall adapter for iPhones and Android.",
                    "Ultra-compact 20W PD fast wall adapter.", "Anker", 1099.0, 1499.0, 70, 7,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865961/ecommerce/products/s5j4da6lirddrgbp3jvm.jpg",
                    "ACTIVE", 4.9, 310, true, "electronics"),

            new CatalogProductSeed("SKU-ELE-006", "Portronics USB-C Cable 1.2m", "portronics-usb-c-cable-1-2m",
                    "Heavy-duty nylon braided 65W fast-charging Type-C to Type-C syncing cable.",
                    "Durable 65W fast-charging Type-C cable.", "Portronics", 249.0, 499.0, 120, 12,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865876/ecommerce/products/kgwcbvvxyvkuootemnzh.jpg",
                    "ACTIVE", 4.7, 240, false, "electronics"),

            new CatalogProductSeed("SKU-ELE-007", "MI 10,000mAh Power Bank", "mi-10000mah-power-bank",
                    "Slim aluminium alloy 22.5W ultra-fast two-way charging dual output power bank.",
                    "22.5W fast-charge 10000mAh power bank.", "Xiaomi", 1299.0, 1799.0, 65, 6,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865932/ecommerce/products/jectccnthaqtj1bu1ajb.jpg",
                    "ACTIVE", 4.8, 490, true, "electronics"),

            new CatalogProductSeed("SKU-ELE-008", "SanDisk 128GB USB Flash Drive", "sandisk-128gb-usb-flash-drive",
                    "Ultra Dual Drive Go USB Type-C and Type-A high-speed 150MB/s thumb drive.",
                    "128GB dual Type-C & Type-A flash drive.", "SanDisk", 799.0, 1250.0, 85, 8,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865907/ecommerce/products/ox58nt0lek46nhqgemgx.jpg",
                    "ACTIVE", 4.8, 330, false, "electronics"),

            new CatalogProductSeed("SKU-ELE-009", "AmazonBasics Laptop Stand", "amazonbasics-laptop-stand",
                    "Ventilated height-adjustable mesh metal stand promoting laptop cooling and posture.",
                    "Ergonomic ventilated laptop desk riser.", "AmazonBasics", 749.0, 1199.0, 55, 5,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865949/ecommerce/products/lu46fwlx9lx8hq0j2hnn.jpg",
                    "ACTIVE", 4.7, 180, false, "electronics"),

            new CatalogProductSeed("SKU-ELE-010", "TP-Link Wi-Fi 6 Router", "tp-link-wi-fi-6-router",
                    "Dual-band Gigabit wireless internet router with speeds up to 1.5 Gbps and 4 antennas.",
                    "Next-gen dual-band Wi-Fi 6 router.", "TP-Link", 2999.0, 4499.0, 35, 4,
                    "https://res.cloudinary.com/oqmadwpj/image/upload/v1787865867/ecommerce/products/mnwaru9uaup4gyghgsak.jpg",
                    "ACTIVE", 4.8, 220, true, "electronics")
        );

        // 3. Clean up non-catalog / stale duplicate products
        Set<String> validSkus = catalog.stream().map(c -> c.sku).collect(Collectors.toSet());
        List<Product> existingProducts = productRepository.findAll();
        int deletedOld = 0;

        for (Product existing : existingProducts) {
            if (existing.getSku() == null || !validSkus.contains(existing.getSku())) {
                try {
                    // Remove any associated relations to avoid FK violations
                    reviewRepository.findAll().stream()
                            .filter(r -> r.getProduct() != null && r.getProduct().getId().equals(existing.getId()))
                            .forEach(reviewRepository::delete);

                    wishlistItemRepository.findAll().stream()
                            .filter(w -> w.getProduct() != null && w.getProduct().getId().equals(existing.getId()))
                            .forEach(wishlistItemRepository::delete);

                    cartItemRepository.findAll().stream()
                            .filter(c -> c.getProduct() != null && c.getProduct().getId().equals(existing.getId()))
                            .forEach(cartItemRepository::delete);

                    orderItemRepository.findAll().stream()
                            .filter(oi -> oi.getProduct() != null && oi.getProduct().getId().equals(existing.getId()))
                            .forEach(orderItemRepository::delete);

                    productRepository.delete(existing);
                    deletedOld++;
                } catch (Exception e) {
                    log.warn("Failed to delete stale product ID: {} - {}", existing.getId(), e.getMessage());
                }
            }
        }

        if (deletedOld > 0) {
            log.info("Cleaned up {} old/duplicate stale products from database.", deletedOld);
        }

        // 4. Insert or Update ONLY the 100 UNIQUE PRODUCTS with their UNIQUE images
        int inserted = 0;
        int updated = 0;

        for (CatalogProductSeed p : catalog) {
            Product prod = productRepository.findBySku(p.sku)
                    .orElseGet(() -> productRepository.findBySlug(p.slug).orElse(null));

            boolean isNew = (prod == null);
            if (isNew) {
                prod = new Product();
                prod.setSku(p.sku);
                inserted++;
            } else {
                updated++;
            }

            prod.setName(p.name);
            prod.setSlug(p.slug);
            prod.setDescription(p.description);
            prod.setShortDescription(p.shortDescription);
            prod.setBrand(p.brand);
            prod.setPrice(p.price);
            prod.setDiscountPrice(p.discountPrice);
            prod.setStock(p.stock);
            prod.setLowStockThreshold(p.lowStockThreshold);
            prod.setImage(p.image); // Update image to unique authentic URL
            prod.setStatus(p.status);
            prod.setRating(p.rating);
            prod.setReviewCount(p.reviewCount);
            prod.setFeatured(p.featured);

            Category cat = catMap.get(p.categoryKey.toLowerCase());
            if (cat != null) {
                prod.setCategory(cat);
            }

            productRepository.save(prod);
        }

        long catCount = categoryRepository.count();
        long prodCount = productRepository.count();
        log.info("Catalog Seeding Complete! Exact Total Products: {} (should be 100), Total Categories: {}",
                prodCount, catCount);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Unique 100-Product Catalog with 100 Unique Images successfully seeded!");
        response.put("inserted", inserted);
        response.put("updated", updated);
        response.put("deletedOld", deletedOld);
        response.put("totalProducts", prodCount);
        response.put("totalCategories", catCount);
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

    private static class CatalogProductSeed {
        String sku;
        String name;
        String slug;
        String description;
        String shortDescription;
        String brand;
        double price;
        Double discountPrice;
        int stock;
        int lowStockThreshold;
        String image;
        String status;
        double rating;
        int reviewCount;
        boolean featured;
        String categoryKey;

        public CatalogProductSeed(String sku, String name, String slug, String description, String shortDescription,
                                  String brand, double price, Double discountPrice, int stock, int lowStockThreshold,
                                  String image, String status, double rating, int reviewCount, boolean featured,
                                  String categoryKey) {
            this.sku = sku;
            this.name = name;
            this.slug = slug;
            this.description = description;
            this.shortDescription = shortDescription;
            this.brand = brand;
            this.price = price;
            this.discountPrice = discountPrice;
            this.stock = stock;
            this.lowStockThreshold = lowStockThreshold;
            this.image = image;
            this.status = status;
            this.rating = rating;
            this.reviewCount = reviewCount;
            this.featured = featured;
            this.categoryKey = categoryKey;
        }
    }
}
