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
```

Open each `.sql` file and run its contents in DBeaver. Do not type the file path into the SQL editor.

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
