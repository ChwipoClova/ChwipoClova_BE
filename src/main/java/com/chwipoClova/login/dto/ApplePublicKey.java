package com.chwipoClova.login.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApplePublicKey {
    private String kid;
    private String alg;
    private String n;
    private String e;
}
