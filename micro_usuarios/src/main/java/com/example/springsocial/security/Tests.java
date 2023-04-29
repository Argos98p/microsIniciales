package com.example.springsocial.security;

import com.example.springsocial.SpringSocialApplication;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.springframework.boot.SpringApplication;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;

public class Tests {
    public static void main(String[] args) {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(),new GsonFactory())
                .setAudience(Collections.singletonList("100416626035-15m8dfdvjhe1iv114l3tdcrpdj299g4r.apps.googleusercontent.com"))
                //.setAudience(Arrays.asList("100416626035-15m8dfdvjhe1iv114l3tdcrpdj299g4r.apps.googleusercontent.com", "100416626035-lfmvok83nlvjpsfmu67t7g69bp0lg7g0.apps.googleusercontent.com"))
                .build();

        try {
            GoogleIdToken idToken = verifier.verify("eyJhbGciOiJSUzI1NiIsImtpZCI6Ijk4NmVlOWEzYjc1MjBiNDk0ZGY1NGZlMzJlM2U1YzRjYTY4NWM4OWQiLCJ0eXAiOiJKV1QifQ.eyJpc3MiOiJodHRwczovL2FjY291bnRzLmdvb2dsZS5jb20iLCJhenAiOiIxMDA0MTY2MjYwMzUtbGZtdm9rODNubHZqcHNmbXU2N3Q3ZzY5YnAwbGc3ZzAuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJhdWQiOiIxMDA0MTY2MjYwMzUtMTVtOGRmZHZqaGUxaXYxMTRsM3RkY3JwZGoyOTlnNHIuYXBwcy5nb29nbGV1c2VyY29udGVudC5jb20iLCJzdWIiOiIxMTUxNjM5NTE2ODAyNDg1NDMxNjIiLCJlbWFpbCI6InJpY2FyZG8uamFycm85OEBnbWFpbC5jb20iLCJlbWFpbF92ZXJpZmllZCI6dHJ1ZSwibmFtZSI6IlJpY2FyZG8gSmFycm8iLCJwaWN0dXJlIjoiaHR0cHM6Ly9saDMuZ29vZ2xldXNlcmNvbnRlbnQuY29tL2EvQUdObXl4WTlaaUVoQXdKdG5zd3ZhQXlLWWN4dktmMnhxOHAzYmtnUDAwU2I9czk2LWMiLCJnaXZlbl9uYW1lIjoiUmljYXJkbyIsImZhbWlseV9uYW1lIjoiSmFycm8iLCJsb2NhbGUiOiJlcy00MTkiLCJpYXQiOjE2Nzk1MTc2NDksImV4cCI6MTY3OTUyMTI0OX0.TFgmYp94GQR5McfNLi7RsQHpZkvFVKVej7_r-BHMgL9ln-I7GRe2jqbcuUyF_uZfWLIeJ69rKALPbgzMtiSOzgn4WurRsXhfJLX9DbvgzJBX5r27vgR7k1zoqe-dExRpaT8fspYuop1amXEMUmXEA3mVr3b-y13fTb7ozhPikGJjblWR3Sx89VQ9DgW9sg8wQWat2dkbm8AxhRVHG1crD6QrNj7m8G6FMzowJYfEr2PMWy62BndChKpa3hVh2pY1O1ATzhpkhZWiHbffGDYl1oSjDBboHuUZH0r7zTO2rzIhlAqzaeTLvEQXWm9ZgNhk_dyvWAUBmTSy2pn9-5YCPw");
            System.out.println(idToken);
        } catch (GeneralSecurityException | IOException e) {
            throw new RuntimeException(e);
        }
    }
}
