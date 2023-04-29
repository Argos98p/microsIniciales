package com.example.springsocial.security;

import com.example.springsocial.config.AppProperties;
import com.example.springsocial.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.lang.Assert;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Service;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class GoogleIdAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    private static final long serialVersionUID = 1L;
    private String tokenParamName = "googleIdToken";
     AppProperties appProperties ;

    /**
     * Creates an instance which will authenticate against the supplied
     * {@code AuthenticationManager} and which will ignore failed authentication attempts,
     * allowing the request to proceed down the filter chain.
     *
     * @param authenticationManager     the bean to submit authentication requests to
     * @param defaultFilterProcessesUrl the url to check for auth requests on (e.g. /login/google)
     * @param userRepositorya
     */
    UserRepository userRepository;
    @Autowired
    TokenProvider tokenProvider;

    String myToken="";
    public GoogleIdAuthenticationFilter(AuthenticationManager authenticationManager, String defaultFilterProcessesUrl, UserRepository userRepository) {
        super(defaultFilterProcessesUrl);
        Assert.notNull(authenticationManager, "authenticationManager cannot be null");
        setAuthenticationManager(authenticationManager);
        this.userRepository= userRepository;
    }


    public String getTokenParamName() {
        return tokenParamName;
    }

    public void setTokenParamName(String tokenParamName) {
        this.tokenParamName = tokenParamName;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        String token = request.getParameter(tokenParamName);

        if (token == null) {

            return null;
        }

        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Google ID Token Authorization parameter found with value '" + token + "'");
        }

        Object details = this.authenticationDetailsSource.buildDetails(request);

        GoogleIdAuthenticationToken authRequest = new GoogleIdAuthenticationToken(token, details);


        GoogleIdAuthenticationProvider googleIdAuthenticationProvider = new GoogleIdAuthenticationProvider(this.userRepository);
        Authentication authResult = googleIdAuthenticationProvider.authenticate(authRequest);
        if(authResult.isAuthenticated()){

            UserPrincipal userPrincipal = (UserPrincipal) authResult.getPrincipal();
            TokenProvider tokenProvider1 = new TokenProvider(new AppProperties());
             myToken = tokenProvider1.createToken(authResult);
            System.out.println(myToken);
        }


        return authResult;
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;


        System.out.println(request.getParameter("googleIdToken"));

        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> errorDetails = new HashMap<>();
        Authentication auth = this.attemptAuthentication((HttpServletRequest) request, (HttpServletResponse) response);


        errorDetails.put("token", myToken);
        errorDetails.put("userData", auth.getPrincipal());
        httpServletResponse.setStatus(HttpStatus.OK.value());
        httpServletResponse.setContentType(MediaType.APPLICATION_JSON_VALUE);
        mapper.writeValue(httpServletResponse.getWriter(), errorDetails);

    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        super.successfulAuthentication(request, response, chain, authResult);
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("message", "valid token");

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    }



    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        super.unsuccessfulAuthentication(request, response, failed);
        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("message", "valid token");

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    }
}