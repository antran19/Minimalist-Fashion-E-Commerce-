package com.uminimalist.store.model;

import com.uminimalist.store.entity.User;

public record CustomerAddressView(
        String recipientName,
        String phone,
        String addressLine,
        String district,
        String city
) {
    public static CustomerAddressView emptyFor(User user) {
        return new CustomerAddressView(user.getFullName(), user.getPhone(), "", "", "");
    }

    public boolean isComplete() {
        return hasText(recipientName)
                && hasText(phone)
                && hasText(addressLine)
                && hasText(district)
                && hasText(city);
    }

    public String fullAddress() {
        if (!hasText(addressLine) && !hasText(district) && !hasText(city)) {
            return "No shipping address saved yet.";
        }
        return String.join(", ", java.util.stream.Stream.of(addressLine, district, city)
                .filter(CustomerAddressView::hasText)
                .toList());
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
