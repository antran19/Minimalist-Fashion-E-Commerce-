package com.uminimalist.store;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MinimalistFashionStoreApplication {

    public static void main(String[] args) {
        SpringApplication.run(MinimalistFashionStoreApplication.class, args);
    }
}
