package com.milvus.vector_spring.common.service;

import com.milvus.vector_spring.common.apipayload.status.ErrorStatus;
import com.milvus.vector_spring.common.exception.CustomException;
import com.milvus.vector_spring.util.properties.CommonProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Service
@RequiredArgsConstructor
@Slf4j
public class EncryptionService {

    private final CommonProperties commonProperties;

    public String encryptData(String data) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            byte[] keyBytes = commonProperties.secretKey().getBytes(StandardCharsets.UTF_8);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

            byte[] iv = new byte[commonProperties.ivSize()];
            new java.security.SecureRandom().nextBytes(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            byte[] encryptedData = cipher.doFinal(data.getBytes(StandardCharsets.UTF_8));

            byte[] encryptedWithIv = new byte[commonProperties.ivSize() + encryptedData.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, commonProperties.ivSize());
            System.arraycopy(encryptedData, 0, encryptedWithIv, commonProperties.ivSize(), encryptedData.length);

            return Base64.getEncoder().encodeToString(encryptedWithIv);
        } catch (Exception e) {
            throw new CustomException(ErrorStatus.ENCRYPTION_ERROR);
        }
    }

    public String decryptData(String encryptedData) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            byte[] decodedData = Base64.getDecoder().decode(encryptedData);

            byte[] iv = new byte[commonProperties.ivSize()];
            System.arraycopy(decodedData, 0, iv, 0, commonProperties.ivSize());
            IvParameterSpec ivSpec = new IvParameterSpec(iv);

            byte[] encryptedContent = new byte[decodedData.length - commonProperties.ivSize()];
            System.arraycopy(decodedData, commonProperties.ivSize(), encryptedContent, 0, encryptedContent.length);

            byte[] keyBytes = commonProperties.secretKey().getBytes(StandardCharsets.UTF_8);
            SecretKeySpec keySpec = new SecretKeySpec(keyBytes, "AES");

            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            byte[] decryptedData = cipher.doFinal(encryptedContent);

            return new String(decryptedData, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new CustomException(ErrorStatus.DECRYPTION_ERROR);
        }
    }
}
