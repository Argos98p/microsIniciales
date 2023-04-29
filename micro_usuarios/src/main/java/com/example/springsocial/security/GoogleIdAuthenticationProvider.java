package com.example.springsocial.security;

import com.example.springsocial.exception.OAuth2AuthenticationProcessingException;
import com.example.springsocial.model.AuthProvider;
import com.example.springsocial.model.User;
import com.example.springsocial.repository.UserRepository;
import com.example.springsocial.security.oauth2.user.GoogleOAuth2UserInfo;
import com.example.springsocial.triplestore.QueryTemplates;
import com.example.springsocial.triplestore.Triplestore;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.MessageSourceAccessor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.SpringSecurityMessageSource;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;


public class GoogleIdAuthenticationProvider implements AuthenticationProvider {


    Triplestore triplestore= new Triplestore();

    private UserRepository userRepository;
    HttpTransport httpTransport = new NetHttpTransport();

    JsonFactory jsonFactory = new JacksonFactory();
    protected MessageSourceAccessor messages = SpringSecurityMessageSource.getAccessor();
    private static final Logger logger = (Logger) LoggerFactory.getLogger(GoogleIdAuthenticationProvider.class);
    private String clientId;

    public GoogleIdAuthenticationProvider(UserRepository userRepository) {
        this.userRepository=userRepository;
    }


    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!supports(authentication.getClass())) {
            if (logger.isDebugEnabled()) {
                logger.debug(String.format("This authentication provider does not support instances of type %s", authentication.getClass().getName()));
            }
            return null;
        }
        GoogleIdAuthenticationToken googleIdAuthenticationToken = (GoogleIdAuthenticationToken) authentication;

        if (logger.isDebugEnabled())
            logger.debug(String.format("Validating google login with token '%s'", googleIdAuthenticationToken.getCredentials()));


        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(httpTransport, jsonFactory)
                .setAudience(Collections.singletonList("100416626035-15m8dfdvjhe1iv114l3tdcrpdj299g4r.apps.googleusercontent.com"))
                .build();

        System.out.println((String) googleIdAuthenticationToken.getCredentials());
        GoogleIdToken idToken = null;
        try {
            idToken = verifier.verify((String) googleIdAuthenticationToken.getCredentials());
            System.out.println(idToken);
        } catch (GeneralSecurityException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (idToken != null) {
            Payload payload = idToken.getPayload();

            Map<String, Object> googleUserInfo = new HashMap<>();
            googleUserInfo.put("sub",(String) payload.get("sub"));
            googleUserInfo.put("name",(String) payload.get("name"));
            googleUserInfo.put("email",(String) payload.get("email"));
            googleUserInfo.put("picture",(String) payload.get("picture"));


            String email = payload.getEmail();
            GoogleOAuth2UserInfo googleOAuth2UserInfo = new GoogleOAuth2UserInfo(googleUserInfo);


            Optional<User> userOptional = userRepository.findByEmail(email);
            User user;
            if(userOptional.isPresent()) {
                user = userOptional.get();
                /*
                if(!user.getProvider().equals(AuthProvider.google)) {
                    throw new OAuth2AuthenticationProcessingException("Looks like you're signed up with " +
                            user.getProvider() + " account. Please use your " + user.getProvider() +
                            " account to login.");
                }*/
                user = updateExistingUser(user, payload);
            } else {
                user = registerNewUser(payload);
            }
            System.out.println(user);
            return new GoogleIdAuthenticationToken((String) googleIdAuthenticationToken.getCredentials(), new UserPrincipal(user.getId(),user.getEmail(),"",new ArrayList<>()),new ArrayList<>() , authentication.getDetails());


        } else {
            System.out.println("Invalid ID token.");
        }
            return null;


    }

    private User registerNewUser(Payload payload) {
        System.out.println("new user");
        User user = new User();

        user.setProvider(AuthProvider.valueOf("google"));
        user.setProviderId((String) payload.get("sub"));
        user.setName((String) payload.get("name"));
        user.setEmail(payload.getEmail());
        user.setImageUrl((String) payload.get("picture"));

        User user1=userRepository.save(user);

        String query = QueryTemplates.queryInsertUser(user1);

        ResponseEntity<?> response = triplestore.PostToTriplestore(query);
        System.out.println(response);

        return user1;
    }

    private User updateExistingUser(User existingUser, Payload payload) {
        System.out.println("update");
        existingUser.setName((String) payload.get("name"));
        existingUser.setImageUrl((String) payload.get("picture"));
        return userRepository.save(existingUser);
    }

    @Override
    public boolean supports(Class<? extends Object> authentication) {
        return (GoogleIdAuthenticationToken.class.isAssignableFrom(authentication));
    }
    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }
}
