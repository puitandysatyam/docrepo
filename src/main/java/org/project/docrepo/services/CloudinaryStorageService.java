package org.project.docrepo.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * This service handles all interactions with the Cloudinary API for file storage.
 */
@Service
public class CloudinaryStorageService {

    private final Cloudinary cloudinary;

    /**
     * The constructor initializes the Cloudinary client.
     * It uses the @Value annotation to securely read your credentials
     * from the application.properties file.
     */
    public CloudinaryStorageService(
            @Value("${cloudinary.cloud_name}") String cloudName,
            @Value("${cloudinary.api_key}") String apiKey,
            @Value("${cloudinary.api_secret}") String apiSecret) {

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        config.put("secure", "true"); // Ensures we always get HTTPS URLs

        this.cloudinary = new Cloudinary(config);
    }

    /**
     * Uploads a profile image file to Cloudinary.
     * @param file The image file uploaded by the user.
     * @param publicId A unique identifier for the image, typically the user's ID.
     * @return The secure, public URL of the uploaded image.
     */
    public String uploadProfileImage(MultipartFile file, String publicId) {
        try {
            // Upload the file to Cloudinary.
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    // We store all images in a 'profile_images' folder for organization.
                    "public_id", "profile_images/" + publicId,
                    // This will replace the image if the user uploads a new one with the same ID.
                    "overwrite", true,
                    "resource_type", "image"
            ));

            // The result from Cloudinary contains the secure URL of the uploaded image.
            return (String) uploadResult.get("secure_url");

        } catch (IOException e) {
            // If the upload fails, we throw an exception.
            throw new RuntimeException("Could not upload file to Cloudinary", e);
        }
    }
}
