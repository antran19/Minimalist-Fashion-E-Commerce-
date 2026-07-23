package com.uminimalist.store.util;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

/**
 * Bulk uploader utility to upload local product images to Cloudinary,
 * recreating the proper folder structure ("uminimalist/products/<slug>/<public_id>")
 * and generating matching SQL insert script.
 */
public class CloudinaryBulkUploader {

    private static final String[] PRODUCT_SLUGS = {
            "air-cotton-tee",
            "easy-cotton-shorts",
            "everyday-zip-hoodie",
            "light-utility-jacket",
            "linen-blend-shirt",
            "oxford-shirt",
            "school-day-cardigan",
            "smart-ankle-pants",
            "soft-jersey-tee",
            "utility-tote"
    };

    public static void main(String[] args) {
        System.out.println("=== Starting Cloudinary Bulk Image Upload ===");

        Map<String, String> envMap = loadDotEnv();
        String cloudName = envMap.getOrDefault("CLOUDINARY_CLOUD_NAME", "dcroyqkoa");
        String apiKey = envMap.getOrDefault("CLOUDINARY_API_KEY", "636328917892277");
        String apiSecret = envMap.getOrDefault("CLOUDINARY_API_SECRET", "Cdy5FvBl0Mi49C70C_12sNdz_bs");

        Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));

        File folder = new File("src/main/resources/static/images/products");
        if (!folder.exists() || !folder.isDirectory()) {
            System.err.println("Folder src/main/resources/static/images/products not found!");
            return;
        }

        File[] files = folder.listFiles((dir, name) -> {
            String lower = name.toLowerCase();
            return lower.endsWith(".jpg") || lower.endsWith(".jpeg") || lower.endsWith(".png") || lower.endsWith(".webp");
        });

        if (files == null || files.length == 0) {
            System.out.println("No image files found to upload.");
            return;
        }

        Arrays.sort(files, Comparator.comparing(File::getName));
        System.out.printf("Found %d image files to process.%n%n", files.length);

        List<String> results = new ArrayList<>();

        for (File file : files) {
            String fileName = file.getName();
            String fileNameWithoutExt = fileName.substring(0, fileName.lastIndexOf('.'));
            String matchedSlug = matchSlug(fileNameWithoutExt);

            if (matchedSlug == null) {
                System.err.println("Skipping " + fileName + ": Could not match product slug.");
                continue;
            }

            String targetFolder = "uminimalist/products/" + matchedSlug;
            String fullPublicId = targetFolder + "/" + fileNameWithoutExt;

            try {
                System.out.printf("Uploading: %s -> Folder: [%s] ... ", fileName, targetFolder);
                Map uploadResult = cloudinary.uploader().upload(file, ObjectUtils.asMap(
                        "folder", targetFolder,
                        "asset_folder", targetFolder,
                        "public_id", fullPublicId,
                        "overwrite", true,
                        "invalidate", true,
                        "resource_type", "image"
                ));
                String secureUrl = (String) uploadResult.get("secure_url");
                System.out.println("SUCCESS!");
                System.out.println("  Public ID : " + uploadResult.get("public_id"));
                System.out.println("  URL       : " + secureUrl);
                results.add(matchedSlug + " | " + uploadResult.get("public_id") + " | " + secureUrl);
            } catch (Exception e) {
                System.err.println("FAILED: " + e.getMessage());
            }

        }

        System.out.println("\n=== Upload Complete! ===");
        System.out.println("Successfully processed " + results.size() + " images.");
    }

    private static String matchSlug(String fileNameWithoutExt) {
        for (String slug : PRODUCT_SLUGS) {
            if (fileNameWithoutExt.startsWith(slug)) {
                return slug;
            }
        }
        return null;
    }

    private static Map<String, String> loadDotEnv() {
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
