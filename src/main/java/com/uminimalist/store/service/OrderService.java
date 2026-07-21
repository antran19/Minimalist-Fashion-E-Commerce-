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
                        payment_method NVARCHAR(30) NOT NULL DEFAULT 'COD',
                        payment_status NVARCHAR(30) NOT NULL DEFAULT 'UNPAID',
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
        addOrderColumnIfMissing("payment_method", "NVARCHAR(30) NOT NULL DEFAULT 'COD'");
        addOrderColumnIfMissing("payment_status", "NVARCHAR(30) NOT NULL DEFAULT 'UNPAID'");
    }

    @Transactional
    public OrderSummaryView placeOrder(String customerEmail, CartView cart, CustomerAddressView shippingAddress) {
        return placeOrder(customerEmail, cart, shippingAddress, "COD");
    }

    @Transactional
    public OrderSummaryView placeOrder(String customerEmail, CartView cart, CustomerAddressView shippingAddress, String paymentMethod) {
        if (cart == null || cart.isEmpty()) {
            throw new IllegalArgumentException("Your cart is empty.");
        }
        if (shippingAddress == null || !shippingAddress.isComplete()) {
            throw new IllegalArgumentException("Please add a complete shipping address before placing the order.");
        }

        String normalizedMethod = (paymentMethod == null || paymentMethod.isBlank()) ? "COD" : paymentMethod.trim().toUpperCase(Locale.ROOT);
        if (!"COD".equals(normalizedMethod) && !"VNPAY".equals(normalizedMethod)) {
            throw new IllegalArgumentException("Invalid payment method. Please select Cash on Delivery (COD) or VNPay Demo.");
        }

        String initialPaymentStatus = "VNPAY".equals(normalizedMethod) ? "PENDING" : "UNPAID";
        String initialOrderStatus = "VNPAY".equals(normalizedMethod) ? "PENDING_PAYMENT" : "PLACED";

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
        Long orderId = insertOrder(orderCode, user, shippingAddress, itemCount, total, normalizedMethod, initialPaymentStatus, initialOrderStatus);
        insertOrderItems(orderId, lines);

        return findOrder(orderId)
                .orElseThrow(() -> new IllegalStateException("Order was created but could not be loaded."));
    }

    public List<OrderSummaryView> findOrdersForCustomer(String customerEmail) {
        ensureOrderTables();
        return loadOrders("""
                SELECT TOP 12 id, order_code, customer_name, customer_email,
                    shipping_name, shipping_phone, shipping_address_line, shipping_district, shipping_city,
                    created_at, status, payment_method, payment_status, item_count, total_amount
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
                      created_at, status, payment_method, payment_status, item_count, total_amount
                  FROM dbo.orders
                  WHERE customer_email = ? AND status = ?
                  ORDER BY created_at DESC, id DESC
                  OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                  """
                : """
                  SELECT id, order_code, customer_name, customer_email,
                      shipping_name, shipping_phone, shipping_address_line, shipping_district, shipping_city,
                      created_at, status, payment_method, payment_status, item_count, total_amount
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
                    created_at, status, payment_method, payment_status, item_count, total_amount
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
                (rs, rowNum) -> new OrderStatusRow(rs.getLong("id"), rs.getString("status"), rs.getString("payment_status")),
                customerEmail,
                orderCode);

        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Order not found.");
        }

        OrderStatusRow order = rows.get(0);
        if ("PAID".equalsIgnoreCase(order.paymentStatus())) {
            throw new IllegalArgumentException("This order has been paid. Please contact support for a refund before cancelling.");
        }

        if (!"PLACED".equalsIgnoreCase(order.status()) && !"PENDING_PAYMENT".equalsIgnoreCase(order.status())) {
            throw new IllegalArgumentException("Only placed or pending payment orders can be cancelled.");
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
                    created_at, status, payment_method, payment_status, item_count, total_amount
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
                    created_at, status, payment_method, payment_status, item_count, total_amount
                FROM dbo.orders
                WHERE status = ?
                ORDER BY created_at DESC, id DESC
                """, status.toUpperCase(Locale.ROOT));
    }

    public PaginatedOrders findPaginatedOrders(int page, int size, String query, String status) {
        ensureOrderTables();
        int safePage = Math.max(0, page);
        int offset = safePage * size;

        StringBuilder whereSql = new StringBuilder(" WHERE 1=1 ");
        List<Object> params = new ArrayList<>();

        if (status != null && !status.isBlank() && !"ALL".equalsIgnoreCase(status)) {
            whereSql.append(" AND status = ? ");
            params.add(status.toUpperCase(Locale.ROOT));
        }

        if (query != null && !query.isBlank()) {
            whereSql.append(" AND (LOWER(order_code) LIKE ? OR LOWER(customer_name) LIKE ? OR LOWER(customer_email) LIKE ?) ");
            String qParam = "%" + query.trim().toLowerCase(Locale.ROOT) + "%";
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
                "created_at, status, payment_method, payment_status, item_count, total_amount " +
                "FROM dbo.orders " + whereSql +
                "ORDER BY created_at DESC, id DESC " +
                "OFFSET " + offset + " ROWS FETCH NEXT " + size + " ROWS ONLY";

        List<OrderSummaryView> orders = loadOrders(selectSql, params.toArray());
        return new PaginatedOrders(orders, safePage + 1, totalPages, totalItems, status == null ? "ALL" : status);
    }

    @Transactional
    public void updateOrderStatus(Long orderId, String newStatus) {
        ensureOrderTables();
        String normalizedStatus = newStatus.toUpperCase(Locale.ROOT);
        List<String> validStatuses = List.of("PLACED", "PENDING_PAYMENT", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED");
        if (!validStatuses.contains(normalizedStatus)) {
            throw new IllegalArgumentException("Invalid order status: " + newStatus);
        }

        // Load current order state
        List<OrderStatusRow> rows = jdbcTemplate.query("""
                SELECT id, status, payment_status
                FROM dbo.orders
                WHERE id = ?
                """,
                (rs, rowNum) -> new OrderStatusRow(rs.getLong("id"), rs.getString("status"), rs.getString("payment_status")),
                orderId);

        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Order not found.");
        }

        String currentStatus = rows.get(0).status();
        String currentPaymentStatus = rows.get(0).paymentStatus();

        if (normalizedStatus.equals(currentStatus)) {
            return; // No change
        }

        // ---- State Machine Validation ----
        boolean isPaid = "PAID".equalsIgnoreCase(currentPaymentStatus);

        // Cannot change a terminal order
        if ("DELIVERED".equalsIgnoreCase(currentStatus)) {
            throw new IllegalArgumentException("Cannot change status of a delivered order.");
        }
        if ("CANCELLED".equalsIgnoreCase(currentStatus)) {
            throw new IllegalArgumentException("Cannot change status of a cancelled order.");
        }

        // Cannot go backwards
        if ("CANCELLED".equals(normalizedStatus)) {
            // Cancel is allowed from most states, but with payment checks
            validateCancellationAllowed(currentStatus, currentPaymentStatus);
        } else {
            // Forward transitions must follow the pipeline
            validateForwardTransition(currentStatus, normalizedStatus, currentPaymentStatus);
        }

        // ---- Execute Status Change ----
        if ("CANCELLED".equals(normalizedStatus)) {
            // Return stock to inventory
            List<OrderStockRow> stockRows = jdbcTemplate.query("""
                    SELECT product_variant_id, quantity
                    FROM dbo.order_items
                    WHERE order_id = ?
                    """,
                    (rs, rowNum) -> new OrderStockRow(rs.getLong("product_variant_id"), rs.getInt("quantity")),
                    orderId);

            for (OrderStockRow stockRow : stockRows) {
                jdbcTemplate.update("""
                        UPDATE dbo.product_variants
                        SET stock_quantity = stock_quantity + ?
                        WHERE id = ?
                        """, stockRow.quantity(), stockRow.productVariantId());
            }
        }

        // Auto-mark COD payment as PAID when delivered
        String paymentStatusUpdate = null;
        if ("DELIVERED".equals(normalizedStatus) && !isPaid) {
            paymentStatusUpdate = "PAID";
        }

        if (paymentStatusUpdate != null) {
            jdbcTemplate.update("""
                    UPDATE dbo.orders
                    SET status = ?, payment_status = ?
                    WHERE id = ?
                    """, normalizedStatus, paymentStatusUpdate, orderId);
        } else {
            jdbcTemplate.update("""
                    UPDATE dbo.orders
                    SET status = ?
                    WHERE id = ?
                    """, normalizedStatus, orderId);
        }
    }

    private void validateCancellationAllowed(String currentStatus, String paymentStatus) {
        boolean isPaid = "PAID".equalsIgnoreCase(paymentStatus);

        if ("SHIPPED".equalsIgnoreCase(currentStatus) || "DELIVERED".equalsIgnoreCase(currentStatus)) {
            throw new IllegalArgumentException("Cannot cancel an order that has already been shipped or delivered.");
        }

        if (isPaid) {
            throw new IllegalArgumentException(
                "This order has been PAID. Please process a refund before cancelling. " +
                "The customer's payment must be returned.");
        }
    }

    private void validateForwardTransition(String current, String next, String paymentStatus) {
        boolean isPending = "PENDING".equalsIgnoreCase(paymentStatus);

        Map<String, List<String>> allowedTransitions = Map.of(
            "PLACED",           List.of("PROCESSING"),
            "PENDING_PAYMENT",  List.of("PROCESSING"),
            "PROCESSING",       List.of("SHIPPED"),
            "SHIPPED",          List.of("DELIVERED")
        );

        List<String> allowed = allowedTransitions.getOrDefault(current, List.of());
        if (!allowed.contains(next)) {
            throw new IllegalArgumentException(
                "Invalid status transition: '" + current + "' → '" + next + "'. " +
                "Allowed next status(es): " + String.join(", ", allowed));
        }

        // If payment is still PENDING (VNPay not confirmed), block moving past PENDING_PAYMENT
        if (isPending && "PENDING_PAYMENT".equalsIgnoreCase(current) && "PROCESSING".equalsIgnoreCase(next)) {
            throw new IllegalArgumentException(
                "This order uses VNPay but payment has not been confirmed yet. " +
                "Wait for the payment callback or verify payment status first.");
        }
    }

    @Transactional
    public void updatePaymentStatus(Long orderId, String paymentStatus, String orderStatus) {
        ensureOrderTables();
        jdbcTemplate.update("""
                UPDATE dbo.orders
                SET payment_status = ?, status = ?
                WHERE id = ?
                """, paymentStatus.toUpperCase(Locale.ROOT), orderStatus.toUpperCase(Locale.ROOT), orderId);
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

    private Long insertOrder(String orderCode, User user, CustomerAddressView shippingAddress, int itemCount, BigDecimal total, String paymentMethod, String paymentStatus, String orderStatus) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO dbo.orders
                        (order_code, user_id, customer_name, customer_email,
                         shipping_name, shipping_phone, shipping_address_line, shipping_district, shipping_city,
                         status, payment_method, payment_status, item_count, total_amount)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
            ps.setString(10, orderStatus);
            ps.setString(11, paymentMethod);
            ps.setString(12, paymentStatus);
            ps.setInt(13, itemCount);
            ps.setBigDecimal(14, total);
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
                    created_at, status, payment_method, payment_status, item_count, total_amount
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
                rs.getString("payment_method") == null ? "COD" : rs.getString("payment_method"),
                rs.getString("payment_status") == null ? "UNPAID" : rs.getString("payment_status"),
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
                        row.paymentMethod(),
                        row.paymentStatus(),
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

    private record OrderStatusRow(Long id, String status, String paymentStatus) {
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
                            String paymentMethod,
                            String paymentStatus,
                            int itemCount,
                            BigDecimal totalAmount) {
    }
}
