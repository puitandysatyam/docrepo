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
     * This method runs automatically after the service is created.
     * It checks if the authorization token exists. If not, it triggers the
     * authorization process and stops the application.
     */
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

        // Check if we already have a credential stored.
        Credential credential = flow.loadCredential("user");
        if (credential == null) {
            // If not, we need to authorize.
            authorize();
        }
    }

    /**
     * Triggers the authorization process by printing a URL to the console.
     */
    private void authorize() throws IOException {
        System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.err.println("!!!               AUTHORIZATION REQUIRED               !!!");
        System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.err.println("1. A refresh token is not found in the '" + TOKENS_DIRECTORY_PATH + "' directory.");
        System.err.println("2. Your browser will now open to complete the one-time authorization.");
        System.err.println("3. If it doesn't, please copy the URL printed below into a browser.");
        System.err.println("4. After authorizing, the 'tokens' folder will be created.");
        System.err.println("5. RESTART the Spring Boot application after the token is generated.");
        System.err.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!\n");

        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");

        // We exit here to force the user to restart the application after authorization.
        System.exit(0);
    }

    private Credential getCredentials() throws IOException {
        // Now this method simply loads the credential, which is guaranteed to exist
        // if the application has started successfully.
        return flow.loadCredential("user");
    }

    public String uploadFile(MultipartFile multipartFile, String uniqueFileName) throws IOException, GeneralSecurityException {
        Credential credential = getCredentials();
        HttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();

        Drive driveService = new Drive.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();

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
}
