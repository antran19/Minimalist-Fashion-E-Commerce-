USE UMinimalistDB;

IF OBJECT_ID(N'dbo.order_items', N'U') IS NOT NULL
BEGIN
    DROP TABLE dbo.order_items;
END;

IF OBJECT_ID(N'dbo.orders', N'U') IS NOT NULL
BEGIN
    DROP TABLE dbo.orders;
END;

CREATE TABLE dbo.orders (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    order_code NVARCHAR(40) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    customer_name NVARCHAR(120) NOT NULL,
    customer_email NVARCHAR(120) NOT NULL,
    status NVARCHAR(30) NOT NULL DEFAULT 'PLACED',
    item_count INT NOT NULL,
    total_amount DECIMAL(10, 2) NOT NULL,
    created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
    CONSTRAINT fk_orders_user
        FOREIGN KEY (user_id) REFERENCES dbo.users(id)
);

CREATE TABLE dbo.order_items (
    id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_variant_id BIGINT NOT NULL,
    product_name NVARCHAR(180) NOT NULL,
    sku NVARCHAR(80) NOT NULL,
    color NVARCHAR(60) NOT NULL,
    size NVARCHAR(40) NOT NULL,
    quantity INT NOT NULL,
    unit_price DECIMAL(10, 2) NOT NULL,
    line_total DECIMAL(10, 2) NOT NULL,
    CONSTRAINT fk_order_items_order
        FOREIGN KEY (order_id) REFERENCES dbo.orders(id),
    CONSTRAINT fk_order_items_variant
        FOREIGN KEY (product_variant_id) REFERENCES dbo.product_variants(id),
    CONSTRAINT ck_order_items_quantity
        CHECK (quantity > 0)
);

CREATE INDEX ix_orders_customer_email ON dbo.orders(customer_email);
CREATE INDEX ix_orders_created_at ON dbo.orders(created_at);
CREATE INDEX ix_order_items_order_id ON dbo.order_items(order_id);
