package com.uminimalist.store.service;

import com.uminimalist.store.entity.User;
import com.uminimalist.store.model.CustomerAddressView;
import com.uminimalist.store.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class CustomerAddressService {

    private final JdbcTemplate jdbcTemplate;
    private final UserRepository userRepository;

    public CustomerAddressService(JdbcTemplate jdbcTemplate, UserRepository userRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.userRepository = userRepository;
    }

    @PostConstruct
    public void ensureAddressTable() {
        jdbcTemplate.execute("""
                IF OBJECT_ID(N'dbo.customer_addresses', N'U') IS NULL
                BEGIN
                    CREATE TABLE dbo.customer_addresses (
                        id BIGINT IDENTITY(1,1) NOT NULL PRIMARY KEY,
                        user_id BIGINT NOT NULL,
                        recipient_name NVARCHAR(120) NOT NULL,
                        phone NVARCHAR(20) NOT NULL,
                        address_line NVARCHAR(255) NOT NULL,
                        district NVARCHAR(120) NOT NULL,
                        city NVARCHAR(120) NOT NULL,
                        is_default BIT NOT NULL DEFAULT 1,
                        created_at DATETIME2 NOT NULL DEFAULT SYSUTCDATETIME(),
                        updated_at DATETIME2 NULL
                    );
                END
                """);
    }

    public CustomerAddressView findDefaultAddress(String customerEmail) {
        ensureAddressTable();
        User user = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Customer account not found."));
        return findDefaultAddress(user).orElse(CustomerAddressView.emptyFor(user));
    }

    @Transactional
    public CustomerAddressView saveDefaultAddress(String customerEmail,
                                                  String recipientName,
                                                  String phone,
                                                  String addressLine,
                                                  String district,
                                                  String city) {
        ensureAddressTable();
        User user = userRepository.findByEmail(customerEmail)
                .orElseThrow(() -> new IllegalArgumentException("Customer account not found."));
        CustomerAddressView address = normalize(recipientName, phone, addressLine, district, city);
        validate(address);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM dbo.customer_addresses WHERE user_id = ? AND is_default = 1",
                Integer.class,
                user.getId());

        if (count != null && count > 0) {
            jdbcTemplate.update("""
                    UPDATE dbo.customer_addresses
                    SET recipient_name = ?, phone = ?, address_line = ?, district = ?, city = ?, updated_at = SYSUTCDATETIME()
                    WHERE user_id = ? AND is_default = 1
                    """,
                    address.recipientName(),
                    address.phone(),
                    address.addressLine(),
                    address.district(),
                    address.city(),
                    user.getId());
        } else {
            jdbcTemplate.update("""
                    INSERT INTO dbo.customer_addresses
                        (user_id, recipient_name, phone, address_line, district, city, is_default)
                    VALUES (?, ?, ?, ?, ?, ?, 1)
                    """,
                    user.getId(),
                    address.recipientName(),
                    address.phone(),
                    address.addressLine(),
                    address.district(),
                    address.city());
        }

        return address;
    }

    private Optional<CustomerAddressView> findDefaultAddress(User user) {
        return jdbcTemplate.query("""
                        SELECT TOP 1 recipient_name, phone, address_line, district, city
                        FROM dbo.customer_addresses
                        WHERE user_id = ? AND is_default = 1
                        ORDER BY updated_at DESC, created_at DESC, id DESC
                        """,
                (rs, rowNum) -> new CustomerAddressView(
                        rs.getString("recipient_name"),
                        rs.getString("phone"),
                        rs.getString("address_line"),
                        rs.getString("district"),
                        rs.getString("city")),
                user.getId())
                .stream()
                .findFirst();
    }

    private CustomerAddressView normalize(String recipientName, String phone, String addressLine, String district, String city) {
        return new CustomerAddressView(
                compact(recipientName),
                compact(phone),
                compact(addressLine),
                compact(district),
                compact(city));
    }

    private static final java.util.regex.Pattern PHONE_PATTERN = java.util.regex.Pattern.compile("^[0-9+\\s\\-()]{8,20}$");

    private void validate(CustomerAddressView address) {
        if (address.recipientName().length() < 2 || address.recipientName().length() > 120) {
            throw new IllegalArgumentException("Recipient name must be between 2 and 120 characters.");
        }
        if (address.phone().length() < 8 || address.phone().length() > 20 || !PHONE_PATTERN.matcher(address.phone()).matches()) {
            throw new IllegalArgumentException("Shipping phone number is invalid. Please enter a valid phone number (8-20 characters/digits).");
        }
        if (address.addressLine().length() < 5 || address.addressLine().length() > 255) {
            throw new IllegalArgumentException("Address line must be between 5 and 255 characters.");
        }
        if (address.district().length() < 2 || address.district().length() > 120) {
            throw new IllegalArgumentException("District must be between 2 and 120 characters.");
        }
        if (address.city().length() < 2 || address.city().length() > 120) {
            throw new IllegalArgumentException("City must be between 2 and 120 characters.");
        }
    }

    private String compact(String value) {
        return value == null ? "" : value.trim().replaceAll("\\s+", " ");
    }
}
