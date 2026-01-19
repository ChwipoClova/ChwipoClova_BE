package com.chwipoClova.login.service;

import com.chwipoClova.login.dto.ApplePublicKey;
import com.chwipoClova.login.dto.ApplePublicKeyResponse;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Component
public class ApplePublicKeyCache {

    private static final String APPLE_KEYS_URL =
            "https://appleid.apple.com/auth/keys";

    private final RestTemplate restTemplate = new RestTemplate();

    private volatile Map<String, RSAPublicKey> keys = new HashMap<>();

    @PostConstruct
    public void init() {
        refresh();
    }

    public RSAPublicKey get(String kid) {

        RSAPublicKey key = keys.get(kid);
        if (key != null) {
            return key;
        }

        synchronized (this) {
            refresh();
        }

        key = keys.get(kid);
        if (key == null) {
            throw new IllegalArgumentException("Apple public key not found");
        }

        return key;
    }

    private void refresh() {
        ApplePublicKeyResponse response =
                restTemplate.getForObject(APPLE_KEYS_URL, ApplePublicKeyResponse.class);

        Map<String, RSAPublicKey> map = new HashMap<>();
        for (ApplePublicKey key : Objects.requireNonNull(response).getKeys()) {
            map.put(key.getKid(), toPublicKey(key));
        }
        keys = map;
    }

    private RSAPublicKey toPublicKey(ApplePublicKey key) {
        try {
            BigInteger n = new BigInteger(1, Base64.getUrlDecoder().decode(key.getN()));
            BigInteger e = new BigInteger(1, Base64.getUrlDecoder().decode(key.getE()));

            return (RSAPublicKey) KeyFactory.getInstance("RSA")
                    .generatePublic(new RSAPublicKeySpec(n, e));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
