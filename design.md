# U-Minimalist Design Guidelines

This file defines the shared design rules for the U-Minimalist project. Use it before creating or editing any screen so the whole website stays visually consistent across macOS and Windows development.

## Design Direction

U-Minimalist should feel clean, direct, practical, and fashion-focused. The visual style is inspired by minimalist everyday apparel: strong whitespace, simple grids, clear product imagery, restrained color, and easy scanning.

The interface should prioritize shopping tasks:

- find products quickly
- compare product variants
- see size, color, price, and stock clearly
- move from product listing to product detail to cart with little friction
- keep admin screens dense, readable, and operational

Avoid decorative complexity. The product, price, stock, and user action should always be easier to notice than the styling.

## Visual Principles

- Use simple, editorial layouts with generous spacing.
- Prefer full-width sections and clean grids over nested cards.
- Use cards only for repeated items such as product cards, category cards, admin rows, modals, or compact panels.
- Keep border radius small: `6px` by default.
- Use thin borders and subtle background shifts instead of heavy shadows.
- Avoid large gradients, decorative blobs, bokeh, or visual noise.
- Product imagery should be large, clear, and inspectable.
- Buttons, filters, forms, and tables should feel practical rather than promotional.

## Color System

Use the existing CSS variables in `src/main/resources/static/css/landing.css` as the source of truth:

```css
:root {
    --page: #ffffff;
    --surface: #f5f5f3;
    --surface-strong: #ededeb;
    --ink: #111111;
    --muted: #696967;
    --line: #e3e3df;
    --accent: #d71920;
    --accent-dark: #b51218;
    --radius: 6px;
    --font-main: "Helvetica Neue", Arial, "Noto Sans", sans-serif;
}
```

Usage rules:

- Page background: `--page`
- Soft section background: `--surface`
- Product/image placeholder background: `--surface-strong`
- Main text: `--ink`
- Secondary text: `--muted`
- Borders: `--line`
- Primary action: `--accent`
- Primary action hover: `--accent-dark`

Do not introduce a new dominant palette without updating this file first.

## Typography

Font family:

```css
font-family: "Helvetica Neue", Arial, "Noto Sans", sans-serif;
```

Heading rules:

- Use bold headings with tight line height.
- Large page headings should use strong weight around `800`.
- Use negative letter spacing only where already established in CSS.
- Do not scale font size directly with viewport width outside `clamp()`.
- Keep labels uppercase only for small utility labels, filters, and form legends.

Body text rules:

- Use readable line height around `1.45` to `1.65`.
- Secondary text should use `--muted`.
- Avoid long explanatory paragraphs inside the product shopping flow.

## Layout System

Use `.page-shell` for page width:

```css
.page-shell {
    width: min(100% - 48px, 1440px);
    margin-inline: auto;
}
```

Header pages may use the wider header shell already defined:

```css
.site-header .page-shell {
    width: min(100% - 72px, 1840px);
}
```

Layout rules:

- Desktop product grids should generally use 3 or 4 columns.
- Tablet product grids should reduce to 2 columns.
- Mobile product grids should reduce to 1 column.
- Admin screens should favor tables, rows, filters, and compact panels over large marketing sections.
- Keep fixed-format elements stable with `aspect-ratio`, grid columns, and min/max widths.

## Header And Navigation

Header should stay simple:

- Brand on the left: `U-Minimalist`
- Main category links: Men, Women, Kids, New arrivals, Best sellers
- Utility links: Search, Account, Cart
- Cart count uses the red accent circle.

Do not add extra decorative nav items, badges, search bars, or icons unless the screen workflow needs them.

## Buttons

Base button class:

```html
class="btn btn-primary retail-btn"
```

Button rules:

- Primary buttons use red accent.
- Secondary buttons can use `.outline`.
- Button radius stays `6px`.
- Button text should be short and action-based: `Shop now`, `Apply`, `Reset`, `Add to cart`, `Checkout`.
- Do not use oversized hero-style buttons inside dense forms or admin panels.

## Forms And Filters

Filters should be compact and direct:

- Use labels above inputs.
- Use selects for fixed option sets.
- Use number inputs for price and quantity.
- Use radio-style option buttons for product size and color choices.
- Preserve selected filter state in the UI when query parameters are active.
- Empty states should explain what happened and offer one clear reset action.

## Product Cards

Product cards should show:

- product image
- category
- product name
- price
- color label
- size label
- stock when relevant
- `New` or `Best` status only when useful

Product card rules:

- Image first, metadata below.
- Product image should use a stable `aspect-ratio`.
- Product names should be short and readable.
- Avoid long descriptions inside product cards.
- Product cards should link to `/products/{slug}`.

## Product Detail Pages

Product detail pages should include:

- large product image
- category
- product name
- price
- short product description
- color selector
- size selector
- quantity selector
- visible stock note
- add-to-cart action

Variant selection must be visually obvious. Size and color choices should not look like plain text.

## Admin Pages

Admin screens should feel operational:

- Use compact headers.
- Use filters and search near the top.
- Use tables or dense rows for products, variants, orders, inventory, and users.
- Use clear status labels for orders and stock.
- Use modals or dedicated forms for create/edit actions.
- Avoid marketing-style hero sections in admin.

Admin actions should be direct:

- Add product
- Edit
- Update stock
- Change status
- View order
- Save
- Cancel

## Responsive Rules

Every screen must work at:

- desktop width
- tablet width
- mobile width

Mobile rules:

- Navigation collapses with Bootstrap.
- Filter panels should stack above product results.
- Product grids become one column.
- Detail pages stack image above form.
- Text must not overflow buttons, cards, filters, or nav items.
- Avoid horizontal scrolling unless showing a deliberate data table.

## Accessibility Rules

- Use semantic headings in order.
- Every form input needs a visible label.
- Buttons must describe the action.
- Images need meaningful `alt` text.
- Links and buttons need visible focus states through browser defaults or CSS.
- Motion should respect `prefers-reduced-motion`.

## Thymeleaf Rules

Use Thymeleaf for server-rendered UI state:

- Use `th:text` for dynamic text.
- Use `th:href` for routes.
- Use `th:each` for repeated products, filters, rows, and options.
- Use `th:selected` and `th:checked` for selected form state.
- Avoid duplicating static HTML for every product or row.

Current important routes:

- `/`
- `/products`
- `/products/{slug}`

## CSS Rules

- Reuse existing classes where possible.
- Add new CSS to `landing.css` until the project grows enough to split files.
- Prefer shared component classes over one-off page-specific styling.
- Keep colors tied to CSS variables.
- Keep spacing, radius, borders, and typography consistent with existing sections.
- Do not use inline styles in templates.

## Copywriting Tone

Copy should be short, clear, and practical.

Good examples:

- `Everyday essentials`
- `Inventory visible before checkout`
- `No matching styles`
- `View all products`
- `Add to cart`

Avoid:

- long marketing claims
- vague hype
- unnecessary feature explanations inside the UI
- decorative badges or labels that do not help shopping

## Before Adding A New Page

Check this list:

1. Does the page follow the same header, shell width, colors, and typography?
2. Is the main user action obvious?
3. Are product, price, variant, stock, or order details easy to scan?
4. Does it work on mobile?
5. Are forms labeled and accessible?
6. Are repeated elements rendered with Thymeleaf loops?
7. Does the page avoid unrelated decorative styling?

## Current Design Source Files

- `src/main/resources/static/css/landing.css`
- `src/main/resources/templates/home.html`
- `src/main/resources/templates/products.html`
- `src/main/resources/templates/product-detail.html`

Update this file whenever the shared visual system changes.
