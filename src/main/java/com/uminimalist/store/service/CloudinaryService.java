package com.uminimalist.store.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

@Service
public class CloudinaryService {

    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/gif"
    );

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of(
            ".jpg", ".jpeg", ".png", ".webp", ".gif"
    );

    private final Cloudinary cloudinary;

    public CloudinaryService(Cloudinary cloudinary) {
        this.cloudinary = cloudinary;
    }

    /**
     * Uploads a MultipartFile to Cloudinary under a specified folder path.
     *
     * @param file       The uploaded image file.
     * @param folderPath Subfolder on Cloudinary (e.g. "uminimalist/products/air-cotton-tee").
     * @return UploadResult record containing publicId and secureUrl.
     */
    @SuppressWarnings("unchecked")
    public UploadResult uploadImage(MultipartFile file, String folderPath) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select a valid image file to upload.");
        }

        String contentType = file.getContentType();
        String filename = file.getOriginalFilename();
        String ext = "";
        if (filename != null && filename.contains(".")) {
            ext = filename.substring(filename.lastIndexOf('.')).toLowerCase();
        }

        boolean contentTypeOk = contentType != null && ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase());
        boolean extensionOk = ALLOWED_EXTENSIONS.contains(ext);

        if (!contentTypeOk && !extensionOk) {
            throw new IllegalArgumentException("Invalid file type. Only JPG, PNG, WEBP, and GIF images are allowed.");
        }

        try {
            String sanitizedFolder = (folderPath == null || folderPath.isBlank())
                    ? "uminimalist/products"
                    : folderPath.trim();

            Map<String, Object> params = ObjectUtils.asMap(
                    "folder", sanitizedFolder,
                    "asset_folder", sanitizedFolder,
                    "overwrite", true,
                    "resource_type", "image"
            );


            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), params);

            String publicId = (String) uploadResult.get("public_id");
            String secureUrl = (String) uploadResult.get("secure_url");

            return new UploadResult(publicId, secureUrl);
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload image to Cloudinary: " + e.getMessage(), e);
        }
    }

    /**
     * Deletes an image from Cloudinary by its public ID.
     *
     * @param publicId Public ID of the image on Cloudinary.
     * @return true if successfully destroyed, false otherwise.
     */
    public boolean deleteImage(String publicId) {
        if (publicId == null || publicId.isBlank()) {
            return false;
        }
        try {
            Map<?, ?> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            return "ok".equalsIgnoreCase((String) result.get("result"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete image from Cloudinary: " + e.getMessage(), e);
        }
    }

    public record UploadResult(String publicId, String secureUrl) {
    }
}
