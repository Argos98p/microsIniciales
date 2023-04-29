package com.example.springsocial.payload;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class GoogleLoginRequest {
    @NotNull
    @NotBlank
    String googleIdToken;

}
