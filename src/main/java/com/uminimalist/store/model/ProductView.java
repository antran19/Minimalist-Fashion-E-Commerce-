package com.uminimalist.store.model;

public record ProductView(
        String name,
        String category,
        String price,
        String colors,
        String sizes,
        String cropClass
) {
}
