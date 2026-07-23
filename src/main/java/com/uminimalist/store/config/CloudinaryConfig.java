package com.uminimalist.store.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

@Configuration
public class CloudinaryConfig {

    @Value("${cloudinary.cloud-name:}")
    private String cloudName;

    @Value("${cloudinary.api-key:}")
    private String apiKey;

    @Value("${cloudinary.api-secret:}")
    private String apiSecret;

    @Bean
    public Cloudinary cloudinary() {
        Map<String, String> envMap = loadDotEnv();

        String finalCloudName = resolveValue(cloudName, envMap.get("CLOUDINARY_CLOUD_NAME"));
        String finalApiKey = resolveValue(apiKey, envMap.get("CLOUDINARY_API_KEY"));
        String finalApiSecret = resolveValue(apiSecret, envMap.get("CLOUDINARY_API_SECRET"));

        Map<String, String> config = ObjectUtils.asMap(
                "cloud_name", finalCloudName.trim(),
                "api_key", finalApiKey.trim(),
                "api_secret", finalApiSecret.trim(),
                "secure", true
        );
        return new Cloudinary(config);
    }

    private String resolveValue(String primary, String envFallback) {
        if (primary != null && !primary.isBlank() && !primary.startsWith("${")) {
            return primary;
        }
        return envFallback != null ? envFallback : "";
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
