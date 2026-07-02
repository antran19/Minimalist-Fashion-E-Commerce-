# U-Minimalist Context Window

Use this file as the quick handoff note when switching between Windows and macOS.

## Project Snapshot

- Product: U-Minimalist, a minimalist fashion e-commerce website for HSF301 Java Frameworks.
- Stack in current repo: Java 17, Spring Boot 3.3.7, Spring MVC, Thymeleaf, Bootstrap 5, Maven.
- Current app port: `9090`, configured in `src/main/resources/application.properties`.
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

## Important Files

- `src/main/java/com/uminimalist/store/controller/HomeController.java`
  - Routes:
    - `/`
    - `/products`
    - `/products/{slug}`
- `src/main/java/com/uminimalist/store/service/LandingPageService.java`
  - Temporary in-memory product data and filtering logic.
  - Later this should be replaced by repository/database queries.
- `src/main/java/com/uminimalist/store/model/ProductView.java`
  - View model for listing/detail product data.
- `src/main/resources/templates/home.html`
  - Landing page.
- `src/main/resources/templates/products.html`
  - Product listing and filters.
- `src/main/resources/templates/product-detail.html`
  - Product detail and variant selection UI.
- `src/main/resources/static/css/landing.css`
  - Shared visual system plus catalog/detail styling.

## How To Run

From the project root:

```bash
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

- Product data is still in memory, not in SQL Server.
- `/cart` is only a placeholder target from the product detail form.
- Authentication, roles, cart persistence, checkout, order history, admin dashboard, and database entities are not implemented yet.
- The current implementation focuses on the Guest browsing flow from the PRD/BRD/PRODUCT documents.

## Recommended Next Steps

1. Add domain entities for Category, Product, ProductVariant, Inventory, User, Cart, Order, and OrderItem.
2. Add Spring Data JPA repositories and SQL Server configuration.
3. Move product data from `LandingPageService` into database-backed services.
4. Implement cart add/update/remove flow.
5. Add Spring Security login/register with Guest, Customer, and Admin roles.
6. Add Admin CRUD screens for products, variants, inventory, and orders.

## Verification Notes

- `mvn test` passed after Maven downloaded missing dependencies.
- Spring Boot app started successfully on port `9090`.
- The server process started during implementation was stopped after verification.
