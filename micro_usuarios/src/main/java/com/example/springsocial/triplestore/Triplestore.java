package com.example.springsocial.triplestore;

import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

public class Triplestore {

    String url = "https://sd-90ffdb51.stardog.cloud:5820/Turismo2";
    private static String getBasicAuthenticationHeader() {
        String email = "ricardo.jarro98@ucuenca.edu.ec";
        String pass = "Chocolate619@";
        String valueToEncode = email + ":" + pass;
        return "Basic " + Base64.getEncoder().encodeToString(valueToEncode.getBytes());
    }

    public ResponseEntity<?> PostToTriplestore(String query){
        MultiValueMap<String, String> headers = new LinkedMultiValueMap<String, String>();
        Map<String, String> map = new HashMap<>();
        map.put("Content-Type", "application/sparql-update");
        map.put("Authorization", getBasicAuthenticationHeader());
        headers.setAll(map);
        HttpEntity<?> request = new HttpEntity<>(query, headers);

        ResponseEntity<?> response = new RestTemplate().postForEntity(url +"/update", request, String.class);
        System.out.println(response);
        return response;
    }
}
