package com.chwipoClova.login.dto;

import lombok.Data;

@Data
public class AppleLoginDto {
    private String identityToken;
    private String email;
    private String name;
}
