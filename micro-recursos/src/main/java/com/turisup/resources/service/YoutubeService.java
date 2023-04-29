package com.turisup.resources.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.youtube.YouTube;
import com.google.api.services.youtube.model.Video;
import com.google.api.services.youtube.model.VideoSnippet;


import java.io.*;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

public class YoutubeService {

    private static final String CLIENT_SECRETS= "/client_secrets.json";
    private static final String CREDENTIALS="/credentials.json";
    private static final Collection<String> SCOPES =
            Arrays.asList("https://www.googleapis.com/auth/youtube.upload");
    private static final String APPLICATION_NAME = "API code samples";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    public static Credential authorize(final NetHttpTransport httpTransport) throws IOException {
        // Load client secrets.
        /*
        InputStream in = ApiExample.class.getResourceAsStream(CLIENT_SECRETS);
        System.out.println(in);
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(httpTransport, JSON_FACTORY, clientSecrets, SCOPES)
                        .build();*/

        InputStream in = YoutubeService.class.getResourceAsStream(CLIENT_SECRETS);
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        Credential credential2 = null;
        credential2 = new GoogleCredential.Builder()
                .setClientSecrets(clientSecrets)
                .setTransport(new NetHttpTransport())
                .setJsonFactory(JSON_FACTORY)
                .build();
        credential2.setAccessToken("ya29.a0Ael9sCO8b77WDMYE5fxvxNPtf1k2oLqHO3h4-w8aBn-yRAMZsgdkk-f8VAzR6z55LnlBRZco24ugdtLsmMw2LwWA0Tq234ybNikJ-IKQP2gpdLNyuYT1Zl924eSOqGq2rW9yUw9359fTaLxvVtPrCDaaLD_zaCgYKAR0SARISFQF4udJhxZI80x1diQe6l14IYmpWkw0163");
        credential2.setRefreshToken("1//05kopqxVpPPmpCgYIARAAGAUSNwF-L9IrAz7GARAmIc1IdOY6KreC2EsGwkv3OsIb4cMLPT-cpoYQxvw5_LtO28fatYzw6LrMNsg");
        credential2.setExpirationTimeMilliseconds(1649194824000L);


        InputStream in2 = YoutubeService.class.getResourceAsStream(CREDENTIALS);


        /*
        Credential credential =
               new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");*/





        return credential2;
    }

    /**
     * Build and return an authorized API client service.
     *
     * @return an authorized API client service
     * @throws GeneralSecurityException, IOException
     */


    public static YouTube getService() throws GeneralSecurityException, IOException {
        final NetHttpTransport httpTransport = GoogleNetHttpTransport.newTrustedTransport();
        Credential credential = authorize(httpTransport);
        return new YouTube.Builder(httpTransport, JSON_FACTORY, credential)
                .setApplicationName(APPLICATION_NAME)
                .build();
    }
    public String UploadVideo(String filePath, String name) throws GeneralSecurityException, IOException {

        YouTube youtubeService = getService();

        // Define the Video object, which will be uploaded as the request body.
        Video video = new Video();
        VideoSnippet videoSnippet = new VideoSnippet();
        videoSnippet.setTitle("name");
        videoSnippet.setDescription("");
        videoSnippet.setCategoryId("22");
        //video.setSnippet(videoSnippet);

        File mediaFile = new File(filePath);
        InputStreamContent mediaContent =
                new InputStreamContent("application/octet-stream",
                        new BufferedInputStream(new FileInputStream(mediaFile)));
        mediaContent.setLength(mediaFile.length());

        // Define and execute the API request

        YouTube.Videos.Insert request = youtubeService.videos()
                .insert(new ArrayList<>(), video, mediaContent);
        Video response = request.execute();

        System.out.println(response.getId());
        return  "todo OK";
    }
}
