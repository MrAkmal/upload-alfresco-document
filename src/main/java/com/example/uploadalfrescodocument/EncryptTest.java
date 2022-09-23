package com.example.uploadalfrescodocument;


import lombok.SneakyThrows;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.security.*;
import java.util.HashMap;
import java.util.Map;


@Component
public class EncryptTest {


    @SneakyThrows
    public static void main(String[] args) {


//        KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
//
//        generator.initialize(2048);
//
//        KeyPair keyPair = generator.genKeyPair();
//
//        PrivateKey aPrivate = keyPair.getPrivate();
//        PublicKey aPublic = keyPair.getPublic();
        byte[] secretKey = "9mng65v8jf4lxn93nabf981m".getBytes();
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey, "TripleDES");


        Cipher cipher = Cipher.getInstance("TripleDES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

        String text = "some";
        byte[] bytes = cipher.doFinal(text.getBytes());

        System.out.println("bytes = " + bytes);


        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

        byte[] decryptedText = cipher.doFinal(bytes);

        System.out.println("IOUtils.toString(decryptedText) = " + IOUtils.toString(decryptedText));
    }


//    @SneakyThrows
//    public static ResponseEntity decrypt() {
//
//        Folder rootFolder = session.getRootFolder();
//
//        Document document = null;
//        for (CmisObject child : rootFolder.getChildren()) {
//            if (child.getId().equals("7123c184-242f-49ae-9d87-0fa2a26e47e6;1.0")) {
//                document = (Document) child;
//                break;
//            }
//        }
//
//        SecretKey secretKey = new SecretKeySpec("1234567812345678".getBytes(), "AES");
//
//
//        Cipher cipher = Cipher.getInstance("AES");
//        cipher.init(Cipher.DECRYPT_MODE, secretKey);
//
//        ContentStream contentStream = document.getContentStream();
//
//        InputStream inputStream = contentStream.getStream();
//
//        byte[] bytes = IOUtils.toByteArray(inputStream);
//
//        byte[] decryptedText = cipher.doFinal(bytes);
//
//
////        ContentStream contentStream1 = new ContentStreamImpl(document.getName(),
////                BigInteger.valueOf(decryptedText.length), document.getContentStreamMimeType(),
////                new ByteArrayInputStream(decryptedText));
////
////
////        document.setContentStream(contentStream1,true);
////
//
//
//        HttpHeaders responseHeaders = new HttpHeaders();
//        responseHeaders.add("content-disposition", "attachment; filename=" +
//                document.getName());
//        responseHeaders.add("Content-Type", document.getContentStreamMimeType());
//        responseHeaders.add("file-name", document.getName());
//
//        return new ResponseEntity(decryptedText, responseHeaders, HttpStatus.OK);
//    }
//
//
////    @SneakyThrows
////    public void encryptRSA(){
////
////
////
////        Cipher encryptCipher = Cipher.getInstance("RSA");
////        encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);
////
////    }
//
//
//    @SneakyThrows
//    public static void encryptSHA() {
//
//
//        MessageDigest messageDigest = MessageDigest.getInstance("SHA-256");
//
//        String content = "Salom";
//
//        byte[] encryptedContent = messageDigest.digest(content.getBytes());
//
//        System.out.println("content.getBytes() = " + content.getBytes());
//
//        System.out.println("encryptedContent = " + encryptedContent);
//
//
//    }
//
//
//    public void some2() {
//
//
//    }


}
