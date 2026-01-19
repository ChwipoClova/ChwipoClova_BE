package com.chwipoClova.login.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class ApplePublicKeyResponse {
    private List<ApplePublicKey> keys;
}
