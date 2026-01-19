package com.chwipoClova.login.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.interfaces.RSAPublicKey;
import java.util.Base64;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AppleTokenVerifier {

    private static final String ISSUER = "https://appleid.apple.com";

    private final ApplePublicKeyCache keyCache;

    @Value("${apple.bundle-id}")
    private String bundleId;

    public Claims verify(String identityToken) {

        String kid = extractKid(identityToken);

        RSAPublicKey publicKey = keyCache.get(kid);

        return Jwts.parserBuilder()
                .setSigningKey(publicKey)
                .requireIssuer(ISSUER)
                .requireAudience(bundleId)
                .build()
                .parseClaimsJws(identityToken)
                .getBody();
    }

    private String extractKid(String token) {
        try {
            String header = token.split("\\.")[0];
            String json = new String(Base64.getUrlDecoder().decode(header));
            return (String) new ObjectMapper()
                    .readValue(json, Map.class)
                    .get("kid");
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid Apple token header");
        }
    }
}