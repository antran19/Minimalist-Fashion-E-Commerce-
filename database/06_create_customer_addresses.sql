USE UMinimalistDB;

IF OBJECT_ID(N'dbo.customer_addresses', N'U') IS NULL
BEGIN
    CREATE TABLE dbo.customer_addresses (
        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
        user_id BIGINT NOT NULL,
        recipient_name NVARCHAR(120) NOT NULL,
        phone NVARCHAR(20) NOT NULL,
        address_line NVARCHAR(255) NOT NULL,
        district NVARCHAR(120) NOT NULL,
        city NVARCHAR(120) NOT NULL,
        is_default BIT NOT NULL DEFAULT 1,
        created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
        updated_at DATETIME2 NULL,
        CONSTRAINT fk_customer_addresses_user
            FOREIGN KEY (user_id) REFERENCES dbo.users(id)
    );
END;

IF NOT EXISTS (SELECT 1 FROM sys.indexes WHERE name = N'ix_customer_addresses_user_id' AND object_id = OBJECT_ID(N'dbo.customer_addresses'))
BEGIN
    CREATE INDEX ix_customer_addresses_user_id ON dbo.customer_addresses(user_id);
END;
