USE UMinimalistDB;

IF OBJECT_ID(N'dbo.customer_cart_items', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.customer_cart_items (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        user_id BIGINT NOT NULL,
        sku NVARCHAR(80) NOT NULL,
        quantity INT NOT NULL,
        created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        updated_at DATETIME2 NULL,
        CONSTRAINT fk_customer_cart_items_user
            FOREIGN KEY (user_id) REFERENCES dbo.users(id),
        CONSTRAINT ck_customer_cart_items_quantity
            CHECK (quantity > 0)
    );
END;

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ux_customer_cart_items_user_sku' AND object_id = OBJECT_ID(N'dbo.customer_cart_items'))
BEGIN
    CREATE UNIQUE INDEX ux_customer_cart_items_user_sku ON dbo.customer_cart_items(user_id, sku);
END;
