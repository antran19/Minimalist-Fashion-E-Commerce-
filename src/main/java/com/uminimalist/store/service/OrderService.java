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

    private static final String PENDING_PAYMENT_STATUS = "PENDING_PAYMENT";

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
        addOrderColumnIfMissing("notes", "NVARCHAR(500) NULL");
        addOrderColumnIfMissing("payment_method", "NVARCHAR(30) NOT NULL DEFAULT 'COD'");
        addOrderColumnIfMissing("payment_status", "NVARCHAR(30) NOT NULL DEFAULT 'UNPAID'");
        addOrderItemColumnIfMissing("product_slug", "NVARCHAR(120) NULL");

        // Fix any existing legacy orders in DB that were confirmed paid via VNPay but payment_status remained PENDING/UNPAID
        jdbcTemplate.execute("""
                UPDATE dbo.orders
                SET payment_status = 'PAID'
                WHERE status IN ('PROCESSING', 'SHIPPED', 'DELIVERED')
                  AND payment_method LIKE '%VNPAY%'
                  AND (payment_status IS NULL OR payment_status = 'PENDING' OR payment_status = 'UNPAID')
                """);
    }

    @Transactional
    public OrderSummaryView placeOrder(String customerEmail, CartView cart, CustomerAddressView shippingAddress) {
        return placeOrder(customerEmail, cart, shippingAddress, "COD", null);
    }

    @Transactional
    public OrderSummaryView placePendingOrder(String customerEmail, CartView cart, CustomerAddressView shippingAddress) {
        return placeOrder(customerEmail, cart, shippingAddress, "PAYPAL", null);
    }

    @Transactional
    public OrderSummaryView placePendingOrder(String customerEmail, CartView cart, CustomerAddressView shippingAddress, String notes) {
        return placeOrder(customerEmail, cart, shippingAddress, "PAYPAL", notes);
    }

    @Transactional
    public OrderSummaryView placeOrder(String customerEmail, CartView cart, CustomerAddressView shippingAddress, String paymentMethod) {
        return placeOrder(customerEmail, cart, shippingAddress, paymentMethod, null);
    }

    @Transactional
    public OrderSummaryView placeOrder(String customerEmail, CartView cart, CustomerAddressView shippingAddress, String paymentMethod, String notes) {
        if (cart == null || cart.isEmpty()) {
            throw new IllegalArgumentException("Your cart is empty.");
        }
        if (shippingAddress == null || !shippingAddress.isComplete()) {
            throw new IllegalArgumentException("Please add a complete shipping address before placing the order.");
        }

        String normalizedMethod = (paymentMethod == null || paymentMethod.isBlank()) ? "COD" : paymentMethod.trim().toUpperCase(Locale.ROOT);
        if (!"COD".equals(normalizedMethod) && !"PAYPAL".equals(normalizedMethod)) {
            throw new IllegalArgumentException("Invalid payment method. Please select Cash on Delivery (COD) or PayPal.");
        }

        boolean isPayPal = "PAYPAL".equals(normalizedMethod);
        String initialPaymentStatus = isPayPal ? "PENDING" : "UNPAID";
        String initialOrderStatus = isPayPal ? PENDING_PAYMENT_STATUS : "PLACED";
        boolean deductStock = !isPayPal;

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
            if (deductStock) {
                variant.setStockQuantity(variant.getStockQuantity() - item.quantity());
            }
        }

        if (deductStock) {
            productVariantRepository.saveAll(lines.stream().map(OrderLine::variant).toList());
        }

        String orderCode = nextOrderCode(user.getId());
        Long orderId = insertOrder(orderCode, user, shippingAddress, itemCount, total, normalizedMethod, initialPaymentStatus, initialOrderStatus, notes);
        insertOrderItems(orderId, lines);

        return findOrder(orderId)
                .orElseThrow(() -> new IllegalStateException("Order was created but could not be loaded."));
    }

    public List<OrderSummaryView> findOrdersForCustomer(String customerEmail) {
        ensureOrderTables();
        return loadOrders("""
                SELECT TOP 12 id, order_code, customer_name, customer_email,
                    shipping_name, shipping_phone, shipping_address_line, shipping_district, shipping_city, notes,
                    created_at, status, payment_method, payment_status, item_count, total_amount
                FROM dbo.orders
                WHERE customer_email = ?
                ORDER BY created_at DESC, id DESC
                """, customerEmail);
    }

    private static final java.util.Set<String> VALID_ORDER_STATUSES = java.util.Set.of(
            "ALL", "PLACED", "PENDING_PAYMENT", "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED"
    );

    public PaginatedOrders findOrdersForCustomerPaginated(String customerEmail, String status, int page, int size) {
        ensureOrderTables();
        
        String normalizedStatus = (status == null || status.isBlank()) ? "ALL" : status.trim().toUpperCase(java.util.Locale.ROOT);
        if (!VALID_ORDER_STATUSES.contains(normalizedStatus)) {
            normalizedStatus = "ALL";
        }
        
        boolean hasStatus = !"ALL".equals(normalizedStatus);
        
        String countSql = hasStatus 
                ? "SELECT COUNT(*) FROM dbo.orders WHERE customer_email = ? AND status = ?"
                : "SELECT COUNT(*) FROM dbo.orders WHERE customer_email = ?";
        
        Object[] countArgs = hasStatus ? new Object[]{customerEmail, normalizedStatus} : new Object[]{customerEmail};
        
        Integer totalCount = jdbcTemplate.queryForObject(countSql, Integer.class, countArgs);
        if (totalCount == null) totalCount = 0;
        
        int totalPages = (int) Math.ceil((double) totalCount / size);
        if (totalPages <= 0) totalPages = 1;
        
        int validatedPage = page;
        if (validatedPage < 1) {
            validatedPage = 1;
        } else if (validatedPage > totalPages) {
            validatedPage = totalPages;
        }
        
        int offset = (validatedPage - 1) * size;
        if (offset < 0) offset = 0;
        
        String sql = hasStatus
                ? """
                  SELECT id, order_code, customer_name, customer_email,
                      shipping_name, shipping_phone, shipping_address_line, shipping_district, shipping_city, notes,
                      created_at, status, payment_method, payment_status, item_count, total_amount
                  FROM dbo.orders
                  WHERE customer_email = ? AND status = ?
                  ORDER BY created_at DESC, id DESC
                  OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                  """
                : """
                  SELECT id, order_code, customer_name, customer_email,
                      shipping_name, shipping_phone, shipping_address_line, shipping_district, shipping_city, notes,
                      created_at, status, payment_method, payment_status, item_count, total_amount
                  FROM dbo.orders
                  WHERE customer_email = ?
                  ORDER BY created_at DESC, id DESC
                  OFFSET ? ROWS FETCH NEXT ? ROWS ONLY
                  """;
                  
        Object[] args = hasStatus 
                ? new Object[]{customerEmail, normalizedStatus, offset, size}
                : new Object[]{customerEmail, offset, size};
                
        List<OrderSummaryView> orders = loadOrders(sql, args);
        
        return new PaginatedOrders(orders, validatedPage, totalPages, totalCount, normalizedStatus);
    }

    public Optional<OrderSummaryView> findOrderForCustomer(String customerEmail, String orderCode) {
        ensureOrderTables();
        return loadOrders("""
                SELECT id, order_code, customer_name, customer_email,
                    shipping_name, shipping_phone, shipping_address_line, shipping_district, shipping_city, notes,
                    created_at, status, payment_method, payment_status, item_count, total_amount
                FROM dbo.orders
                WHERE customer_email = ? AND order_code = ?
                """, customerEmail, orderCode).stream().findFirst();
    }

    @Transactional
    public void cancelOrderForCustomer(String customerEmail, String orderCode) {
        ensureOrderTables();
        OrderStatusRow order = requireOrder(customerEmail, orderCode);

        if ("PAID".equalsIgnoreCase(order.paymentStatus())) {
            throw new IllegalArgumentException("This order has been paid. Please contact support for a refund before cancelling.");
        }

        if (!"PLACED".equalsIgnoreCase(order.status()) && !"PENDING_PAYMENT".equalsIgnoreCase(order.status())) {
            throw new IllegalArgumentException("Only placed or pending payment orders can be cancelled.");
        }

        if (!"PENDING_PAYMENT".equalsIgnoreCase(order.status())) {
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
        }

        jdbcTemplate.update("""
                UPDATE dbo.orders
                SET status = 'CANCELLED'
                WHERE id = ?
                """, order.id());
    }

    /**
     * Confirms a pending order once the payment gateway reports success. Stock is only
     * taken here, and the guarded UPDATE makes the deduction atomic so two shoppers
     * cannot both claim the last unit. Safe to call twice (the gateway may retry).
     */
    @Transactional
    public OrderSummaryView confirmPaidOrder(String customerEmail, String orderCode) {
        ensureOrderTables();
        OrderStatusRow order = findOrderRowByCode(customerEmail, orderCode);
        if (order == null) {
            throw new IllegalArgumentException("Order not found.");
        }

        if ("PAID".equalsIgnoreCase(order.paymentStatus())) {
            return findOrder(order.id())
                    .orElseThrow(() -> new IllegalStateException("Order not found."));
        }

        if (!PENDING_PAYMENT_STATUS.equalsIgnoreCase(order.status())) {
            return findOrder(order.id())
                    .orElseThrow(() -> new IllegalArgumentException("Order is not pending payment."));
        }

        List<OrderStockRow> stockRows = jdbcTemplate.query("""
                        SELECT product_variant_id, quantity
                        FROM dbo.order_items
                        WHERE order_id = ?
                        """,
                (rs, rowNum) -> new OrderStockRow(rs.getLong("product_variant_id"), rs.getInt("quantity")),
                order.id());

        for (OrderStockRow stockRow : stockRows) {
            int updated = jdbcTemplate.update("""
                    UPDATE dbo.product_variants
                    SET stock_quantity = stock_quantity - ?
                    WHERE id = ? AND stock_quantity >= ?
                    """, stockRow.quantity(), stockRow.productVariantId(), stockRow.quantity());
            if (updated == 0) {
                jdbcTemplate.update("UPDATE dbo.orders SET status = 'CANCELLED', payment_status = 'FAILED' WHERE id = ?", order.id());
                throw new IllegalArgumentException("One or more products are no longer available in the requested quantity. Order cancelled.");
            }
        }

        jdbcTemplate.update("UPDATE dbo.orders SET status = 'PROCESSING', payment_status = 'PAID' WHERE id = ?", order.id());
        return findOrder(order.id())
                .orElseThrow(() -> new IllegalStateException("Order was paid but could not be loaded."));
    }

    public List<String> getOrderSkus(String orderCode) {
        ensureOrderTables();
        OrderStatusRow order = findOrderRowByCode(null, orderCode);
        if (order == null) return List.of();
        return jdbcTemplate.query("""
                SELECT pv.sku
                FROM dbo.order_items oi
                JOIN dbo.product_variants pv ON oi.product_variant_id = pv.id
                WHERE oi.order_id = ?
                """, (rs, rowNum) -> rs.getString("sku"), order.id());
    }

    /**
     * Marks a pending order as PENDING_PAYMENT / FAILED after a failed or abandoned payment.
     */
    @Transactional
    public void markPaymentFailed(String customerEmail, String orderCode) {
        ensureOrderTables();
        OrderStatusRow order = findOrderRowByCode(customerEmail, orderCode);
        if (order != null && PENDING_PAYMENT_STATUS.equalsIgnoreCase(order.status())) {
            jdbcTemplate.update("UPDATE dbo.orders SET status = 'PENDING_PAYMENT', payment_status = 'FAILED' WHERE id = ?", order.id());
        }
    }

    @Transactional
    public String preparePayAgainUrl(String customerEmail, String orderCode, PayPalService payPalService) {
        ensureOrderTables();
        OrderStatusRow orderRow = findOrderRowByCode(customerEmail, orderCode);
        if (orderRow == null) {
            throw new IllegalArgumentException("Order not found.");
        }

        if (!"PENDING_PAYMENT".equalsIgnoreCase(orderRow.status())) {
            throw new IllegalArgumentException("Only pending payment orders can be paid again.");
        }

        if (!"PAYPAL".equalsIgnoreCase(orderRow.paymentMethod())) {
            throw new IllegalArgumentException("Only PayPal orders can be paid online again.");
        }

        if ("PAID".equalsIgnoreCase(orderRow.paymentStatus())) {
            throw new IllegalArgumentException("This order has already been paid.");
        }

        // Check stock availability before creating payment URL
        List<OrderStockRow> stockRows = jdbcTemplate.query("""
                        SELECT product_variant_id, quantity
                        FROM dbo.order_items
                        WHERE order_id = ?
                        """,
                (rs, rowNum) -> new OrderStockRow(rs.getLong("product_variant_id"), rs.getInt("quantity")),
                orderRow.id());

        for (OrderStockRow stockRow : stockRows) {
            Integer available = jdbcTemplate.queryForObject("""
                    SELECT stock_quantity FROM dbo.product_variants WHERE id = ? AND active = 1
                    """, Integer.class, stockRow.productVariantId());

            if (available == null || available < stockRow.quantity()) {
                jdbcTemplate.update("UPDATE dbo.orders SET status = 'CANCELLED', payment_status = 'FAILED' WHERE id = ?", orderRow.id());
                throw new IllegalArgumentException("One or more products are no longer available in the requested quantity. Please update your cart and place a new order.");
            }
        }

        jdbcTemplate.update("UPDATE dbo.orders SET payment_status = 'PENDING' WHERE id = ?", orderRow.id());

        double vndAmount = orderRow.totalAmount() != null ? orderRow.totalAmount().doubleValue() : 0.0;
        try {
            com.paypal.api.payments.Payment payment = payPalService.createPayment(
                    vndAmount,
                    "sale",
                    "Order " + orderRow.orderCode(),
                    "http://localhost:9090/paypal/cancel",
                    "http://localhost:9090/paypal/success"
            );
            for (com.paypal.api.payments.Links link : payment.getLinks()) {
                if (link.getRel().equals("approval_url")) {
                    return link.getHref();
                }
            }
        } catch (com.paypal.base.rest.PayPalRESTException e) {
            throw new RuntimeException("Error creating PayPal payment", e);
        }
        return "redirect:/checkout";
    }

    private OrderStatusRow findOrderRowByCode(String customerEmail, String orderCode) {
        if (orderCode == null) return null;
        String baseCode = orderCode.contains("-R") ? orderCode.substring(0, orderCode.indexOf("-R")) : orderCode;
        List<OrderStatusRow> list;
        if (customerEmail != null && !customerEmail.isBlank()) {
            list = jdbcTemplate.query("""
                    SELECT id, status, payment_status, payment_method, order_code, total_amount
                    FROM dbo.orders
                    WHERE customer_email = ? AND (order_code = ? OR order_code = ?)
                    """,
                    (rs, rowNum) -> new OrderStatusRow(
                            rs.getLong("id"),
                            rs.getString("status"),
                            rs.getString("payment_status"),
                            rs.getString("payment_method"),
                            rs.getString("order_code"),
                            rs.getBigDecimal("total_amount")
                    ),
                    customerEmail, orderCode, baseCode);
        } else {
            list = jdbcTemplate.query("""
                    SELECT id, status, payment_status, payment_method, order_code, total_amount
                    FROM dbo.orders
                    WHERE order_code = ? OR order_code = ?
                    """,
                    (rs, rowNum) -> new OrderStatusRow(
                            rs.getLong("id"),
                            rs.getString("status"),
                            rs.getString("payment_status"),
                            rs.getString("payment_method"),
                            rs.getString("order_code"),
                            rs.getBigDecimal("total_amount")
                    ),
                    orderCode, baseCode);
        }
        return list.stream().findFirst().orElse(null);
    }

    private OrderStatusRow requireOrder(String customerEmail, String orderCode) {
        OrderStatusRow row = findOrderRowByCode(customerEmail, orderCode);
        if (row == null) {
            throw new IllegalArgumentException("Order not found.");
        }
        return row;
    }

    public List<OrderSummaryView> findRecentOrders() {
        ensureOrderTables();
        return loadOrders("""
                SELECT TOP 20 id, order_code, customer_name, customer_email,
                    shipping_name, shipping_phone, shipping_address_line, shipping_district, shipping_city, notes,
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
                    shipping_name, shipping_phone, shipping_address_line, shipping_district, shipping_city, notes,
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
                "shipping_name, shipping_phone, shipping_address_line, shipping_district, shipping_city, notes, " +
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
        List<String> validStatuses = List.of("PLACED", PENDING_PAYMENT_STATUS, "PROCESSING", "SHIPPED", "DELIVERED", "CANCELLED");
        if (!validStatuses.contains(normalizedStatus)) {
            throw new IllegalArgumentException("Invalid order status: " + newStatus);
        }

        // Load current order state
        List<OrderStatusRow> rows = jdbcTemplate.query("""
                SELECT id, status, payment_status, payment_method, order_code, total_amount
                FROM dbo.orders
                WHERE id = ?
                """,
                (rs, rowNum) -> new OrderStatusRow(
                        rs.getLong("id"),
                        rs.getString("status"),
                        rs.getString("payment_status"),
                        rs.getString("payment_method"),
                        rs.getString("order_code"),
                        rs.getBigDecimal("total_amount")
                ),
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
        if ("CANCELLED".equals(normalizedStatus) && !"PENDING_PAYMENT".equalsIgnoreCase(currentStatus)) {
            // Return stock to inventory (only for orders that previously deducted stock)
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

        // If payment is still PENDING (PayPal not confirmed), block moving past PENDING_PAYMENT
        if (isPending && "PENDING_PAYMENT".equalsIgnoreCase(current) && "PROCESSING".equalsIgnoreCase(next)) {
            throw new IllegalArgumentException(
                "This order uses PayPal but payment has not been confirmed yet. " +
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

    private Long insertOrder(String orderCode, User user, CustomerAddressView shippingAddress, int itemCount, BigDecimal total, String paymentMethod, String paymentStatus, String orderStatus, String notes) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement("""
                    INSERT INTO dbo.orders
                        (order_code, user_id, customer_name, customer_email,
                         shipping_name, shipping_phone, shipping_address_line, shipping_district, shipping_city, notes,
                         status, payment_method, payment_status, item_count, total_amount)
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
            ps.setString(10, notes == null || notes.isBlank() ? null : notes.trim());
            ps.setString(11, orderStatus);
            ps.setString(12, paymentMethod);
            ps.setString(13, paymentStatus);
            ps.setInt(14, itemCount);
            ps.setBigDecimal(15, total);
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
                    (order_id, product_variant_id, product_name, sku, color, size, quantity, unit_price, line_total, product_slug)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
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
            ps.setString(10, item.productSlug());
        });
    }

    private Optional<OrderSummaryView> findOrder(Long orderId) {
        List<OrderSummaryView> orders = loadOrders("""
                SELECT id, order_code, customer_name, customer_email,
                    shipping_name, shipping_phone, shipping_address_line, shipping_district, shipping_city, notes,
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
                rs.getString("notes"),
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
                        row.notes(),
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
                        SELECT oi.order_id, oi.product_name, oi.sku, oi.color, oi.size, oi.quantity, oi.unit_price, oi.line_total,
                               COALESCE(oi.product_slug, p.slug) AS product_slug
                        FROM dbo.order_items oi
                        LEFT JOIN dbo.product_variants pv ON oi.product_variant_id = pv.id
                        LEFT JOIN dbo.products p ON pv.product_id = p.id
                        WHERE oi.order_id IN (%s)
                        ORDER BY oi.id ASC
                        """.formatted(placeholders),
                (rs) -> {
                    Map<Long, List<OrderItemView>> grouped = new LinkedHashMap<>();
                    while (rs.next()) {
                        Long orderId = rs.getLong("order_id");
                        String slug = rs.getString("product_slug");
                        String sku = rs.getString("sku");
                        String productName = rs.getString("product_name");
                        String imgPath = imagePath(slug, sku, productName);
                        grouped.computeIfAbsent(orderId, ignored -> new ArrayList<>()).add(new OrderItemView(
                                productName,
                                sku,
                                rs.getString("color"),
                                rs.getString("size"),
                                rs.getInt("quantity"),
                                currencyFormat.format(rs.getBigDecimal("unit_price")),
                                currencyFormat.format(rs.getBigDecimal("line_total")),
                                imgPath,
                                slug
                        ));
                    }
                    return grouped;
                },
                orderIds.toArray());
    }

    private String imagePath(String slug, String sku, String productName) {
        String effectiveSlug = slug;
        if (effectiveSlug == null || effectiveSlug.isBlank()) {
            if (sku != null && !sku.isBlank()) {
                String lowerSku = sku.toLowerCase();
                if (lowerSku.startsWith("air-cotton-tee")) effectiveSlug = "air-cotton-tee";
                else if (lowerSku.startsWith("light-utility-jacket")) effectiveSlug = "light-utility-jacket";
                else if (lowerSku.startsWith("soft-jersey-tee")) effectiveSlug = "soft-jersey-tee";
                else if (lowerSku.startsWith("everyday-zip-hoodie")) effectiveSlug = "everyday-zip-hoodie";
                else if (lowerSku.startsWith("smart-ankle-pants")) effectiveSlug = "smart-ankle-pants";
                else if (lowerSku.startsWith("oxford-shirt")) effectiveSlug = "oxford-shirt";
                else if (lowerSku.startsWith("linen-blend-shirt")) effectiveSlug = "linen-blend-shirt";
                else if (lowerSku.startsWith("utility-tote")) effectiveSlug = "utility-tote";
                else if (lowerSku.startsWith("easy-cotton-shorts")) effectiveSlug = "easy-cotton-shorts";
                else if (lowerSku.startsWith("school-day-cardigan")) effectiveSlug = "school-day-cardigan";
            }
        }
        if (effectiveSlug == null || effectiveSlug.isBlank()) {
            if (productName != null && !productName.isBlank()) {
                String lowerName = productName.toLowerCase();
                if (lowerName.contains("cotton tee")) effectiveSlug = "air-cotton-tee";
                else if (lowerName.contains("utility jacket")) effectiveSlug = "light-utility-jacket";
                else if (lowerName.contains("jersey tee")) effectiveSlug = "soft-jersey-tee";
                else if (lowerName.contains("zip hoodie")) effectiveSlug = "everyday-zip-hoodie";
                else if (lowerName.contains("ankle pants")) effectiveSlug = "smart-ankle-pants";
                else if (lowerName.contains("oxford shirt")) effectiveSlug = "oxford-shirt";
                else if (lowerName.contains("linen")) effectiveSlug = "linen-blend-shirt";
                else if (lowerName.contains("tote")) effectiveSlug = "utility-tote";
                else if (lowerName.contains("shorts")) effectiveSlug = "easy-cotton-shorts";
                else if (lowerName.contains("cardigan")) effectiveSlug = "school-day-cardigan";
            }
        }
        if (effectiveSlug == null || effectiveSlug.isBlank()) {
            return "/images/product-collage.png";
        }
        return switch (effectiveSlug) {
            case "air-cotton-tee" -> "/images/products/air-cotton-tee-v2.png";
            case "light-utility-jacket" -> "/images/products/light-utility-jacket-v2.png";
            case "soft-jersey-tee" -> "/images/products/soft-jersey-tee-v2.png";
            case "everyday-zip-hoodie" -> "/images/products/everyday-zip-hoodie-v2.png";
            case "smart-ankle-pants" -> "/images/products/smart-ankle-pants-v2.png";
            case "oxford-shirt" -> "/images/products/oxford-shirt-v2.png";
            case "linen-blend-shirt" -> "/images/products/linen-blend-shirt-v2.png";
            case "utility-tote" -> "/images/products/utility-tote-v2.png";
            case "easy-cotton-shorts" -> "/images/products/easy-cotton-shorts-v2.png";
            case "school-day-cardigan" -> "/images/products/school-day-cardigan-v2.png";
            default -> "/images/product-collage.png";
        };
    }

    private String nextOrderCode(Long userId) {
        int randomSuffix = java.util.concurrent.ThreadLocalRandom.current().nextInt(100, 1000);
        return "UM-" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS"))
                + "-" + randomSuffix + "-" + userId;
    }

    private void addOrderColumnIfMissing(String columnName, String definition) {
        jdbcTemplate.execute("""
                IF COL_LENGTH('dbo.orders', '%s') IS NULL
                BEGIN
                    ALTER TABLE dbo.orders ADD %s %s;
                END
                """.formatted(columnName, columnName, definition));
    }

    private void addOrderItemColumnIfMissing(String columnName, String definition) {
        jdbcTemplate.execute("""
                IF COL_LENGTH('dbo.order_items', '%s') IS NULL
                BEGIN
                    ALTER TABLE dbo.order_items ADD %s %s;
                END
                """.formatted(columnName, columnName, definition));
    }

    private record OrderLine(CartItemView item, ProductVariant variant, BigDecimal unitPrice, BigDecimal lineTotal) {
    }

    private record OrderStatusRow(Long id, String status, String paymentStatus, String paymentMethod, String orderCode, BigDecimal totalAmount) {
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
                            String notes,
                            LocalDateTime createdAt,
                            String status,
                            String paymentMethod,
                            String paymentStatus,
                            int itemCount,
                            BigDecimal totalAmount) {
    }
}
