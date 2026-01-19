package com.chwipoClova.login.dto;

import lombok.Data;

@Data
public class GoogleLoginDto {
    private String sub;
    private String email;
    private String name;
    private String picture;
}
