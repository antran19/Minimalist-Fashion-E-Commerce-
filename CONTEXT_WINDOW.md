# U-Minimalist Context Window

Use this file as the quick handoff note when switching between Windows and macOS.

## Project Snapshot

- Product: U-Minimalist, a minimalist fashion e-commerce website for HSF301 Java Frameworks.
- Stack in current repo: Java 17, Spring Boot 3.3.7, Spring MVC, Spring Data JPA, Thymeleaf, Bootstrap 5, Maven, SQL Server.
- Current app port: `9090`, configured in `src/main/resources/application.properties`.
- Current local DB: SQL Server in Docker on `localhost:1433`, database `UMinimalistDB`.
- Main documentation already reviewed: `PRD.md`, `BRD.md`, `PRODUCT.md`.

## What Was Implemented In This Pass

- Added a real product catalog flow at `/products`.
- Added server-side product filtering by:
  - search query: `q`
  - category/collection: `collection`
  - size: `size`
  - color: `color`
  - price range: `minPrice`, `maxPrice`
  - sort: `new`, `best`, `price-asc`, `price-desc`
- Added product detail page at `/products/{slug}`.
- Product detail page includes:
  - product image
  - category
  - name
  - price
  - color radio options
  - size radio options
  - quantity input
  - visible stock count
  - add-to-cart form placeholder posting to `/cart`
- Updated home page product cards to link to product detail pages.
- Expanded the in-memory product model to include slug, collection, numeric price, price label, colors, sizes, stock, new flag, and best-seller flag.
- Added responsive CSS for catalog, filters, empty state, product status labels, and product detail layout.
- Added `.m2repo/` to `.gitignore` because a temporary local Maven cache may appear during sandboxed builds.
- Added SQL Server/JPA foundation for catalog data.
- Added Docker Compose and SQL scripts for local SQL Server setup.
- Added JPA entities and repositories for Category, Product, and ProductVariant.
- Updated catalog service to read product/category/variant data from SQL Server instead of the in-memory list.
- Added session-based cart flow for guest shopping:
  - add variant to cart from product detail
  - view cart
  - update item quantity
  - remove item
  - shared header cart count

## Important Files

- `src/main/java/com/uminimalist/store/controller/HomeController.java`
  - Routes:
    - `/`
    - `/products`
    - `/products/{slug}`
- `src/main/java/com/uminimalist/store/controller/CartController.java`
  - Routes:
    - `GET /cart`
    - `POST /cart`
    - `POST /cart/update`
    - `POST /cart/remove`
- `src/main/java/com/uminimalist/store/controller/GlobalModelAttributes.java`
  - Adds `cartCount` to rendered pages.
- `src/main/java/com/uminimalist/store/service/LandingPageService.java`
  - Database-backed catalog service and filtering logic.
- `src/main/java/com/uminimalist/store/model/ProductView.java`
  - View model for listing/detail product data.
- `src/main/java/com/uminimalist/store/entity/Category.java`
- `src/main/java/com/uminimalist/store/entity/Product.java`
- `src/main/java/com/uminimalist/store/entity/ProductVariant.java`
- `src/main/java/com/uminimalist/store/repository/CategoryRepository.java`
- `src/main/java/com/uminimalist/store/repository/ProductRepository.java`
- `src/main/java/com/uminimalist/store/repository/ProductVariantRepository.java`
- `src/main/java/com/uminimalist/store/service/ShoppingCartService.java`
  - Session cart service. Later this should become database-backed per authenticated customer.
- `database/`
  - SQL Server scripts and setup notes.
- `src/main/resources/templates/home.html`
  - Landing page.
- `src/main/resources/templates/products.html`
  - Product listing and filters.
- `src/main/resources/templates/product-detail.html`
- `src/main/resources/templates/cart.html`
  - Product detail and variant selection UI.
- `src/main/resources/static/css/landing.css`
  - Shared visual system plus catalog/detail styling.

## How To Run

From the project root:

```bash
docker compose up -d sqlserver
mvn spring-boot:run -DskipTests
```

Then open:

```text
http://localhost:9090
http://localhost:9090/products
http://localhost:9090/products/air-cotton-tee
```

Run checks:

```bash
mvn test
```

## Database Setup

Default local Docker SQL Server credentials:

```text
Host: localhost
Port: 1433
Database: UMinimalistDB
Username: sa
Password: Sa123456@
```

Run SQL scripts in DBeaver in this order:

```text
database/01_create_database.sql
database/02_create_catalog_tables.sql
database/03_seed_catalog.sql
```

## Windows Notes

- Use Java 17.
- Use Maven from PowerShell or IntelliJ terminal.
- Same commands work:

```powershell
mvn spring-boot:run -DskipTests
mvn test
```

- If port `9090` is busy, change `server.port` in `src/main/resources/application.properties`.

## macOS Notes

- Use Java 17.
- Maven command is the same:

```bash
mvn spring-boot:run -DskipTests
mvn test
```

## Current Limitations

- Cart is session-based and works for guest browsing, but it is not persisted to database yet.
- Authentication, roles, cart persistence, checkout, order history, admin dashboard, and database entities are not implemented yet.
- The current implementation focuses on the Guest browsing flow from the PRD/BRD/PRODUCT documents.

## Recommended Next Steps

1. Run and verify the SQL scripts in DBeaver.
2. Start the Spring Boot app against `UMinimalistDB`.
3. Persist cart to database after authentication is added.
4. Add Spring Security login/register with Guest, Customer, and Admin roles.
5. Add Admin CRUD screens for products, variants, inventory, and orders.

## Verification Notes

- `mvn test` passed after Maven downloaded missing dependencies.
- Spring Boot app started successfully on port `9090`.
- The server process started during implementation was stopped after verification.
