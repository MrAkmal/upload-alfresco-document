package com.example.uploadalfrescodocument;


import lombok.SneakyThrows;
import org.apache.commons.io.IOUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.FileReader;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

public class EncryptTest {
//
//    public static final String encryptAlgorithm = "AES";
//    public static final String secretKey = "123";

    @SneakyThrows
    public static void test(MultipartFile file) {


        byte[] text = file.getInputStream().readAllBytes();


        byte[] encode = Base64.getEncoder().encode("456145afasfsa".getBytes());

        SecretKey secretKey = new  SecretKeySpec("1234567812345678".getBytes(),"AES");


        Cipher cipher = Cipher.getInstance("AES");




//        byte[] text = "No body can see me.".getBytes(StandardCharsets.UTF_8);


        System.out.println("text = " + text.toString());

        cipher.init(Cipher.ENCRYPT_MODE,secretKey);

        byte[] encryptedText = cipher.doFinal(text);

        System.out.println("encryptedText.length = " + encryptedText.length);

        System.out.println("encryptedText = " + encryptedText.toString());
        String str = IOUtils.toString(encryptedText);

        cipher.init(Cipher.DECRYPT_MODE,secretKey);
        byte[] decryptedText = cipher.doFinal(encryptedText);

        System.out.println("decryptedText = " + decryptedText.toString());

        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(decryptedText);

        String str1 = IOUtils.toString(byteArrayInputStream, StandardCharsets.UTF_8);

        System.out.println("str = " + str);
        System.out.println("str1 = " + str1);


//        encryptSHA();

    }


//    @SneakyThrows
//    public void encryptRSA(){
//
//
//
//        Cipher encryptCipher = Cipher.getInstance("RSA");
//        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
//
//    }


    @SneakyThrows
    public static void encryptSHA(){


        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");

        String content = "Salom";

        byte[] encryptedContent = messageDigest.digest(content.getBytes());

        System.out.println("content.getBytes() = " + content.getBytes());

        System.out.println("encryptedContent = " + encryptedContent);


    }




}
