package org.project.docrepo.services;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.util.Map;

@Service
public class CloudinaryStorageService {

    private final Cloudinary cloudinary;

    /**
     * CORRECTED CONSTRUCTOR:
     * This now uses Spring's @Value annotation to safely inject the properties
     * from your application.properties file. This is the standard and most reliable way.
     */
    public CloudinaryStorageService(
            @Value("${cloudinary.cloud_name}") String cloudName,
            @Value("${cloudinary.api_key}") String apiKey,
            @Value("${cloudinary.api_secret}") String apiSecret) {

        this.cloudinary = new Cloudinary(ObjectUtils.asMap(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret,
                "secure", true
        ));
    }

    /**
     * Uploads a profile image to Cloudinary.
     * @param file The image file to upload.
     * @param publicId A unique ID for the image (we'll use the user's ID).
     * @return The secure URL of the uploaded and optimized image.
     */
    public String uploadProfileImage(MultipartFile file, String publicId) {
        try {
            Map uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    // This creates a clean folder structure in your Cloudinary account
                    "public_id", "docrepo/profile_images/" + publicId,
                    // Replaces the old image if a user uploads a new one
                    "overwrite", true,
                    // Automatically crops the image to a 400x400 square, focusing on the face
                    "transformation", "w_400,h_400,c_fill,g_face"
            ));

            return (String) uploadResult.get("secure_url");

        } catch (IOException e) {
            throw new RuntimeException("Could not upload file to Cloudinary", e);
        }
    }
}

