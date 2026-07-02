USE UMinimalistDB;

IF OBJECT_ID(N'dbo.product_variants', N'U') IS NOT NULL
BEGIN
    DROP TABLE dbo.product_variants;
END;

IF OBJECT_ID(N'dbo.products', N'U') IS NOT NULL
BEGIN
    DROP TABLE dbo.products;
END;

IF OBJECT_ID(N'dbo.categories', N'U') IS NOT NULL
BEGIN
    DROP TABLE dbo.categories;
END;

CREATE TABLE dbo.categories (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    slug NVARCHAR(60) NOT NULL UNIQUE,
    name NVARCHAR(120) NOT NULL,
    description NVARCHAR(255) NULL,
    display_order INT NOT NULL DEFAULT 0,
    active BIT NOT NULL DEFAULT 1
);

CREATE TABLE dbo.products (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    category_id BIGINT NOT NULL,
    slug NVARCHAR(120) NOT NULL UNIQUE,
    name NVARCHAR(180) NOT NULL,
    product_type NVARCHAR(80) NOT NULL,
    description NVARCHAR(500) NULL,
    base_price DECIMAL(10, 2) NOT NULL,
    crop_class NVARCHAR(80) NOT NULL,
    new_arrival BIT NOT NULL DEFAULT 0,
    best_seller BIT NOT NULL DEFAULT 0,
    active BIT NOT NULL DEFAULT 1,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT fk_products_category
        FOREIGN KEY (category_id) REFERENCES dbo.categories(id)
);

CREATE TABLE dbo.product_variants (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    product_id BIGINT NOT NULL,
    sku NVARCHAR(80) NOT NULL UNIQUE,
    color NVARCHAR(60) NOT NULL,
    size NVARCHAR(40) NOT NULL,
    stock_quantity INT NOT NULL DEFAULT 0,
    active BIT NOT NULL DEFAULT 1,
    CONSTRAINT fk_product_variants_product
        FOREIGN KEY (product_id) REFERENCES dbo.products(id),
    CONSTRAINT ck_product_variants_stock_quantity
        CHECK (stock_quantity >= 0)
);

CREATE INDEX ix_products_category_id ON dbo.products(category_id);
CREATE INDEX ix_products_active ON dbo.products(active);
CREATE INDEX ix_product_variants_product_id ON dbo.product_variants(product_id);
CREATE INDEX ix_product_variants_color ON dbo.product_variants(color);
CREATE INDEX ix_product_variants_size ON dbo.product_variants(size);
