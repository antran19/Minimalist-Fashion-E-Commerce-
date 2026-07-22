package com.uminimalist.store.util;

import java.util.regex.Pattern;

public final class PhoneValidator {

    /*
     * Vietnamese mobile phone prefixes:
     * 03x, 05x, 07x, 08x, 09x
     */
    private static final Pattern VIETNAM_MOBILE_PATTERN = Pattern.compile(
            "^(03[2-9]|05[25689]|07[06-9]|08[1-9]|09[0-46-9])\\d{7}$"
    );

    private PhoneValidator() {
    }

    public static String normalizeVietnameseMobile(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Phone number is required.");
        }

        // Remove spaces, dots, hyphens and parentheses.
        String normalized = value.trim()
                .replaceAll("[\\s.\\-()]", "");

        // Convert international format to local format.
        if (normalized.startsWith("+84")) {
            normalized = "0" + normalized.substring(3);
        } else if (normalized.startsWith("84")) {
            normalized = "0" + normalized.substring(2);
        }

        if (!VIETNAM_MOBILE_PATTERN.matcher(normalized).matches()) {
            throw new IllegalArgumentException(
                    "Please enter a valid Vietnamese mobile number, for example 0912345678."
            );
        }

        return normalized;
    }
}
