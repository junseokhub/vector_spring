package com.milvus.vector_spring.common.service;

import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.config.properties.AppProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class EncryptionService {

    private final AppProperties appProperties;

    public String encryptData(String data) {
        try {
            byte[] keyBytes = appProperties.secretKey().getBytes(StandardCharsets.UTF_8);
            byte[] iv = new byte[appProperties.ivSize()];
            new SecureRandom().nextBytes(iv);

            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), new IvParameterSpec(iv));
            byte[] encrypted = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            byte[] result = new byte[appProperties.ivSize() + encrypted.length];
            System.arraycopy(iv, 0, result, 0, appProperties.ivSize());
            System.arraycopy(encrypted, 0, result, appProperties.ivSize(), encrypted.length);
            return Base64.getEncoder().encodeToString(result);
        } catch (Exception e) {
            throw new CustomException(ErrorStatus.ENCRYPTION_ERROR);
        }
    }

    public String decryptData(String encryptedData) {
        try {
            byte[] decoded = Base64.getDecoder().decode(encryptedData);
            byte[] iv = new byte[appProperties.ivSize()];
            System.arraycopy(decoded, 0, iv, 0, appProperties.ivSize());

            byte[] encryptedContent = new byte[decoded.length - appProperties.ivSize()];
            System.arraycopy(decoded, appProperties.ivSize(), encryptedContent, 0, encryptedContent.length);

            byte[] keyBytes = appProperties.secretKey().getBytes(StandardCharsets.UTF_8);
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(keyBytes, "AES"), new IvParameterSpec(iv));
            return new String(cipher.doFinal(encryptedContent), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CustomException(ErrorStatus.DECRYPTION_ERROR);
        }
    }
}
