package com.uminimalist.store.service;

import com.uminimalist.store.entity.ProductVariant;
import com.uminimalist.store.entity.User;
import com.uminimalist.store.model.CartItemView;
import com.uminimalist.store.model.CartView;
import com.uminimalist.store.model.CustomerAddressView;
import com.uminimalist.store.model.OrderItemView;
import com.uminimalist.store.model.OrderSummaryView;
import com.uminimalist.store.model.PaginatedOrders;
import com.uminimalist.store.repository.ProductVariantRepository;
import com.uminimalist.store.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;
    private final ProductVariantRepository productVariantRepository;
    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.US);

    public OrderService(JdbcTemplate jdbcTemplate,
                        UserRepository userRepository,
                        ProductVariantRepository productVariantRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRepository = userRepository;
        this.productVariantRepository = productVariantRepository;
    }

    @PostConstruct
    public void ensureOrderTables() {
        jdbcTemplate.execute("""
                IF OBJECT_ID(N'dbo.order_items', N'U') IS NULL
                BEGIN
                    CREATE TABLE dbo.order_items (
                        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
                        order_id BIGINT NOT NULL,
                        product_variant_id BIGINT NOT NULL,
                        product_name NVARCHAR(180) NOT NULL,
                        sku NVARCHAR(80) NOT NULL,
                        color NVARCHAR(60) NOT NULL,
                        size NVARCHAR(40) NOT NULL,
                        quantity INT NOT NULL,
                        unit_price DECIMAL(10, 2) NOT NULL,
                        line_total DECIMAL(10, 2) NOT NULL
                    );
                END
                """);
        jdbcTemplate.execute("""
                IF OBJECT_ID(N'dbo.orders', N'U') IS NULL
                BEGIN
                    CREATE TABLE dbo.orders (
                        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
                        order_code NVARCHAR(40) NOT NULL UNIQUE,
                        user_id BIGINT NOT NULL,
                        customer_name NVARCHAR(120) NOT NULL,
                        customer_email NVARCHAR(120) NOT NULL,
                        shipping_name NVARCHAR(120) NULL,
                        shipping_phone NVARCHAR(20) NULL,
                        shipping_address_line NVARCHAR(255) NULL,
                        shipping_district NVARCHAR(120) NULL,
                        shipping_city NVARCHAR(120) NULL,
                        status NVARCHAR(30) NOT NULL DEFAULT 'PLACED',
                        item_count INT NOT NULL,
                        total_amount DECIMAL(10, 2) NOT NULL,
                        created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME()
                    );
                END
                """);
        addOrderColumnIfMissing("shipping_name", "NVARCHAR(120) NULL");
        addOrderColumnIfMissing("shipping_phone", "NVARCHAR(20) NULL");
        addOrderColumnIfMissing("shipping_address_line", "NVARCHAR(255) NULL");
        addOrderColumnIfMissing("shipping_district", "NVARCHAR(120) NULL");
        addOrderColumnIfMissing("shipping_city", "NVARCHAR(120) NULL");
    }

    @Transactional
    public OrderSummaryView placeOrder(String customerEmail, CartView cart, CustomerAddressView shippingAddress) {
        if (cart == null || cart.isEmpty()) {
            throw new IllegalArgumentException("Your cart is empty.");
        }
        if (shippingAddress == null || !shippingAddress.isComplete()) {
            throw new IllegalArgumentException("Please add a complete shipping address before placing the order.");
        }

        User user = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Customer account not found."));
        List<CartItemView> cartItems = cart.items();
        Map<String, ProductVariant> variants = productVariantRepository.findBySkuInAndActiveTrueAndProductActiveTrue(
                        cartItems.stream().map(CartItemView::sku).toList())
                .stream()
                .collect(Collectors.toMap(ProductVariant::getSku, Function.identity()));

        List<OrderLine> lines = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        int itemCount = 0;

        for (CartItemView item : cartItems) {
            ProductVariant variant = Optional.ofNullable(variants.get(item.sku()))
                    .orElseThrow(() -> new IllegalArgumentException(item.productName() + " is no longer available."));
            if (item.quantity() > variant.getStockQuantity()) {
                throw new IllegalArgumentException(item.productName() + " only has " + variant.getStockQuantity() + " item(s) left.");
            }

            BigDecimal unitPrice = variant.getProduct().getBasePrice();
            BigDecimal lineTotal = unitPrice.multiply(BigDecimal.valueOf(item.quantity()));
            lines.add(new OrderLine(item, variant, unitPrice, lineTotal));
            total = total.add(lineTotal);
            itemCount += item.quantity();
            variant.setStockQuantity(variant.getStockQuantity() - item.quantity());
        }

        productVariantRepository.saveAll(lines.stream().map(OrderLine::variant).toList());

        String orderCode = nextOrderCode(user.getId());
        Long orderId = insertOrder(orderCode, user, shippingAddress, itemCount, total);
        insertOrderItems(orderId, lines);

        return findOrder(orderId)
                .orElseThrow(() -> new IllegalStateException("Order was created but could not be loaded."));
    }

    public List<OrderSummaryView> findOrdersForCustomer(String customerEmail) {
        ensureOrderTables();
        return loadOrders("""
                SELECT TOP 12 id, order_code, customer_name, customer_email,
                    shipping_name, shipping_phone, shipping_address_line, shipping_district, shipping_city,
                    created_at, status, item_count, total_amount
                FROM dbo.orders
                WHERE customer_email = ?
                ORDER BY created_at DESC, id DESC
                """, customerEmail);
    }

    public PaginatedOrders findOrdersForCustomerPaginated(String customerEmail, String status, int page, int size) {
        ensureOrderTables();
        
        boolean hasStatus = status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status);
        
        String countSql = hasStatus 
                ? "SELECT COUNT(*) FROM dbo.orders WHERE customer_email = ? AND status = ?"
                : "SELECT COUNT(*) FROM dbo.orders WHERE customer_email = ?";
        
        Object[] countArgs = hasStatus ? new Object[]{customerEmail, status} : new Object[]{customerEmail};
        
        Integer totalCount = jdbcTemplate.queryForObject(countSql, Integer.class, countArgs);
        if (totalCount == null) totalCount = 0;
        
        int totalPages = (int) Math.ceil((double) totalCount / size);
        if (totalPages == 0) totalPages = 1;
        
        int offset = (page - 1) * size;
        if (offset < 0) offset = 0;
        
        String sql = hasStatus
                ? """
                  SELECT id, order_code, customer_name, customer_email,
                      shipping_name, shipping_phone, shipping_address_line, shipping_district, shipping_city,
                      created_at, status, item_count, total_amount
                  FROM dbo.orders
                  WHERE customer_email = ? AND status = ?
                  ORDER BY created_at DESC, id DESC
                  OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                  """
                : """
                  SELECT id, order_code, customer_name, customer_email,
                      shipping_name, shipping_phone, shipping_address_line, shipping_district, shipping_city,
                      created_at, status, item_count, total_amount
                  FROM dbo.orders
                  WHERE customer_email = ?
                  ORDER BY created_at DESC, id DESC
                  OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                  """;
                  
        Object[] args = hasStatus 
                ? new Object[]{customerEmail, status, offset, size}
                : new Object[]{customerEmail, offset, size};
                
        List<OrderSummaryView> orders = loadOrders(sql, args);
        
        return new PaginatedOrders(orders, page, totalPages, totalCount, status != null ? status : "ALL");
    }

    public Optional<OrderSummaryView> findOrderForCustomer(String customerEmail, String orderCode) {
        ensureOrderTables();
        return loadOrders("""
                SELECT id, order_code, customer_name, customer_email,
                    shipping_name, shipping_phone, shipping_address_line, shipping_district, shipping_city,
                    created_at, status, item_count, total_amount
                FROM dbo.orders
                WHERE customer_email = ? AND order_code = ?
                """, customerEmail, orderCode).stream().findFirst();
    }

    @Transactional
    public void cancelOrderForCustomer(String customerEmail, String orderCode) {
        ensureOrderTables();
        List<OrderStatusRow> rows = jdbcTemplate.query("""
                        SELECT id, status
                        FROM dbo.orders
                        WHERE customer_email = ? AND order_code = ?
                        """,
                (rs, rowNum) -> new OrderStatusRow(rs.getLong("id"), rs.getString("status")),
                customerEmail,
                orderCode);

        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Order not found.");
        }

        OrderStatusRow order = rows.get(0);
        if (!"PLACED".equalsIgnoreCase(order.status())) {
            throw new IllegalArgumentException("Only placed orders can be cancelled.");
        }

        List<OrderStockRow> stockRows = jdbcTemplate.query("""
                        SELECT product_variant_id, quantity
                        FROM dbo.order_items
                        WHERE order_id = ?
                        """,
                (rs, rowNum) -> new OrderStockRow(rs.getLong("product_variant_id"), rs.getInt("quantity")),
                order.id());

        for (OrderStockRow stockRow : stockRows) {
            jdbcTemplate.update("""
                    UPDATE dbo.product_variants
                    SET stock_quantity = stock_quantity + ?
                    WHERE id = ?
                    """, stockRow.quantity(), stockRow.productVariantId());
        }

        jdbcTemplate.update("""
                UPDATE dbo.orders
                SET status = 'CANCELLED'
                WHERE id = ?
                """, order.id());
    }

    public List<OrderSummaryView> findRecentOrders() {
        ensureOrderTables();
        return loadOrders("""
                SELECT TOP 20 id, order_code, customer_name, customer_email,
                    shipping_name, shipping_phone, shipping_address_line, shipping_district, shipping_city,
                    created_at, status, item_count, total_amount
                FROM dbo.orders
                ORDER BY created_at DESC, id DESC
                """);
    }

    public List<OrderSummaryView> findOrdersByStatus(String status) {
        ensureOrderTables();
        if (status == null || status.isBlank() || "ALL".equalsIgnoreCase(status)) {
            return findRecentOrders();
        }
        return loadOrders("""
                SELECT TOP 50 id, order_code, customer_name, customer_email,
                    shipping_name, shipping_phone, shipping_address_line, shipping_district, shipping_city,
                    created_at, status, item_count, total_amount
                FROM dbo.orders
                WHERE status = ?
                ORDER BY created_at DESC, id DESC
                """, status.toUpperCase());
    }

    public PaginatedOrders findPaginatedOrders(int page, int size, String query, String status) {
        ensureOrderTables();
        int safePage = Math.max(0, page);
        int offset = safePage * size;

        StringBuilder whereSql = new StringBuilder(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            whereSql.append(" AND status = ? ");
            params.add(status.toUpperCase());
        }

        if (query != null && !query.isBlank()) {
            whereSql.append(" AND (LOWER(order_code) LIKE ? OR LOWER(customer_name) LIKE ? OR LOWER(customer_email) LIKE ?) ");
            String qParam = "%" + query.trim().toLowerCase() + "%";
            params.add(qParam);
            params.add(qParam);
            params.add(qParam);
        }

        String countSql = "SELECT COUNT(*) FROM dbo.orders " + whereSql;
        Integer totalCountObj = jdbcTemplate.queryForObject(countSql, Integer.class, params.toArray());
        long totalItems = totalCountObj == null ? 0 : totalCountObj;
        int totalPages = (int) Math.ceil((double) totalItems / size);
        if (totalPages < 1) totalPages = 1;

        String selectSql = "SELECT id, order_code, customer_name, customer_email, " +
                "shipping_name, shipping_phone, shipping_address_line, shipping_district, shipping_city, " +
                "created_at, status, item_count, total_amount " +
                "FROM dbo.orders " + whereSql +
                "ORDER BY created_at DESC, id DESC " +
                "OFFSET " + offset + " ROWS FETCH NEXT " + size + " ROWS ONLY";

        List<OrderSummaryView> orders = loadOrders(selectSql, params.toArray());
        return new PaginatedOrders(orders, safePage + 1, totalPages, totalItems, status == null ? "ALL" : status);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, String newStatus) {
        ensureOrderTables();
        List<String> validStatuses = List.of("PLACED", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED");
        if (!validStatuses.contains(newStatus.toUpperCase())) {
            throw new IllegalArgumentException("Invalid order status: " + newStatus);
        }
        jdbcTemplate.update("""
                UPDATE dbo.orders
                SET status = ?
                WHERE id = ?
                """, newStatus.toUpperCase(), orderId);
    }

    public int countOrders() {
        ensureOrderTables();
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM dbo.orders", Integer.class);
        return count == null ? 0 : count;
    }

    public String totalRevenueLabel() {
        ensureOrderTables();
        BigDecimal total = jdbcTemplate.queryForObject("SELECT COALESCE(SUM(total_amount), 0) FROM dbo.orders WHERE status = 'DELIVERED'", BigDecimal.class);
        return currencyFormat.format(total == null ? BigDecimal.ZERO : total);
    }

    private Long insertOrder(String orderCode, User user, CustomerAddressView shippingAddress, int itemCount, BigDecimal total) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO dbo.orders
                        (order_code, user_id, customer_name, customer_email,
                         shipping_name, shipping_phone, shipping_address_line, shipping_district, shipping_city,
                         status, item_count, total_amount)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                    """, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, orderCode);
            ps.setLong(2, user.getId());
            ps.setString(3, user.getFullName());
            ps.setString(4, user.getEmail());
            ps.setString(5, shippingAddress.recipientName());
            ps.setString(6, shippingAddress.phone());
            ps.setString(7, shippingAddress.addressLine());
            ps.setString(8, shippingAddress.district());
            ps.setString(9, shippingAddress.city());
            ps.setString(10, "PLACED");
            ps.setInt(11, itemCount);
            ps.setBigDecimal(12, total);
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key == null) {
            throw new IllegalStateException("Could not create order.");
        }
        return key.longValue();
    }

    private void insertOrderItems(Long orderId, List<OrderLine> lines) {
        jdbcTemplate.batchUpdate("""
                INSERT INTO dbo.order_items
                    (order_id, product_variant_id, product_name, sku, color, size, quantity, unit_price, line_total)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, lines, 20, (ps, line) -> {
            CartItemView item = line.item();
            ProductVariant variant = line.variant();
            ps.setLong(1, orderId);
            ps.setLong(2, variant.getId());
            ps.setString(3, item.productName());
            ps.setString(4, item.sku());
            ps.setString(5, item.color());
            ps.setString(6, item.size());
            ps.setInt(7, item.quantity());
            ps.setBigDecimal(8, line.unitPrice());
            ps.setBigDecimal(9, line.lineTotal());
        });
    }

    private Optional<OrderSummaryView> findOrder(Long orderId) {
        List<OrderSummaryView> orders = loadOrders("""
                SELECT id, order_code, customer_name, customer_email,
                    shipping_name, shipping_phone, shipping_address_line, shipping_district, shipping_city,
                    created_at, status, item_count, total_amount
                FROM dbo.orders
                WHERE id = ?
                """, orderId);
        return orders.stream().findFirst();
    }

    private List<OrderSummaryView> loadOrders(String sql, Object... args) {
        List<OrderRow> rows = jdbcTemplate.query(sql, (rs, rowNum) -> new OrderRow(
                rs.getLong("id"),
                rs.getString("order_code"),
                rs.getString("customer_name"),
                rs.getString("customer_email"),
                rs.getString("shipping_name"),
                rs.getString("shipping_phone"),
                rs.getString("shipping_address_line"),
                rs.getString("shipping_district"),
                rs.getString("shipping_city"),
                rs.getTimestamp("created_at") == null ? null : rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getString("status"),
                rs.getInt("item_count"),
                rs.getBigDecimal("total_amount")
        ), args);

        Map<Long, List<OrderItemView>> itemsByOrder = loadItems(rows.stream().map(OrderRow::id).toList());
        return rows.stream()
                .map(row -> new OrderSummaryView(
                        row.id(),
                        row.orderCode(),
                        row.customerName(),
                        row.customerEmail(),
                        row.shippingName(),
                        row.shippingPhone(),
                        row.shippingAddressLine(),
                        row.shippingDistrict(),
                        row.shippingCity(),
                        row.createdAt(),
                        row.status(),
                        row.itemCount(),
                        currencyFormat.format(row.totalAmount()),
                        itemsByOrder.getOrDefault(row.id(), List.of())
                ))
                .toList();
    }

    private Map<Long, List<OrderItemView>> loadItems(Collection<Long> orderIds) {
        if (orderIds.isEmpty()) {
            return Map.of();
        }

        String placeholders = orderIds.stream().map(id -> "?").collect(Collectors.joining(","));
        return jdbcTemplate.query("""
                        SELECT order_id, product_name, sku, color, size, quantity, unit_price, line_total
                        FROM dbo.order_items
                        WHERE order_id IN (%s)
                        ORDER BY id ASC
                        """.formatted(placeholders),
                (rs) -> {
                    Map<Long, List<OrderItemView>> grouped = new LinkedHashMap<>();
                    while (rs.next()) {
                        Long orderId = rs.getLong("order_id");
                        grouped.computeIfAbsent(orderId, ignored -> new ArrayList<>()).add(new OrderItemView(
                                rs.getString("product_name"),
                                rs.getString("sku"),
                                rs.getString("color"),
                                rs.getString("size"),
                                rs.getInt("quantity"),
                                currencyFormat.format(rs.getBigDecimal("unit_price")),
                                currencyFormat.format(rs.getBigDecimal("line_total"))
                        ));
                    }
                    return grouped;
                },
                orderIds.toArray());
    }

    private String nextOrderCode(Long userId) {
        return "UM-" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-" + userId;
    }

    private void addOrderColumnIfMissing(String columnName, String definition) {
        jdbcTemplate.execute("""
                IF COL_LENGTH('dbo.orders', '%s') IS NULL
                BEGIN
                    ALTER TABLE dbo.orders ADD %s %s;
                END
                """.formatted(columnName, columnName, definition));
    }

    private record OrderLine(CartItemView item, ProductVariant variant, BigDecimal unitPrice, BigDecimal lineTotal) {
    }

    private record OrderStatusRow(Long id, String status) {
    }

    private record OrderStockRow(Long productVariantId, int quantity) {
    }

    private record OrderRow(Long id,
                            String orderCode,
                            String customerName,
                            String customerEmail,
                            String shippingName,
                            String shippingPhone,
                            String shippingAddressLine,
                            String shippingDistrict,
                            String shippingCity,
                            LocalDateTime createdAt,
                            String status,
                            int itemCount,
                            BigDecimal totalAmount) {
    }
}
