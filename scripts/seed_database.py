import pymysql
import sys

def seed():
    conn = pymysql.connect(
        user='root',
        password='Keshav@12345',
        unix_socket='/tmp/mysql.sock',
        database='ecommerce',
        autocommit=True
    )

    with conn.cursor() as cursor:
        # Kill hung queries waiting for metadata locks
        cursor.execute("SHOW PROCESSLIST")
        current_thread_id = conn.thread_id()
        processes = cursor.fetchall()
        for row in processes:
            pid = row[0]
            info = str(row[7])
            state = str(row[6])
            if pid != current_thread_id and ('metadata lock' in state.lower() or 'TRUNCATE' in info or 'DELETE FROM' in info or 'CREATE TABLE' in info):
                try:
                    cursor.execute(f"KILL {pid}")
                    print(f"Killed hung thread {pid}")
                except Exception:
                    pass

        cursor.execute("""
            CREATE TABLE IF NOT EXISTS wishlist_items (
                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                user_id BIGINT NOT NULL,
                product_id BIGINT NOT NULL,
                created_at DATETIME NULL,
                CONSTRAINT uk_user_product UNIQUE (user_id, product_id),
                CONSTRAINT fk_wishlist_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
                CONSTRAINT fk_wishlist_product FOREIGN KEY (product_id) REFERENCES product(id) ON DELETE CASCADE
            )
        """)
        print("Ensured wishlist_items table exists in database.")

    categories = [
        (1, 'Beverages', 'Refreshing organic juices, cold brew elixirs, and prebiotic drinks.', 'https://res.cloudinary.com/oqmadwpj/image/upload/v1787846790/ecommerce/products/foq3pj2h2qmtckbuwu0o.jpg', 'ACTIVE'),
        (2, 'Snacks', 'Artisan corn chips, roasted nuts, organic dried fruits, and healthy bites.', 'https://res.cloudinary.com/oqmadwpj/image/upload/v1787846783/ecommerce/products/sonwmknronpjyv4qoxdb.jpg', 'ACTIVE'),
        (3, 'Dairy', 'Farm-fresh milk, organic Greek yogurt, artisan cheeses, and butter.', 'https://res.cloudinary.com/oqmadwpj/image/upload/v1787846339/ecommerce/products/qwdqaxxm1blrcvlfymgc.jpg', 'ACTIVE'),
        (4, 'Personal Care', 'Botanical shampoos, body washes, organic lotions, and gentle skincare.', 'https://res.cloudinary.com/oqmadwpj/image/upload/v1787846342/ecommerce/products/omgs1uts0ckmlwkewdat.jpg', 'ACTIVE'),
        (5, 'Household', 'Plant-derived cleaners, eco surface sprays, and eco-friendly home care.', 'https://res.cloudinary.com/oqmadwpj/image/upload/v1787846347/ecommerce/products/k5jjawg3uy4gamhpagie.jpg', 'ACTIVE'),
        (6, 'Accessories', 'Studio noise-canceling headphones, smart wearables, and tech accessories.', 'https://res.cloudinary.com/oqmadwpj/image/upload/v1787846770/ecommerce/products/yn4qovboszpxtefr7yjo.jpg', 'ACTIVE'),
        (7, 'Clothing', 'Sustainable cotton hoodies, minimal streetwear tees, and casual wear.', 'https://res.cloudinary.com/oqmadwpj/image/upload/v1787846825/ecommerce/products/hhyogdp8dbpetoqfkiie.jpg', 'ACTIVE'),
        (8, 'Footwear', 'Minimalist leather sneakers, ergonomic trainers, and comfortable slides.', 'https://res.cloudinary.com/oqmadwpj/image/upload/v1787846776/ecommerce/products/vmw38u1w7d7nbxbmer9m.jpg', 'ACTIVE'),
        (9, 'Electronics', 'Smart watches, noise cancelling headphones, speakers, and gadgets.', 'https://res.cloudinary.com/oqmadwpj/image/upload/v1787846812/ecommerce/products/mcbgbgucqnd293rjid65.jpg', 'ACTIVE'),
    ]

    products = [
        # Electronics & Accessories
        ('Apex Pro OLED Smartwatch', 'Ultra-bright OLED smartwatch with fitness tracking, heart rate monitoring, and 7-day battery life.', 3499.00, 50, 'https://res.cloudinary.com/oqmadwpj/image/upload/v1787846812/ecommerce/products/mcbgbgucqnd293rjid65.jpg', 'ACTIVE', 9),
        ('Aethelgard Studio Headphones', 'Over-ear wireless headphones featuring hybrid active noise cancellation and plush memory foam earcups.', 2999.00, 40, 'https://res.cloudinary.com/oqmadwpj/image/upload/v1787846770/ecommerce/products/yn4qovboszpxtefr7yjo.jpg', 'ACTIVE', 6),
        ('True Wireless Earbuds Pro', 'Ultra HD sound drivers with active noise cancellation and 30-hour playback case.', 2499.00, 60, 'https://res.cloudinary.com/oqmadwpj/image/upload/v1787846770/ecommerce/products/yn4qovboszpxtefr7yjo.jpg', 'ACTIVE', 9),
        ('Precision Beard & Hair Trimmer', 'Self-sharpening titanium blades with 20 length settings and fast USB-C charging.', 1299.00, 35, 'https://res.cloudinary.com/oqmadwpj/image/upload/v1787846350/ecommerce/products/wjcpgszt9ygl61qcag7s.jpg', 'ACTIVE', 9),
        ('Stainless Steel Chrono Watch', 'Water resistant silver stainless steel chronograph wrist watch with Japanese quartz movement.', 2199.00, 25, 'https://res.cloudinary.com/oqmadwpj/image/upload/v1787846344/ecommerce/products/quvwpulztuthhxzw6etw.jpg', 'ACTIVE', 6),

        # Clothing & Footwear
        ('Apex White Leather Sneaker', 'Full-grain white calfskin leather upper sneakers with lightweight durable rubber soles.', 1899.00, 45, 'https://res.cloudinary.com/oqmadwpj/image/upload/v1787846776/ecommerce/products/vmw38u1w7d7nbxbmer9m.jpg', 'ACTIVE', 8),
        ('Royal Blue Terravibe Hoodie', '450gsm heavyweight organic French terry cotton hoodie with double-lined hood.', 1299.00, 50, 'https://res.cloudinary.com/oqmadwpj/image/upload/v1787846825/ecommerce/products/hhyogdp8dbpetoqfkiie.jpg', 'ACTIVE', 7),
        ('Traditional Cotton Festive Kurta', 'Breathable handwoven pure cotton mandarin collar ethnic kurta perfect for Rakhi & celebrations.', 1199.00, 30, 'https://res.cloudinary.com/oqmadwpj/image/upload/v1787846348/ecommerce/products/ai2oqvvi0ly2rguzvo1l.jpg', 'ACTIVE', 7),
        ('Quick-Dry Athletic Activewear T-Shirt', 'Lightweight stretch moisture-wicking training crewneck for workouts and sports.', 799.00, 70, 'https://res.cloudinary.com/oqmadwpj/image/upload/v1787846349/ecommerce/products/tc7exznhsshk3tpdjzyp.jpg', 'ACTIVE', 7),
        ('Oversized Streetwear Graphic Tee', 'Relaxed drop-shoulder heavyweight organic cotton graphic streetwear t-shirt.', 499.00, 80, 'https://res.cloudinary.com/oqmadwpj/image/upload/v1787846345/ecommerce/products/lkf9m7irrwycezuvypa2.jpg', 'ACTIVE', 7),

        # Snacks & Festive
        ('Artisan Festive Sweet & Nut Hamper', 'Premium Rakshabandhan & festive gift box containing roasted nuts, dates, and sweets.', 699.00, 40, 'https://res.cloudinary.com/oqmadwpj/image/upload/v1787846338/ecommerce/products/elq6vcvrofjtvcff01gu.jpg', 'ACTIVE', 2),
        ('Organic Dried Figs & Dates', 'Sun-dried natural Mediterranean figs and Medjool dates with zero added sugar.', 349.00, 60, 'https://res.cloudinary.com/oqmadwpj/image/upload/v1787846352/ecommerce/products/ap3odxzsbeeul5vk0fdr.jpg', 'ACTIVE', 2),
        ('Chipotle Lime Nachos', 'Gluten-free organic stone-ground corn chips seasoned with smoked chipotle & tangy lime.', 99.00, 100, 'https://res.cloudinary.com/oqmadwpj/image/upload/v1787846783/ecommerce/products/sonwmknronpjyv4qoxdb.jpg', 'ACTIVE', 2),
        ('Roasted Salted Almonds 200g', 'Slow roasted California almonds lightly dusted with Himalayan pink salt.', 299.00, 75, 'https://images.unsplash.com/photo-1508061252425-f3832161b462?auto=format&fit=crop&w=600&q=80', 'ACTIVE', 2),

        # Beverages
        ('Berry Blast Juice', 'Cold-pressed prebiotic & probiotic juice drink infused with wild organic berries and organic lemon.', 199.00, 90, 'https://res.cloudinary.com/oqmadwpj/image/upload/v1787846790/ecommerce/products/foq3pj2h2qmtckbuwu0o.jpg', 'ACTIVE', 1),
        ('Zest Immunity Booster', 'Raw organic Valencia orange juice blended with organic turmeric root, black pepper, and ginger.', 189.00, 85, 'https://res.cloudinary.com/oqmadwpj/image/upload/v1787846773/ecommerce/products/f9dankyiaztin6lfhl0q.jpg', 'ACTIVE', 1),
        ('Organic Cold Brew Coffee', 'Steeped for 18 hours using 100% Arabica organic single-origin beans.', 249.00, 65, 'https://res.cloudinary.com/oqmadwpj/image/upload/v1787846346/ecommerce/products/cbync2cj7nl96ci2r1er.jpg', 'ACTIVE', 1),

        # Personal Care, Household & Accessories
        ('Organic Botanical Body Lotion', 'Deep hydration body moisturizer enriched with cold-pressed jojoba oil and shea butter.', 349.00, 50, 'https://res.cloudinary.com/oqmadwpj/image/upload/v1787846343/ecommerce/products/x75tuhyehqywxjjyxjxc.jpg', 'ACTIVE', 4),
        ('Organic Lavender Surface Spray', 'Non-toxic multi-surface cleaner made with natural essential oils and antibacterial plant agents.', 249.00, 40, 'https://res.cloudinary.com/oqmadwpj/image/upload/v1787846342/ecommerce/products/ngtg0d35hoziqkth5ycv.jpg', 'ACTIVE', 5),
        ('Plush Giant Bear Companion', 'Ultra-soft premium plush teddy bear toy gift with velvety fur and embroidered paws.', 899.00, 30, 'https://res.cloudinary.com/oqmadwpj/image/upload/v1787846347/ecommerce/products/haozaebqas8wxykrvjqm.jpg', 'ACTIVE', 6),
        ('Farm Fresh Whole Milk 1L', 'Grass-fed pasture raised organic whole milk with natural cream layer.', 89.00, 120, 'https://res.cloudinary.com/oqmadwpj/image/upload/v1787846341/ecommerce/products/mobflyhllbqeejcagegn.jpg', 'ACTIVE', 3),
    ]

    with conn.cursor() as cursor:
        cursor.execute("DELETE FROM order_items")
        cursor.execute("DELETE FROM cart_items")
        cursor.execute("DELETE FROM product")
        cursor.execute("DELETE FROM category")

        # Insert Categories
        cat_sql = "INSERT INTO category (id, name, description, image, status) VALUES (%s, %s, %s, %s, %s)"
        cursor.executemany(cat_sql, categories)
        print(f"Successfully inserted {len(categories)} categories into MySQL.")

        # Insert Products
        prod_sql = "INSERT INTO product (name, description, price, stock, image, status, category_id) VALUES (%s, %s, %s, %s, %s, %s, %s)"
        cursor.executemany(prod_sql, products)
        print(f"Successfully inserted {len(products)} products into MySQL.")

        # Verify
        cursor.execute("SELECT count(*) FROM category")
        cat_count = cursor.fetchone()[0]
        cursor.execute("SELECT count(*) FROM product")
        prod_count = cursor.fetchone()[0]

        print(f"Database Verification: {cat_count} categories and {prod_count} products are now LIVE in the database!")

    conn.close()

if __name__ == '__main__':
    seed()
