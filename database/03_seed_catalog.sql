USE UMinimalistDB;

DELETE FROM dbo.product_variants;
DELETE FROM dbo.products;
DELETE FROM dbo.categories;

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

INSERT INTO dbo.product_variants (product_id, sku, color, size, stock_quantity)
SELECT p.id, v.sku, v.color, v.size, v.stock_quantity
FROM dbo.products p
CROSS APPLY (VALUES
    (N'AIR-COTTON-TEE-CREAM-S', N'Cream', N'S', 12),
    (N'AIR-COTTON-TEE-CREAM-M', N'Cream', N'M', 13),
    (N'AIR-COTTON-TEE-WHITE-L', N'White', N'L', 11),
    (N'AIR-COTTON-TEE-WHITE-XL', N'White', N'XL', 12)
) v(sku, color, size, stock_quantity)
WHERE p.slug = N'air-cotton-tee';

INSERT INTO dbo.product_variants (product_id, sku, color, size, stock_quantity)
SELECT p.id, v.sku, v.color, v.size, v.stock_quantity
FROM dbo.products p
CROSS APPLY (VALUES
    (N'LIGHT-UTILITY-JACKET-NAVY-S', N'Navy', N'S', 6),
    (N'LIGHT-UTILITY-JACKET-NAVY-M', N'Navy', N'M', 7),
    (N'LIGHT-UTILITY-JACKET-NAVY-L', N'Navy', N'L', 5)
) v(sku, color, size, stock_quantity)
WHERE p.slug = N'light-utility-jacket';

INSERT INTO dbo.product_variants (product_id, sku, color, size, stock_quantity)
SELECT p.id, v.sku, v.color, v.size, v.stock_quantity
FROM dbo.products p
CROSS APPLY (VALUES
    (N'OXFORD-SHIRT-WHITE-S', N'White', N'S', 7),
    (N'OXFORD-SHIRT-WHITE-M', N'White', N'M', 8),
    (N'OXFORD-SHIRT-WHITE-L', N'White', N'L', 8),
    (N'OXFORD-SHIRT-WHITE-XL', N'White', N'XL', 8)
) v(sku, color, size, stock_quantity)
WHERE p.slug = N'oxford-shirt';

INSERT INTO dbo.product_variants (product_id, sku, color, size, stock_quantity)
SELECT p.id, v.sku, v.color, v.size, v.stock_quantity
FROM dbo.products p
CROSS APPLY (VALUES
    (N'SOFT-JERSEY-TEE-SAGE-XS', N'Sage', N'XS', 10),
    (N'SOFT-JERSEY-TEE-SAGE-S', N'Sage', N'S', 11),
    (N'SOFT-JERSEY-TEE-SAGE-M', N'Sage', N'M', 12),
    (N'SOFT-JERSEY-TEE-SAGE-L', N'Sage', N'L', 9)
) v(sku, color, size, stock_quantity)
WHERE p.slug = N'soft-jersey-tee';

INSERT INTO dbo.product_variants (product_id, sku, color, size, stock_quantity)
SELECT p.id, v.sku, v.color, v.size, v.stock_quantity
FROM dbo.products p
CROSS APPLY (VALUES
    (N'SMART-ANKLE-PANTS-BLACK-XS', N'Black', N'XS', 5),
    (N'SMART-ANKLE-PANTS-BLACK-S', N'Black', N'S', 5),
    (N'SMART-ANKLE-PANTS-BLACK-M', N'Black', N'M', 6),
    (N'SMART-ANKLE-PANTS-BLACK-L', N'Black', N'L', 5),
    (N'SMART-ANKLE-PANTS-BLACK-XL', N'Black', N'XL', 5)
) v(sku, color, size, stock_quantity)
WHERE p.slug = N'smart-ankle-pants';

INSERT INTO dbo.product_variants (product_id, sku, color, size, stock_quantity)
SELECT p.id, v.sku, v.color, v.size, v.stock_quantity
FROM dbo.products p
CROSS APPLY (VALUES
    (N'EVERYDAY-ZIP-HOODIE-GREY-S', N'Grey', N'S', 4),
    (N'EVERYDAY-ZIP-HOODIE-GREY-M', N'Grey', N'M', 4),
    (N'EVERYDAY-ZIP-HOODIE-GREY-L', N'Grey', N'L', 4),
    (N'EVERYDAY-ZIP-HOODIE-GREY-XL', N'Grey', N'XL', 3)
) v(sku, color, size, stock_quantity)
WHERE p.slug = N'everyday-zip-hoodie';

INSERT INTO dbo.product_variants (product_id, sku, color, size, stock_quantity)
SELECT p.id, v.sku, v.color, v.size, v.stock_quantity
FROM dbo.products p
CROSS APPLY (VALUES
    (N'LINEN-BLEND-SHIRT-NATURAL-S', N'Natural', N'S', 5),
    (N'LINEN-BLEND-SHIRT-NATURAL-M', N'Natural', N'M', 6),
    (N'LINEN-BLEND-SHIRT-NATURAL-L', N'Natural', N'L', 6),
    (N'LINEN-BLEND-SHIRT-NATURAL-XL', N'Natural', N'XL', 5)
) v(sku, color, size, stock_quantity)
WHERE p.slug = N'linen-blend-shirt';

INSERT INTO dbo.product_variants (product_id, sku, color, size, stock_quantity)
SELECT p.id, v.sku, v.color, v.size, v.stock_quantity
FROM dbo.products p
CROSS APPLY (VALUES
    (N'UTILITY-TOTE-RED-ONE-SIZE', N'Red', N'One size', 64)
) v(sku, color, size, stock_quantity)
WHERE p.slug = N'utility-tote';

INSERT INTO dbo.product_variants (product_id, sku, color, size, stock_quantity)
SELECT p.id, v.sku, v.color, v.size, v.stock_quantity
FROM dbo.products p
CROSS APPLY (VALUES
    (N'SCHOOL-DAY-CARDIGAN-BLUE-110', N'Blue', N'110', 8),
    (N'SCHOOL-DAY-CARDIGAN-BLUE-120', N'Blue', N'120', 9),
    (N'SCHOOL-DAY-CARDIGAN-GREY-130', N'Grey', N'130', 9),
    (N'SCHOOL-DAY-CARDIGAN-GREY-140', N'Grey', N'140', 8)
) v(sku, color, size, stock_quantity)
WHERE p.slug = N'school-day-cardigan';

INSERT INTO dbo.product_variants (product_id, sku, color, size, stock_quantity)
SELECT p.id, v.sku, v.color, v.size, v.stock_quantity
FROM dbo.products p
CROSS APPLY (VALUES
    (N'EASY-COTTON-SHORTS-KHAKI-110', N'Khaki', N'110', 7),
    (N'EASY-COTTON-SHORTS-KHAKI-120', N'Khaki', N'120', 7),
    (N'EASY-COTTON-SHORTS-KHAKI-130', N'Khaki', N'130', 7),
    (N'EASY-COTTON-SHORTS-KHAKI-140', N'Khaki', N'140', 7)
) v(sku, color, size, stock_quantity)
WHERE p.slug = N'easy-cotton-shorts';
