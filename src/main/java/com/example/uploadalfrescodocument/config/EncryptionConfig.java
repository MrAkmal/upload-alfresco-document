package com.example.uploadalfrescodocument.config;


import lombok.SneakyThrows;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;

@Component
public class EncryptionConfig {


    @Value("${aes.secret.key}")
    public String aesSecretKey;


    @Value("${rsa.secret.key}")
    private String rsaSecretKey;


    @Value("${sha256.secret.key}")
    private String sha256SecretKey;


    @SneakyThrows
    public byte[] encrypt(InputStream inputStream, String algorithm) {



        byte[] contentBytes = inputStream.readAllBytes();


        byte[] encryptedContent = null;

        if ("AES".equals(algorithm)) {
            encryptedContent= doEncrypt(algorithm, contentBytes, aesSecretKey.getBytes());
        } else if ("RSA".equals(algorithm)) {

            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            KeyPair kp = kpg.generateKeyPair();

            PrivateKey privateKey = kp.getPrivate();
            PublicKey publicKey = kp.getPublic();


//            encryptedContent =  doEncrypt(algorithm, contentBytes, privateKey);
        } else if ("SHA256".equals(algorithm)) {
            encryptedContent = doEncrypt(algorithm, contentBytes, sha256SecretKey.getBytes());
        } else {
            throw new RuntimeException("Wrong algorithm type");
        }

        System.out.println("encryptedContent.toString() = " + encryptedContent.toString());
        System.out.println("encryptedContent.length = " + encryptedContent.length);
        return encryptedContent;
    }

    @SneakyThrows
    private byte[] doEncrypt(String algorithm, byte[] contentBytes, byte[] ownSecretKey) {
        SecretKey secretKey = new SecretKeySpec(ownSecretKey, algorithm);

        Cipher cipher = Cipher.getInstance(algorithm);

        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(contentBytes);
    }


    @SneakyThrows
    public byte[] decrypt(String algorithm, String ownSecretKey,byte[] contentBytes) {

        SecretKey secretKey = new SecretKeySpec(ownSecretKey.getBytes(), algorithm);

        Cipher cipher = Cipher.getInstance(algorithm);

        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        return cipher.doFinal(contentBytes);
    }


}
