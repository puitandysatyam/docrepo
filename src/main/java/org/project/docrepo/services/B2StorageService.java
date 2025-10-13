package org.project.docrepo.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.time.Duration;

@Service
public class B2StorageService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner; // --- NEW: Client for creating signed URLs
    private final String bucketName;

    public B2StorageService(@Value("${b2.accessKeyId}") String accessKeyId,
                            @Value("${b2.secretKey}") String secretKey,
                            @Value("${b2.bucketName}") String bucketName,
                            @Value("${b2.region}") String region,
                            @Value("${b2.endpoint}") String endpoint) {

        this.bucketName = bucketName;
        AwsBasicCredentials credentials = AwsBasicCredentials.create(accessKeyId, secretKey);
        StaticCredentialsProvider credentialsProvider = StaticCredentialsProvider.create(credentials);
        URI endpointUri = URI.create(endpoint);

        // Standard S3 client for direct operations like upload, download, delete
        this.s3Client = S3Client.builder()
                .region(Region.of(region))
                .endpointOverride(endpointUri)
                .credentialsProvider(credentialsProvider)
                .build();

        // --- NEW ---
        // A separate presigner client, configured the same way.
        // Its only job is to create secure, temporary URLs.
        this.s3Presigner = S3Presigner.builder()
                .region(Region.of(region))
                .endpointOverride(endpointUri)
                .credentialsProvider(credentialsProvider)
                .build();
    }

    public record B2File(String fileName, String contentType, InputStream inputStream) {}

    /**
     * Uploads a file to the configured Backblaze B2 bucket.
     * @return The unique file NAME (key), NOT the full URL.
     */
    public String uploadFile(MultipartFile file, String fileName) {
        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(fileName)
                    .contentType(file.getContentType())
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));

            // --- KEY CHANGE ---
            // We now return only the file's unique key. This is a more robust design.
            return fileName;

        } catch (IOException e) {
            throw new RuntimeException("Failed to upload file to B2", e);
        }
    }

    // --- NEW METHOD FOR PRE-SIGNED URLS ---
    /**
     * Generates a temporary, secure, pre-signed URL for a private object.
     * This URL can be used in a browser to view the object for a limited time.
     * @param fileName The key of the file in the bucket.
     * @return A pre-signed URL string that expires after a set duration.
     */
    public String generatePresignedUrl(String fileName) {
        // 1. Create a GetObjectRequest, just like in the download method.
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        // 2. Create a presign request, specifying how long the URL should be valid for.
        GetObjectPresignRequest presignRequest = GetObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(10)) // The "guest pass" is valid for 10 minutes.
                .getObjectRequest(getObjectRequest)
                .build();

        // 3. Use the presigner client to create the URL.
        PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(presignRequest);

        // 4. Return the full URL as a string.
        return presignedRequest.url().toString();
    }


    public B2File downloadFile(String fileName) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        var response = s3Client.getObject(getObjectRequest);
        String contentType = response.response().contentType();

        return new B2File(fileName, contentType, response);
    }

    public void deleteFile(String fileName) {
        DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                .bucket(bucketName)
                .key(fileName)
                .build();

        s3Client.deleteObject(deleteObjectRequest);
    }
}

