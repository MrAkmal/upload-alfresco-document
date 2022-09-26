package com.example.uploadalfrescodocument.service;


import com.example.uploadalfrescodocument.config.EncryptionConfig;
import com.example.uploadalfrescodocument.dto.BackUpChangeDeleteTimeDTO;
import com.example.uploadalfrescodocument.entity.EncryptedDocument;
import com.example.uploadalfrescodocument.repository.EncryptedDocumentRepository;
import lombok.SneakyThrows;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.ObjectId;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.security.KeyPair;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class EncryptionService {


    private final EncryptionConfig encryptionConfig;
    private final EncryptedDocumentRepository encryptedDocumentRepository;

    private final KeyPair keyPair;

    private final RestTemplate restTemplate;


    @Autowired
    public EncryptionService(EncryptionConfig encryptionConfig, EncryptedDocumentRepository encryptedDocumentRepository, KeyPair keyPair, RestTemplate restTemplate) {
        this.encryptionConfig = encryptionConfig;
        this.encryptedDocumentRepository = encryptedDocumentRepository;
        this.keyPair = keyPair;
        this.restTemplate = restTemplate;
    }


    @SneakyThrows
    public byte[] getDecryptedContentBytes(Document document, String encryptionAlgorithm) {

        byte[] decryptedContent = null;
        if (encryptionAlgorithm.equals("AES")) {
            SecretKey secretKey = new SecretKeySpec(encryptionConfig.aesSecretKey.getBytes(), encryptionAlgorithm);

            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            ContentStream contentStream = document.getContentStream();

            InputStream inputStream = contentStream.getStream();

            byte[] bytes = IOUtils.toByteArray(inputStream);

            decryptedContent = cipher.doFinal(bytes);
        } else if (encryptionAlgorithm.equals("RSA")) {

            ContentStream contentStream = document.getContentStream();

            InputStream inputStream = contentStream.getStream();

            byte[] bytes = IOUtils.toByteArray(inputStream);

            Cipher cipher = Cipher.getInstance("RSA");
            cipher.init(Cipher.DECRYPT_MODE, keyPair.getPrivate());
            decryptedContent = cipher.doFinal(bytes);

        } else if (encryptionAlgorithm.equals("TripleDES")) {
            SecretKey secretKey = new SecretKeySpec(encryptionConfig.tdesSecretKey.getBytes(), encryptionAlgorithm);

            Cipher cipher = Cipher.getInstance("TripleDES");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            ContentStream contentStream = document.getContentStream();

            InputStream inputStream = contentStream.getStream();

            byte[] bytes = IOUtils.toByteArray(inputStream);

            decryptedContent = cipher.doFinal(bytes);

        } else {
            InputStream inputStream = document.getContentStream().getStream();
            decryptedContent = IOUtils.toByteArray(inputStream);
        }

        return decryptedContent;
    }

    public String getAlgorithmByDocumentId(String documentId) {


        Optional<EncryptedDocument> optional = encryptedDocumentRepository.findByDocumentId(documentId);

        if (optional.isEmpty()) throw new RuntimeException("Algorithm not found");

        return optional.get().getAlgorithm();

    }

    public void deleteDocument(Document documentByName) {

        List<Document> allVersions = documentByName.getAllVersions();

        List<String> documentIds = allVersions.stream()
                .map(ObjectId::getId)
                .toList();

        encryptedDocumentRepository.deleteEncryptedDocumentsByDocumentIdIsIn(documentIds);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<BackUpChangeDeleteTimeDTO> entity = new HttpEntity<>(new BackUpChangeDeleteTimeDTO(documentIds,
                LocalDateTime.now()),headers);

        restTemplate.exchange("http://localhost:1515/v1/back_up/delete", HttpMethod.POST,entity,Void.class);

    }

    public void saveEncryption(String documentId, String algorithm) {
        encryptedDocumentRepository.save(new EncryptedDocument(documentId, algorithm));
    }
}
