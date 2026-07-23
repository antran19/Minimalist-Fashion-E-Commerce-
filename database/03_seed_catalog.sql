USE UMinimalistDB;

-- Disable all constraints temporarily to allow clean reset
EXEC sp_MSforeachtable "ALTER TABLE ? NOCHECK CONSTRAINT ALL";

IF OBJECT_ID(N'dbo.order_items', N'U') IS NOT NULL DELETE FROM dbo.order_items;
IF OBJECT_ID(N'dbo.orders', N'U') IS NOT NULL DELETE FROM dbo.orders;
IF OBJECT_ID(N'dbo.customer_cart_items', N'U') IS NOT NULL DELETE FROM dbo.customer_cart_items;
IF OBJECT_ID(N'dbo.product_reviews', N'U') IS NOT NULL DELETE FROM dbo.product_reviews;
IF OBJECT_ID(N'dbo.customer_wishlist_items', N'U') IS NOT NULL DELETE FROM dbo.customer_wishlist_items;
IF OBJECT_ID(N'dbo.product_images', N'U') IS NOT NULL DELETE FROM dbo.product_images;
IF OBJECT_ID(N'dbo.product_variants', N'U') IS NOT NULL DELETE FROM dbo.product_variants;
IF OBJECT_ID(N'dbo.products', N'U') IS NOT NULL DELETE FROM dbo.products;
IF OBJECT_ID(N'dbo.categories', N'U') IS NOT NULL DELETE FROM dbo.categories;

INSERT INTO dbo.categories (slug, name, description, display_order)
VALUES
    (N'men', N'Men', N'Everyday layers with clean lines.', 1),
    (N'women', N'Women', N'Soft essentials for simple routines.', 2),
    (N'kids', N'Kids', N'Easy pieces for school and weekends.', 3);

INSERT INTO dbo.products (category_id, slug, name, product_type, description, base_price, crop_class, new_arrival, best_seller)
VALUES
    ((SELECT id FROM dbo.categories WHERE slug = N'men'), N'air-cotton-tee', N'Air Cotton Tee', N'T-shirt', N'Clean daily tee with a soft cotton handfeel.', 19.90, N'product-tee', 1, 1),
    ((SELECT id FROM dbo.categories WHERE slug = N'men'), N'light-utility-jacket', N'Light Utility Jacket', N'Outerwear', N'A light layer for simple weekday outfits.', 59.90, N'product-jacket', 1, 0),
    ((SELECT id FROM dbo.categories WHERE slug = N'men'), N'oxford-shirt', N'Oxford Shirt', N'Shirt', N'Crisp button-down shirt for work and weekends.', 34.90, N'product-shirt', 0, 1),
    ((SELECT id FROM dbo.categories WHERE slug = N'women'), N'soft-jersey-tee', N'Soft Jersey Tee', N'T-shirt', N'Easy jersey tee with a relaxed everyday shape.', 19.90, N'product-sage', 1, 0),
    ((SELECT id FROM dbo.categories WHERE slug = N'women'), N'smart-ankle-pants', N'Smart Ankle Pants', N'Pants', N'Straight ankle pants for repeat outfits.', 39.90, N'product-pants', 0, 1),
    ((SELECT id FROM dbo.categories WHERE slug = N'women'), N'everyday-zip-hoodie', N'Everyday Zip Hoodie', N'Sweatshirt', N'A clean zip hoodie for cool mornings.', 49.90, N'product-hoodie', 1, 0),
    ((SELECT id FROM dbo.categories WHERE slug = N'men'), N'linen-blend-shirt', N'Linen Blend Shirt', N'Shirt', N'Light linen blend shirt with natural texture.', 34.90, N'product-linen', 0, 0),
    ((SELECT id FROM dbo.categories WHERE slug = N'women'), N'utility-tote', N'Utility Tote', N'Accessories', N'A compact tote for daily carry.', 14.90, N'product-tote', 1, 1),
    ((SELECT id FROM dbo.categories WHERE slug = N'kids'), N'school-day-cardigan', N'School Day Cardigan', N'Knitwear', N'Soft cardigan for school and weekends.', 29.90, N'product-kids-cardigan', 1, 0),
    ((SELECT id FROM dbo.categories WHERE slug = N'kids'), N'easy-cotton-shorts', N'Easy Cotton Shorts', N'Shorts', N'Cotton shorts for warm days and active routines.', 16.90, N'product-kids-shorts', 0, 1);

-- 1. Air Cotton Tee (Cream, Light Blue, Pink)
INSERT INTO dbo.product_variants (product_id, sku, color, size, stock_quantity)
SELECT p.id, v.sku, v.color, v.size, v.stock_quantity
FROM dbo.products p
CROSS APPLY (VALUES
    (N'AIR-COTTON-TEE-CREAM-S', N'Cream', N'S', 12),
    (N'AIR-COTTON-TEE-CREAM-M', N'Cream', N'M', 13),
    (N'AIR-COTTON-TEE-LIGHTBLUE-S', N'Light Blue', N'S', 10),
    (N'AIR-COTTON-TEE-LIGHTBLUE-M', N'Light Blue', N'M', 14),
    (N'AIR-COTTON-TEE-PINK-S', N'Pink', N'S', 11),
    (N'AIR-COTTON-TEE-PINK-M', N'Pink', N'M', 12)
) v(sku, color, size, stock_quantity)
WHERE p.slug = N'air-cotton-tee';

-- 2. Light Utility Jacket (Gray, Navy)
INSERT INTO dbo.product_variants (product_id, sku, color, size, stock_quantity)
SELECT p.id, v.sku, v.color, v.size, v.stock_quantity
FROM dbo.products p
CROSS APPLY (VALUES
    (N'LIGHT-UTILITY-JACKET-GRAY-S', N'Gray', N'S', 6),
    (N'LIGHT-UTILITY-JACKET-GRAY-M', N'Gray', N'M', 8),
    (N'LIGHT-UTILITY-JACKET-NAVY-S', N'Navy', N'S', 6),
    (N'LIGHT-UTILITY-JACKET-NAVY-M', N'Navy', N'M', 7)
) v(sku, color, size, stock_quantity)
WHERE p.slug = N'light-utility-jacket';

-- 3. Oxford Shirt (Brown, Light Blue, White)
INSERT INTO dbo.product_variants (product_id, sku, color, size, stock_quantity)
SELECT p.id, v.sku, v.color, v.size, v.stock_quantity
FROM dbo.products p
CROSS APPLY (VALUES
    (N'OXFORD-SHIRT-BROWN-S', N'Brown', N'S', 7),
    (N'OXFORD-SHIRT-BROWN-M', N'Brown', N'M', 8),
    (N'OXFORD-SHIRT-LIGHTBLUE-S', N'Light Blue', N'S', 10),
    (N'OXFORD-SHIRT-LIGHTBLUE-M', N'Light Blue', N'M', 12),
    (N'OXFORD-SHIRT-WHITE-S', N'White', N'S', 9),
    (N'OXFORD-SHIRT-WHITE-M', N'White', N'M', 15)
) v(sku, color, size, stock_quantity)
WHERE p.slug = N'oxford-shirt';

-- 4. Soft Jersey Tee (Brown, Red, White)
INSERT INTO dbo.product_variants (product_id, sku, color, size, stock_quantity)
SELECT p.id, v.sku, v.color, v.size, v.stock_quantity
FROM dbo.products p
CROSS APPLY (VALUES
    (N'SOFT-JERSEY-TEE-BROWN-XS', N'Brown', N'XS', 10),
    (N'SOFT-JERSEY-TEE-BROWN-S', N'Brown', N'S', 11),
    (N'SOFT-JERSEY-TEE-RED-S', N'Red', N'S', 10),
    (N'SOFT-JERSEY-TEE-RED-M', N'Red', N'M', 14),
    (N'SOFT-JERSEY-TEE-WHITE-S', N'White', N'S', 8),
    (N'SOFT-JERSEY-TEE-WHITE-M', N'White', N'M', 12)
) v(sku, color, size, stock_quantity)
WHERE p.slug = N'soft-jersey-tee';

-- 5. Smart Ankle Pants (Black, Brown, White)
INSERT INTO dbo.product_variants (product_id, sku, color, size, stock_quantity)
SELECT p.id, v.sku, v.color, v.size, v.stock_quantity
FROM dbo.products p
CROSS APPLY (VALUES
    (N'SMART-ANKLE-PANTS-BLACK-XS', N'Black', N'XS', 5),
    (N'SMART-ANKLE-PANTS-BLACK-S', N'Black', N'S', 5),
    (N'SMART-ANKLE-PANTS-BROWN-S', N'Brown', N'S', 6),
    (N'SMART-ANKLE-PANTS-BROWN-M', N'Brown', N'M', 8),
    (N'SMART-ANKLE-PANTS-WHITE-S', N'White', N'S', 7),
    (N'SMART-ANKLE-PANTS-WHITE-M', N'White', N'M', 9)
) v(sku, color, size, stock_quantity)
WHERE p.slug = N'smart-ankle-pants';

-- 6. Everyday Zip Hoodie (Black, Cream, Red)
INSERT INTO dbo.product_variants (product_id, sku, color, size, stock_quantity)
SELECT p.id, v.sku, v.color, v.size, v.stock_quantity
FROM dbo.products p
CROSS APPLY (VALUES
    (N'EVERYDAY-ZIP-HOODIE-BLACK-S', N'Black', N'S', 8),
    (N'EVERYDAY-ZIP-HOODIE-BLACK-M', N'Black', N'M', 10),
    (N'EVERYDAY-ZIP-HOODIE-CREAM-S', N'Cream', N'S', 8),
    (N'EVERYDAY-ZIP-HOODIE-CREAM-M', N'Cream', N'M', 10),
    (N'EVERYDAY-ZIP-HOODIE-RED-S', N'Red', N'S', 7),
    (N'EVERYDAY-ZIP-HOODIE-RED-M', N'Red', N'M', 9)
) v(sku, color, size, stock_quantity)
WHERE p.slug = N'everyday-zip-hoodie';


-- 7. Linen Blend Shirt (Cream, Orange, White)
INSERT INTO dbo.product_variants (product_id, sku, color, size, stock_quantity)
SELECT p.id, v.sku, v.color, v.size, v.stock_quantity
FROM dbo.products p
CROSS APPLY (VALUES
    (N'LINEN-BLEND-SHIRT-CREAM-S', N'Cream', N'S', 5),
    (N'LINEN-BLEND-SHIRT-CREAM-M', N'Cream', N'M', 8),
    (N'LINEN-BLEND-SHIRT-ORANGE-S', N'Orange', N'S', 6),
    (N'LINEN-BLEND-SHIRT-ORANGE-M', N'Orange', N'M', 9),
    (N'LINEN-BLEND-SHIRT-WHITE-S', N'White', N'S', 11),
    (N'LINEN-BLEND-SHIRT-WHITE-M', N'White', N'M', 14)
) v(sku, color, size, stock_quantity)
WHERE p.slug = N'linen-blend-shirt';

-- 8. Utility Tote (Pink, Red, Yellow)
INSERT INTO dbo.product_variants (product_id, sku, color, size, stock_quantity)
SELECT p.id, v.sku, v.color, v.size, v.stock_quantity
FROM dbo.products p
CROSS APPLY (VALUES
    (N'UTILITY-TOTE-PINK-ONE-SIZE', N'Pink', N'One size', 25),
    (N'UTILITY-TOTE-RED-ONE-SIZE', N'Red', N'One size', 30),
    (N'UTILITY-TOTE-YELLOW-ONE-SIZE', N'Yellow', N'One size', 20)
) v(sku, color, size, stock_quantity)
WHERE p.slug = N'utility-tote';

-- 9. School Day Cardigan (Black, Dark Green, Navy)
INSERT INTO dbo.product_variants (product_id, sku, color, size, stock_quantity)
SELECT p.id, v.sku, v.color, v.size, v.stock_quantity
FROM dbo.products p
CROSS APPLY (VALUES
    (N'SCHOOL-DAY-CARDIGAN-BLACK-110', N'Black', N'110', 8),
    (N'SCHOOL-DAY-CARDIGAN-BLACK-120', N'Black', N'120', 10),
    (N'SCHOOL-DAY-CARDIGAN-DARKGREEN-110', N'Dark Green', N'110', 7),
    (N'SCHOOL-DAY-CARDIGAN-NAVY-120', N'Navy', N'120', 9)
) v(sku, color, size, stock_quantity)
WHERE p.slug = N'school-day-cardigan';

-- 10. Easy Cotton Shorts (Blue, Gray)
INSERT INTO dbo.product_variants (product_id, sku, color, size, stock_quantity)
SELECT p.id, v.sku, v.color, v.size, v.stock_quantity
FROM dbo.products p
CROSS APPLY (VALUES
    (N'EASY-COTTON-SHORTS-BLUE-110', N'Blue', N'110', 10),
    (N'EASY-COTTON-SHORTS-GRAY-120', N'Gray', N'120', 12)
) v(sku, color, size, stock_quantity)
WHERE p.slug = N'easy-cotton-shorts';

-- Re-enable all constraints
EXEC sp_MSforeachtable "ALTER TABLE ? WITH CHECK CHECK CONSTRAINT ALL";
