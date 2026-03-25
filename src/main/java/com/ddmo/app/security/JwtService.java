package com.ddmo.app.security;

import com.ddmo.app.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final SecureRandom secureRandom = new SecureRandom();

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    public String generateToken(String username, long tenantId) {
        String encryptedTenantId = encryptTenantId(String.valueOf(tenantId));
        Instant now = Instant.now();
        return Jwts.builder()
            .subject(username)
            .claims(Map.of("tid_enc", encryptedTenantId))
            .issuedAt(Date.from(now))
            .expiration(Date.from(now.plusSeconds(jwtProperties.getExpireMinutes() * 60)))
            .signWith(signKey())
            .compact();
    }

    public long parseTenantId(String token) {
        Claims claims = Jwts.parser()
            .verifyWith(signKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
        String encryptedTenantId = claims.get("tid_enc", String.class);
        if (encryptedTenantId == null || encryptedTenantId.isBlank()) {
            throw new IllegalArgumentException("token 缺少租户信息");
        }
        return Long.parseLong(decryptTenantId(encryptedTenantId));
    }

    private SecretKey signKey() {
        byte[] keyBytes = jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    private String encryptTenantId(String tenantId) {
        try {
            byte[] iv = new byte[12];
            secureRandom.nextBytes(iv);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey(), new GCMParameterSpec(128, iv));
            byte[] encrypted = cipher.doFinal(tenantId.getBytes(StandardCharsets.UTF_8));
            byte[] packed = new byte[iv.length + encrypted.length];
            System.arraycopy(iv, 0, packed, 0, iv.length);
            System.arraycopy(encrypted, 0, packed, iv.length, encrypted.length);
            return Base64.getUrlEncoder().withoutPadding().encodeToString(packed);
        } catch (Exception e) {
            throw new IllegalStateException("租户ID加密失败", e);
        }
    }

    private String decryptTenantId(String encryptedText) {
        try {
            byte[] packed = Base64.getUrlDecoder().decode(encryptedText);
            byte[] iv = new byte[12];
            byte[] encrypted = new byte[packed.length - 12];
            System.arraycopy(packed, 0, iv, 0, 12);
            System.arraycopy(packed, 12, encrypted, 0, encrypted.length);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, aesKey(), new GCMParameterSpec(128, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalArgumentException("token 中租户信息非法");
        }
    }

    private SecretKeySpec aesKey() {
        try {
            byte[] raw = jwtProperties.getTenantAesKey().getBytes(StandardCharsets.UTF_8);
            byte[] digest = MessageDigest.getInstance("SHA-256").digest(raw);
            byte[] key = new byte[16];
            System.arraycopy(digest, 0, key, 0, 16);
            return new SecretKeySpec(key, "AES");
        } catch (Exception e) {
            throw new IllegalStateException("AES密钥生成失败", e);
        }
    }
}
