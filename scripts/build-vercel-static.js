const fs = require("fs");
const path = require("path");

const root = path.resolve(__dirname, "..");
const templatePath = path.join(root, "src", "main", "resources", "templates", "home.html");
const staticPath = path.join(root, "src", "main", "resources", "static");
const distPath = path.join(root, "dist");

const categories = [
  {
    name: "Men",
    description: "Everyday layers with clean lines.",
    href: "/products?collection=men",
    cropClass: "crop-men"
  },
  {
    name: "Women",
    description: "Soft essentials for simple routines.",
    href: "/products?collection=women",
    cropClass: "crop-women"
  },
  {
    name: "Kids",
    description: "Easy pieces for school and weekends.",
    href: "/products?collection=kids",
    cropClass: "crop-kids"
  }
];

const essentials = [
  "Variant-first product detail pages for size and color accuracy",
  "Clear cart totals before checkout",
  "Inventory checks before every order",
  "Responsive pages built for quick browsing"
];

const products = [
  ["Air Cotton Tee", "T-shirt", "$19.90", "Cream, white", "S M L XL", "product-tee"],
  ["Light Utility Jacket", "Outerwear", "$59.90", "Navy", "S M L", "product-jacket"],
  ["Oxford Shirt", "Shirt", "$34.90", "White", "S M L XL", "product-shirt"],
  ["Soft Jersey Tee", "T-shirt", "$19.90", "Sage", "XS S M L", "product-sage"],
  ["Smart Ankle Pants", "Pants", "$39.90", "Black", "XS S M L XL", "product-pants"],
  ["Everyday Zip Hoodie", "Sweatshirt", "$49.90", "Grey", "S M L XL", "product-hoodie"],
  ["Linen Blend Shirt", "Shirt", "$34.90", "Natural", "S M L XL", "product-linen"],
  ["Utility Tote", "Accessories", "$14.90", "Red", "One size", "product-tote"]
].map(([name, category, price, colors, sizes, cropClass]) => ({
  name,
  category,
  price,
  colors,
  sizes,
  cropClass
}));

function escapeHtml(value) {
  return value
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

function thymeleafUrl(expression) {
  const match = expression.match(/^@\{(.+)\}$/);
  if (!match) {
    return expression;
  }

  const body = match[1];
  const paramStart = body.indexOf("(");
  if (paramStart === -1) {
    return body;
  }

  const base = body.slice(0, paramStart);
  const params = body
    .slice(paramStart + 1, -1)
    .split(",")
    .map((pair) => pair.trim())
    .filter(Boolean)
    .map((pair) => {
      const [key, rawValue] = pair.split("=");
      const value = rawValue.trim().replace(/^'|'$/g, "");
      return `${encodeURIComponent(key.trim())}=${encodeURIComponent(value)}`;
    })
    .join("&");

  return `${base}?${params}`;
}

function transformAttribute(value) {
  if (value.startsWith("|") && value.endsWith("|")) {
    return value
      .slice(1, -1)
      .replace(/@\{[^}]+\}/g, (token) => thymeleafUrl(token));
  }

  return thymeleafUrl(value);
}

function buildHtml() {
  let html = fs.readFileSync(templatePath, "utf8");

  html = html.replace(/\s+xmlns:th="[^"]+"/, "");
  html = html.replace(/\s+th:(href|src|srcset|action)="([^"]*)"/g, (_, attr, value) => {
    return ` ${attr}="${transformAttribute(value)}"`;
  });

  html = html
    .replace(
      /href="\/webjars\/bootstrap\/5\.3\.3\/css\/bootstrap\.min\.css"/g,
      'href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/css/bootstrap.min.css"'
    )
    .replace(
      /src="\/webjars\/bootstrap\/5\.3\.3\/js\/bootstrap\.bundle\.min\.js"/g,
      'src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.3/dist/js/bootstrap.bundle.min.js"'
    );

  html = html.replace(
    /<div class="category-grid">[\s\S]*?<\/div>\s*<\/section>/,
    `<div class="category-grid">
${categories
  .map(
    (category) => `            <a class="category-card reveal-card" href="${category.href}">
                <span class="category-image ${category.cropClass}">
                    <img src="/images/product-collage.png" alt="${escapeHtml(category.name)} minimalist apparel">
                </span>
                <span class="category-copy">
                    <strong>${escapeHtml(category.name)}</strong>
                    <small>${escapeHtml(category.description)}</small>
                </span>
            </a>`
  )
  .join("\n")}
        </div>
    </section>`
  );

  html = html.replace(
    /<ul>\s*<li th:each="essential : \$\{essentials\}" th:text="\$\{essential\}">Clear inventory<\/li>\s*<\/ul>/,
    `<ul>
${essentials.map((essential) => `                    <li>${escapeHtml(essential)}</li>`).join("\n")}
                </ul>`
  );

  html = html.replace(
    /<div class="product-grid">[\s\S]*?<\/div>\s*<\/section>\s*<section class="campaign-section/,
    `<div class="product-grid">
${products
  .map(
    (product) => `            <article class="product-card reveal-card">
                <a href="/products" class="product-image ${product.cropClass}">
                    <img src="/images/product-collage.png" alt="${escapeHtml(product.name)}">
                </a>
                <div class="product-meta">
                    <p>${escapeHtml(product.category)}</p>
                    <h3>${escapeHtml(product.name)}</h3>
                    <div class="product-line">
                        <span>${escapeHtml(product.price)}</span>
                        <span>${escapeHtml(product.colors)}</span>
                    </div>
                    <small>${escapeHtml(product.sizes)}</small>
                </div>
            </article>`
  )
  .join("\n")}
        </div>
    </section>

    <section class="campaign-section`
  );

  html = html.replace(/\s+th:[a-zA-Z-]+="[^"]*"/g, "");

  return html;
}

fs.rmSync(distPath, { recursive: true, force: true });
fs.mkdirSync(distPath, { recursive: true });
fs.cpSync(staticPath, distPath, { recursive: true });
fs.writeFileSync(path.join(distPath, "index.html"), buildHtml(), "utf8");

console.log(`Built Vercel static demo at ${path.relative(root, distPath)}`);
