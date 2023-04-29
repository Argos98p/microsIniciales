package com.turisup.resources.utils;


import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
public class Request {

    public static ResponseEntity<?> simpleGetRequest(String url, String token){
        RestTemplate restTemplate = new RestTemplate();
        // create headers
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", token);
        HttpEntity request = new HttpEntity(headers);
        ResponseEntity<?> response = restTemplate.exchange(
                url,
                HttpMethod.GET,
                request,
                String.class,
                1
        );
        return response;
    }
}
