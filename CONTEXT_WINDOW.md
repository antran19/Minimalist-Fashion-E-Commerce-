# U-Minimalist Context Window

Use this file as the quick handoff note when switching machines or resuming a Codex thread.

## Project Snapshot

- Product: U-Minimalist, a minimalist fashion e-commerce website for HSF301 Java Frameworks.
- Direction: local-first Spring Boot demo, inspired by UNIQLO-style clean retail UI.
- Stack: Java 17, Spring Boot 3.3.7, Spring MVC, Spring Security, Spring Data JPA, Thymeleaf, Bootstrap 5, Maven, SQL Server.
- Current app port: `9090`, configured in `src/main/resources/application.properties`.
- Current local DB: SQL Server in Docker on `localhost:1433`, database `UMinimalistDB`.
- Main documentation already reviewed: `PRD.md`, `BRD.md`, `PRODUCT.md`.
- Deployment note: Vercel/static deploy was canceled. Keep the project running locally only.

## Current Demo Accounts

```text
Customer: customer@uminimalist.com / customer123
Admin:    admin@uminimalist.com / admin123
```

Seed/demo users are initialized from `src/main/java/com/uminimalist/store/config/DataInitializer.java`.

## Implemented Features

### Landing Page And Catalog

- Landing page at `/` with full-viewport fashion hero, fixed transparent header, GSAP reveal animations, category tiles, editorial campaign section, new arrivals, and footer.
- Product catalog at `/products`.
- Server-side product filtering by:
  - search query: `q`
  - category/collection: `collection`
  - size: `size`
  - color: `color`
  - price range: `minPrice`, `maxPrice`
  - sort: `new`, `best`, `price-asc`, `price-desc`
- Product detail page at `/products/{slug}`.
- Product detail page includes image, category, name, price, color options, size options, quantity input, visible stock count, and add-to-cart.
- Product cards link to detail pages.
- Catalog data is database-backed through SQL Server/JPA.

### Product Image Polish

- Product cards now support a real `imagePath` instead of relying only on CSS background crops from collage images.
- New generated product assets were copied into:
  - `src/main/resources/static/images/products/air-cotton-tee.png`
  - `src/main/resources/static/images/products/light-utility-jacket.png`
  - `src/main/resources/static/images/products/everyday-zip-hoodie.png`
  - `src/main/resources/static/images/products/smart-ankle-pants.png`
  - `src/main/resources/static/images/products/soft-jersey-tee.png`
- `LandingPageService` maps important slugs to these product-specific images.
- `ShoppingCartService` also maps cart item images to the same assets.
- CSS class `.product-image-real` renders images with `object-fit: contain` on a clean off-white product surface.
- Home `New arrivals` now excludes kids products and accessories so the section does not show the child model/cardigan in the adult landing flow.
- Existing collage/campaign images are still used as fallback or category/editorial imagery.

### Cart

- Session-based cart flow:
  - add variant to cart from product detail
  - view cart
  - update item quantity
  - remove item
  - shared header cart count
- Cart checkout button posts to `/checkout`.
- Checkout currently creates a demo confirmation and clears the session cart.

### Authentication And Customer

- Spring Security login/register is implemented.
- Customer account page at `/account`.
- `/account` shows profile status, current cart, and demo order activity.
- Customer checkout route:
  - `POST /checkout`
  - requires a non-empty cart
  - clears cart and flashes a demo confirmation
- Auth forms include CSRF tokens.

### Admin

- Admin dashboard at `/admin/dashboard`.
- Dashboard stats include user count, product count, variant count, and low-stock variant count.
- Admin can:
  - toggle product active/inactive
  - update variant stock
  - toggle variant active/inactive
  - toggle user active/inactive
- Admin cannot disable their own account.
- Admin forms include CSRF tokens.

## Important Files

- `src/main/java/com/uminimalist/store/controller/HomeController.java`
  - `/`
  - `/products`
  - `/products/{slug}`
- `src/main/java/com/uminimalist/store/controller/CartController.java`
  - `GET /cart`
  - `POST /cart`
  - `POST /cart/update`
  - `POST /cart/remove`
- `src/main/java/com/uminimalist/store/controller/AuthController.java`
  - login/register routes.
- `src/main/java/com/uminimalist/store/controller/CustomerController.java`
  - `GET /account`
  - `POST /checkout`
- `src/main/java/com/uminimalist/store/controller/AdminController.java`
  - admin dashboard and admin POST actions.
- `src/main/java/com/uminimalist/store/controller/GlobalModelAttributes.java`
  - Adds `cartCount` to rendered pages.
- `src/main/java/com/uminimalist/store/config/SecurityConfig.java`
  - Security rules, login/logout, role routes.
- `src/main/java/com/uminimalist/store/config/DataInitializer.java`
  - Demo users.
- `src/main/java/com/uminimalist/store/service/LandingPageService.java`
  - Database-backed catalog filtering and product image mapping.
- `src/main/java/com/uminimalist/store/service/ShoppingCartService.java`
  - Session cart service and cart item image mapping.
- `src/main/java/com/uminimalist/store/service/AdminCatalogService.java`
  - Admin product/user/inventory actions.
- `src/main/java/com/uminimalist/store/service/CustomUserDetailsService.java`
  - Spring Security user lookup.
- `src/main/java/com/uminimalist/store/model/ProductView.java`
  - Listing/detail product view model, includes `imagePath`.
- `src/main/java/com/uminimalist/store/model/CartItemView.java`
  - Cart item view model, includes `imagePath`.
- `src/main/java/com/uminimalist/store/model/UserRegistrationDto.java`
  - Registration validation DTO.
- `src/main/java/com/uminimalist/store/entity/Category.java`
- `src/main/java/com/uminimalist/store/entity/Product.java`
- `src/main/java/com/uminimalist/store/entity/ProductVariant.java`
- `src/main/java/com/uminimalist/store/entity/User.java`
- `src/main/java/com/uminimalist/store/repository/CategoryRepository.java`
- `src/main/java/com/uminimalist/store/repository/ProductRepository.java`
- `src/main/java/com/uminimalist/store/repository/ProductVariantRepository.java`
- `src/main/java/com/uminimalist/store/repository/UserRepository.java`
- `src/main/resources/templates/home.html`
  - Landing page.
- `src/main/resources/templates/products.html`
  - Catalog listing and filters.
- `src/main/resources/templates/product-detail.html`
- `src/main/resources/templates/cart.html`
- `src/main/resources/templates/account.html`
- `src/main/resources/templates/login.html`
- `src/main/resources/templates/register.html`
- `src/main/resources/templates/admin/dashboard.html`
- `src/main/resources/static/css/landing.css`
  - Shared visual system, catalog/detail/cart/account/admin styling.
- `src/main/resources/static/js/landing.js`
  - GSAP landing motion.
- `src/main/resources/static/vendor/gsap/`
  - Local GSAP assets.
- `database/`
  - SQL Server scripts and setup notes.

## Database Setup

Default local Docker SQL Server credentials:

```text
Host: localhost
Port: 1433
Database: UMinimalistDB
Username: sa
Password: Sa123456@
```

Run SQL scripts in DBeaver in this order when resetting catalog/user tables:

```text
database/01_create_database.sql
database/02_create_catalog_tables.sql
database/03_seed_catalog.sql
database/04_create_user_tables.sql
```

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
http://localhost:9090/account
http://localhost:9090/admin/dashboard
```

Run checks:

```bash
mvn test
```

## Verification Notes

- `mvn test` passed after the latest product image/model updates.
- Customer login was previously verified in browser and `/account` rendered correctly.
- Admin login was previously verified in browser and `/admin/dashboard` rendered correctly.
- Latest browser refresh after image polish was interrupted by the user before visual verification finished. If the running Spring Boot process does not have devtools hot reload, restart the app to see Java-side changes such as New arrivals filtering.

## Current Limitations

- Cart is still session-based and not persisted to database per authenticated customer.
- Checkout is demo-only and does not create real order/order item records yet.
- Order history on `/account` is a placeholder/demo activity section.
- Admin product management is toggle/stock focused, not full create/edit/delete product CRUD yet.
- Product image mapping is currently slug-based in services, not stored in the database schema.
- Mobile was intentionally deprioritized per user request; desktop demo is the priority.

## Recommended Next Steps

1. Restart the local Spring Boot app and visually verify `/` and `/products` after image polish.
2. Add persistent Order and OrderItem entities for real checkout history.
3. Move product `imagePath` into the database schema instead of service switch statements.
4. Add admin create/edit product forms if more demo depth is needed.
5. Add customer persisted cart after authentication.
