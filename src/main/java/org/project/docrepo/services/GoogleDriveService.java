package org.project.docrepo.services;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Service
public class GoogleDriveService {

    private static final String APPLICATION_NAME = "DocRepo";
    private static final JsonFactory JSON_FACTORY = GsonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";
    private static final List<String> SCOPES = Collections.singletonList(DriveScopes.DRIVE_FILE);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";
    private static final String FOLDER_ID = "10xZ-0m9MZEdNB-__AC7OxybOreQWk3Rx"; // Your Folder ID

    private GoogleAuthorizationCodeFlow flow;

    /**
     * A simple record to hold the data for a downloaded file.
     * This is a clean way to return multiple values from the downloadFile method.
     * @param name The name of the file.
     * @param mimeType The content type of the file (e.g., "application/pdf").
     * @param inputStream The raw data stream of the file.
     */
    public record DriveFile(String name, String mimeType, InputStream inputStream) {}


    @PostConstruct
    public void init() throws IOException, GeneralSecurityException {
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        InputStream in = GoogleDriveService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();

        Credential credential = flow.loadCredential("user");
        if (credential == null) {
            authorize();
        }
    }

    private void authorize() throws IOException {
        System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.err.println("!!!               AUTHORIZATION REQUIRED               !!!");
        // ... (rest of the authorization messages)
        System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

        System.exit(0);
    }

    private Credential getCredentials() throws IOException {
        return flow.loadCredential("user");
    }

    private Drive getDriveService() throws IOException, GeneralSecurityException {
        Credential credential = getCredentials();
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        return new Drive.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }


    public String uploadFile(MultipartFile multipartFile, String uniqueFileName) throws IOException, GeneralSecurityException {
        Drive driveService = getDriveService();

        File fileMetadata = new File();
        fileMetadata.setName(uniqueFileName);
        fileMetadata.setParents(Collections.singletonList(FOLDER_ID));

        InputStreamContent mediaContent = new InputStreamContent(
                multipartFile.getContentType(),
                multipartFile.getInputStream()
        );

        File uploadedFile = driveService.files().create(fileMetadata, mediaContent)
                .setFields("id")
                .execute();

        return uploadedFile.getId();
    }


    // --- NEW METHOD FOR DOWNLOADING FILES ---

    /**
     * Downloads a file from Google Drive.
     * @param fileId The unique ID of the file to download from Google Drive.
     * @return A DriveFile record containing the file's name, MIME type, and its content as an InputStream.
     * @throws IOException If there's a network error.
     * @throws GeneralSecurityException If there's an authentication error.
     */
    public DriveFile downloadFile(String fileId) throws IOException, GeneralSecurityException {
        Drive driveService = getDriveService();

        // First, get the file's metadata to find out its name and MIME type.
        File fileMetadata = driveService.files().get(fileId).setFields("name, mimeType").execute();
        String fileName = fileMetadata.getName();
        String mimeType = fileMetadata.getMimeType();

        // Then, get the file's actual content as an InputStream.
        InputStream inputStream = driveService.files().get(fileId).executeMediaAsInputStream();

        // Return all three pieces of information neatly packaged in our record.
        return new DriveFile(fileName, mimeType, inputStream);
    }
}