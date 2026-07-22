USE UMinimalistDB;

IF OBJECT_ID(N'dbo.product_images', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.product_images (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        product_id BIGINT NOT NULL FOREIGN KEY REFERENCES dbo.products(id) ON DELETE CASCADE,
        image_url NVARCHAR(500) NOT NULL,
        public_id NVARCHAR(200) NULL,
        color NVARCHAR(60) NULL,
        is_primary BIT NOT NULL DEFAULT 0,
        display_order INT NOT NULL DEFAULT 0,
        created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
    );
END;

DELETE FROM dbo.product_images;

INSERT INTO dbo.product_images (product_id, image_url, public_id, color, is_primary, display_order, created_at)
VALUES
    ((SELECT id FROM dbo.products WHERE slug = N'air-cotton-tee'), N'https://res.cloudinary.com/dcroyqkoa/image/upload/v1784721513/uminimalist/products/air-cotton-tee/air-cotton-tee-cream.jpg', N'uminimalist/products/air-cotton-tee/air-cotton-tee-cream', N'Cream', 1, 0, GETUTCDATE()),
    ((SELECT id FROM dbo.products WHERE slug = N'air-cotton-tee'), N'https://res.cloudinary.com/dcroyqkoa/image/upload/v1784721515/uminimalist/products/air-cotton-tee/air-cotton-tee-light-blue.png', N'uminimalist/products/air-cotton-tee/air-cotton-tee-light-blue', N'Light Blue', 0, 1, GETUTCDATE()),
    ((SELECT id FROM dbo.products WHERE slug = N'air-cotton-tee'), N'https://res.cloudinary.com/dcroyqkoa/image/upload/v1784721518/uminimalist/products/air-cotton-tee/air-cotton-tee-pink.png', N'uminimalist/products/air-cotton-tee/air-cotton-tee-pink', N'Pink', 0, 2, GETUTCDATE()),
    ((SELECT id FROM dbo.products WHERE slug = N'easy-cotton-shorts'), N'https://res.cloudinary.com/dcroyqkoa/image/upload/v1784721521/uminimalist/products/easy-cotton-shorts/easy-cotton-shorts-blue.png', N'uminimalist/products/easy-cotton-shorts/easy-cotton-shorts-blue', N'Blue', 1, 0, GETUTCDATE()),
    ((SELECT id FROM dbo.products WHERE slug = N'easy-cotton-shorts'), N'https://res.cloudinary.com/dcroyqkoa/image/upload/v1784721524/uminimalist/products/easy-cotton-shorts/easy-cotton-shorts-gray.jpg', N'uminimalist/products/easy-cotton-shorts/easy-cotton-shorts-gray', N'Gray', 0, 1, GETUTCDATE()),
    ((SELECT id FROM dbo.products WHERE slug = N'everyday-zip-hoodie'), N'https://res.cloudinary.com/dcroyqkoa/image/upload/v1784721527/uminimalist/products/everyday-zip-hoodie/everyday-zip-cream.jpg', N'uminimalist/products/everyday-zip-hoodie/everyday-zip-cream', N'Cream', 1, 0, GETUTCDATE()),
    ((SELECT id FROM dbo.products WHERE slug = N'everyday-zip-hoodie'), N'https://res.cloudinary.com/dcroyqkoa/image/upload/v1784721529/uminimalist/products/everyday-zip-hoodie/everyday-zip-hoodie.jpg', N'uminimalist/products/everyday-zip-hoodie/everyday-zip-hoodie', N'General', 0, 1, GETUTCDATE()),
    ((SELECT id FROM dbo.products WHERE slug = N'everyday-zip-hoodie'), N'https://res.cloudinary.com/dcroyqkoa/image/upload/v1784721531/uminimalist/products/everyday-zip-hoodie/everyday-zip-red.jpg', N'uminimalist/products/everyday-zip-hoodie/everyday-zip-red', N'Red', 0, 2, GETUTCDATE()),
    ((SELECT id FROM dbo.products WHERE slug = N'light-utility-jacket'), N'https://res.cloudinary.com/dcroyqkoa/image/upload/v1784721534/uminimalist/products/light-utility-jacket/light-utility-jacket-gray.jpg', N'uminimalist/products/light-utility-jacket/light-utility-jacket-gray', N'Gray', 1, 0, GETUTCDATE()),
    ((SELECT id FROM dbo.products WHERE slug = N'light-utility-jacket'), N'https://res.cloudinary.com/dcroyqkoa/image/upload/v1784721537/uminimalist/products/light-utility-jacket/light-utility-jacket-navy.jpg', N'uminimalist/products/light-utility-jacket/light-utility-jacket-navy', N'Navy', 0, 1, GETUTCDATE()),
    ((SELECT id FROM dbo.products WHERE slug = N'linen-blend-shirt'), N'https://res.cloudinary.com/dcroyqkoa/image/upload/v1784721540/uminimalist/products/linen-blend-shirt/linen-blend-shirt-cream.jpg', N'uminimalist/products/linen-blend-shirt/linen-blend-shirt-cream', N'Cream', 1, 0, GETUTCDATE()),
    ((SELECT id FROM dbo.products WHERE slug = N'linen-blend-shirt'), N'https://res.cloudinary.com/dcroyqkoa/image/upload/v1784721542/uminimalist/products/linen-blend-shirt/linen-blend-shirt-orange.jpg', N'uminimalist/products/linen-blend-shirt/linen-blend-shirt-orange', N'Orange', 0, 1, GETUTCDATE()),
    ((SELECT id FROM dbo.products WHERE slug = N'linen-blend-shirt'), N'https://res.cloudinary.com/dcroyqkoa/image/upload/v1784721544/uminimalist/products/linen-blend-shirt/linen-blend-shirt-white.jpg', N'uminimalist/products/linen-blend-shirt/linen-blend-shirt-white', N'White', 0, 2, GETUTCDATE()),
    ((SELECT id FROM dbo.products WHERE slug = N'oxford-shirt'), N'https://res.cloudinary.com/dcroyqkoa/image/upload/v1784721546/uminimalist/products/oxford-shirt/oxford-shirt-brown.jpg', N'uminimalist/products/oxford-shirt/oxford-shirt-brown', N'Brown', 1, 0, GETUTCDATE()),
    ((SELECT id FROM dbo.products WHERE slug = N'oxford-shirt'), N'https://res.cloudinary.com/dcroyqkoa/image/upload/v1784721548/uminimalist/products/oxford-shirt/oxford-shirt-light-blue.jpg', N'uminimalist/products/oxford-shirt/oxford-shirt-light-blue', N'Light Blue', 0, 1, GETUTCDATE()),
    ((SELECT id FROM dbo.products WHERE slug = N'oxford-shirt'), N'https://res.cloudinary.com/dcroyqkoa/image/upload/v1784721551/uminimalist/products/oxford-shirt/oxford-shirt-white.jpg', N'uminimalist/products/oxford-shirt/oxford-shirt-white', N'White', 0, 2, GETUTCDATE()),
    ((SELECT id FROM dbo.products WHERE slug = N'school-day-cardigan'), N'https://res.cloudinary.com/dcroyqkoa/image/upload/v1784721554/uminimalist/products/school-day-cardigan/school-day-cardigan-black.jpg', N'uminimalist/products/school-day-cardigan/school-day-cardigan-black', N'Black', 1, 0, GETUTCDATE()),
    ((SELECT id FROM dbo.products WHERE slug = N'school-day-cardigan'), N'https://res.cloudinary.com/dcroyqkoa/image/upload/v1784721556/uminimalist/products/school-day-cardigan/school-day-cardigan-dark-green.jpg', N'uminimalist/products/school-day-cardigan/school-day-cardigan-dark-green', N'Dark Green', 0, 1, GETUTCDATE()),
    ((SELECT id FROM dbo.products WHERE slug = N'school-day-cardigan'), N'https://res.cloudinary.com/dcroyqkoa/image/upload/v1784721558/uminimalist/products/school-day-cardigan/school-day-cardigan-navy.jpg', N'uminimalist/products/school-day-cardigan/school-day-cardigan-navy', N'Navy', 0, 2, GETUTCDATE()),
    ((SELECT id FROM dbo.products WHERE slug = N'smart-ankle-pants'), N'https://res.cloudinary.com/dcroyqkoa/image/upload/v1784721561/uminimalist/products/smart-ankle-pants/smart-ankle-pants-black.jpg', N'uminimalist/products/smart-ankle-pants/smart-ankle-pants-black', N'Black', 1, 0, GETUTCDATE()),
    ((SELECT id FROM dbo.products WHERE slug = N'smart-ankle-pants'), N'https://res.cloudinary.com/dcroyqkoa/image/upload/v1784721564/uminimalist/products/smart-ankle-pants/smart-ankle-pants-brown.jpg', N'uminimalist/products/smart-ankle-pants/smart-ankle-pants-brown', N'Brown', 0, 1, GETUTCDATE()),
    ((SELECT id FROM dbo.products WHERE slug = N'smart-ankle-pants'), N'https://res.cloudinary.com/dcroyqkoa/image/upload/v1784721566/uminimalist/products/smart-ankle-pants/smart-ankle-pants-white.jpg', N'uminimalist/products/smart-ankle-pants/smart-ankle-pants-white', N'White', 0, 2, GETUTCDATE()),
    ((SELECT id FROM dbo.products WHERE slug = N'soft-jersey-tee'), N'https://res.cloudinary.com/dcroyqkoa/image/upload/v1784721568/uminimalist/products/soft-jersey-tee/soft-jersey-tee-brown.jpg', N'uminimalist/products/soft-jersey-tee/soft-jersey-tee-brown', N'Brown', 1, 0, GETUTCDATE()),
    ((SELECT id FROM dbo.products WHERE slug = N'soft-jersey-tee'), N'https://res.cloudinary.com/dcroyqkoa/image/upload/v1784721571/uminimalist/products/soft-jersey-tee/soft-jersey-tee-red.jpg', N'uminimalist/products/soft-jersey-tee/soft-jersey-tee-red', N'Red', 0, 1, GETUTCDATE()),
    ((SELECT id FROM dbo.products WHERE slug = N'soft-jersey-tee'), N'https://res.cloudinary.com/dcroyqkoa/image/upload/v1784721573/uminimalist/products/soft-jersey-tee/soft-jersey-tee-white.jpg', N'uminimalist/products/soft-jersey-tee/soft-jersey-tee-white', N'White', 0, 2, GETUTCDATE()),
    ((SELECT id FROM dbo.products WHERE slug = N'utility-tote'), N'https://res.cloudinary.com/dcroyqkoa/image/upload/v1784721575/uminimalist/products/utility-tote/utility-tote-pink.jpg', N'uminimalist/products/utility-tote/utility-tote-pink', N'Pink', 1, 0, GETUTCDATE()),
    ((SELECT id FROM dbo.products WHERE slug = N'utility-tote'), N'https://res.cloudinary.com/dcroyqkoa/image/upload/v1784721578/uminimalist/products/utility-tote/utility-tote-red.jpg', N'uminimalist/products/utility-tote/utility-tote-red', N'Red', 0, 1, GETUTCDATE()),
    ((SELECT id FROM dbo.products WHERE slug = N'utility-tote'), N'https://res.cloudinary.com/dcroyqkoa/image/upload/v1784721580/uminimalist/products/utility-tote/utility-tote-yellow.jpg', N'uminimalist/products/utility-tote/utility-tote-yellow', N'Yellow', 0, 2, GETUTCDATE());

-- Sync product_images to matching variant SKUs by color
UPDATE pv
SET pv.image_url = pi.image_url,
    pv.image_public_id = pi.public_id
FROM dbo.product_variants pv
JOIN dbo.product_images pi ON pi.product_id = pv.product_id AND pi.color = pv.color;

