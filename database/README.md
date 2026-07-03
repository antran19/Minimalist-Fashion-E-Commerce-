# Database Setup

This project uses SQL Server for the main application database.

## Local Docker SQL Server

Start SQL Server with Docker Compose:

```bash
docker compose up -d sqlserver
```

Default local credentials:

```text
Host: localhost
Port: 1433
Username: sa
Password: Sa123456@
```

The password can be changed with:

```bash
MSSQL_SA_PASSWORD='YourStrongPassword@123' docker compose up -d sqlserver
```

## Create Database And Seed Catalog

Run these scripts in DBeaver in order:

```text
database/01_create_database.sql
database/02_create_catalog_tables.sql
database/03_seed_catalog.sql
database/04_create_user_tables.sql
database/05_create_order_tables.sql
database/06_create_customer_addresses.sql
database/07_create_customer_wishlist.sql
database/08_create_customer_cart_items.sql
```

Open each `.sql` file and run its contents in DBeaver (or via `sqlcmd`). Do not type the file path into the SQL editor.
Scripts `06`, `07`, and `08` are additive and only create tables/indexes when missing; they do not drop existing customer data.

After that, connect DBeaver to:

```text
Database: UMinimalistDB
```

## Spring Boot Connection

The app reads these environment variables if provided:

```text
DB_URL
DB_USERNAME
DB_PASSWORD
```

If they are not provided, it uses local Docker defaults from `application.properties`.

## Demo Accounts

The application seeds these accounts on startup if the `users` table exists and the email is not present:

```text
Admin: admin@uminimalist.com / admin123
Customer: customer@uminimalist.com / customer123
```
