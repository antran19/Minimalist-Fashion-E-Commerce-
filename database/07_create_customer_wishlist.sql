USE UMinimalistDB;

IF OBJECT_ID(N'dbo.customer_wishlist', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.customer_wishlist (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        user_id BIGINT NOT NULL,
        product_id BIGINT NOT NULL,
        created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        CONSTRAINT fk_customer_wishlist_user
            FOREIGN KEY (user_id) REFERENCES dbo.users(id),
        CONSTRAINT fk_customer_wishlist_product
            FOREIGN KEY (product_id) REFERENCES dbo.products(id),
        CONSTRAINT uq_customer_wishlist
            UNIQUE (user_id, product_id)
    );
END;

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_customer_wishlist_user_id' AND object_id = OBJECT_ID(N'dbo.customer_wishlist'))
BEGIN
    CREATE INDEX ix_customer_wishlist_user_id ON dbo.customer_wishlist(user_id);
END;
