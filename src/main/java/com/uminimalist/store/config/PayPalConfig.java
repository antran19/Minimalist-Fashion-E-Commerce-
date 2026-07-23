package com.uminimalist.store.config;

import com.paypal.base.rest.APIContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class PayPalConfig {

    @Value("${paypal.client.id:}")
    private String clientId;

    @Value("${paypal.client.secret:}")
    private String clientSecret;

    @Value("${paypal.mode:sandbox}")
    private String mode;

    @Bean
    public APIContext apiContext() {
        Map<String, String> envMap = loadDotEnv();

        String finalClientId = resolveValue(clientId, envMap.get("PAYPAL_CLIENT_ID"), "dummy_client_id");
        String finalClientSecret = resolveValue(clientSecret, envMap.get("PAYPAL_CLIENT_SECRET"), "dummy_client_secret");
        String finalMode = resolveValue(mode, envMap.get("PAYPAL_MODE"), "sandbox");

        return new APIContext(finalClientId, finalClientSecret, finalMode);
    }

    private String resolveValue(String primary, String envFallback, String defaultVal) {
        if (primary != null && !primary.isBlank() && !primary.startsWith("${")) {
            return primary;
        }
        if (envFallback != null && !envFallback.isBlank()) {
            return envFallback;
        }
        return defaultVal;
    }

    private Map<String, String> loadDotEnv() {
        Map<String, String> map = new HashMap<>();
        File envFile = new File(".env");
        if (!envFile.exists()) return map;
        try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) continue;
                int eq = line.indexOf('=');
                if (eq > 0) {
                    map.put(line.substring(0, eq).trim(), line.substring(eq + 1).trim());
                }
            }
        } catch (Exception ignored) {
        }
        return map;
    }
}

