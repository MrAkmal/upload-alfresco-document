package com.example.uploadalfrescodocument.alfresco;


import com.example.uploadalfrescodocument.config.AlfrescoConfig;
import com.example.uploadalfrescodocument.config.EncryptionConfig;
import com.example.uploadalfrescodocument.dto.DeleteDocumentDTO;
import com.example.uploadalfrescodocument.dto.FileUploadDTO;
import com.example.uploadalfrescodocument.service.EncryptionService;
import lombok.SneakyThrows;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class AlfrescoService {

    private final AlfrescoConfig config;
    private final EncryptionConfig encryptionConfig;

    private final EncryptionService encryptionService;
    private final KeyPair keyPair;


    @Autowired
    public AlfrescoService(AlfrescoConfig config, EncryptionConfig encryptionConfig, EncryptionService encryptionService, KeyPair keyPair) {
        this.config = config;
        this.encryptionConfig = encryptionConfig;
        this.encryptionService = encryptionService;
        this.keyPair = keyPair;
    }

    public String uploadFile(FileUploadDTO dto) {


        Folder userTypeFolder = findByUserType(dto.getUserType());

        if (userTypeFolder.getName().equals(config.disputeDocumentFolderName)
                || userTypeFolder.getName().equals(config.amendmentDocumentFolderName)) {
            return uploadFileBasedOnUserType(dto, userTypeFolder);
        } else {
            throw new RuntimeException("Wrong userType");
        }

    }


    private String uploadFileBasedOnUserType(FileUploadDTO dto, Folder userTypeFolder) {


        Folder disputeIdFolder = checkFolder(userTypeFolder, String.valueOf(dto.getCommonId()));


        if (Objects.isNull(disputeIdFolder)) {
            disputeIdFolder = createFolder(userTypeFolder, String.valueOf(dto.getCommonId()));
        }

        Folder userIdFolder = checkFolder(disputeIdFolder, String.valueOf(dto.getUserId()));

        if (Objects.isNull(userIdFolder)) {
            userIdFolder = createFolder(disputeIdFolder, String.valueOf(dto.getUserId()));
        }

        return uploadDocument(userIdFolder, dto);


    }

    @SneakyThrows
    private String uploadDocument(Folder parentFolder, FileUploadDTO dto) {

        String originalFilename = dto.getFile().getOriginalFilename();

        Map<String, Object> properties = new HashMap<>();

        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        properties.put(PropertyIds.NAME, originalFilename);

        MultipartFile multipartFile = dto.getFile();

        byte[] realContent = multipartFile.getInputStream().readAllBytes();

        ContentStream contentStream = null;
        if (dto.getAlgorithm().equals("AES")) {
            contentStream = getAESContentStream(multipartFile, realContent);
        } else if (dto.getAlgorithm().equals("RSA")) {
            contentStream = getRSAContentStream(multipartFile, realContent);
        } else if (dto.getAlgorithm().equals("TripleDES")) {
            contentStream = getDESContentStream(multipartFile, realContent);
        } else {
            contentStream = new ContentStreamImpl(multipartFile.getOriginalFilename(),
                    BigInteger.valueOf(realContent.length), multipartFile.getContentType(), new ByteArrayInputStream(realContent));
        }


        Document document = parentFolder.createDocument(properties, contentStream, VersioningState.MAJOR);
        return document.getId();

    }

    @SneakyThrows
    private ContentStream getRSAContentStream(MultipartFile multipartFile, byte[] realContent) {


        PublicKey aPublic = keyPair.getPublic();
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, aPublic);

        byte[] encryptedContent = cipher.doFinal(realContent);

        return new ContentStreamImpl(multipartFile.getOriginalFilename(),
                BigInteger.valueOf(encryptedContent.length), multipartFile.getContentType(),
                new ByteArrayInputStream(encryptedContent));
    }

    @SneakyThrows
    private ContentStream getDESContentStream(MultipartFile multipartFile, byte[] realContent) {


        SecretKeySpec secretKeySpec = new SecretKeySpec(encryptionConfig.tdesSecretKey.getBytes(), "TripleDES");

        Cipher cipher = Cipher.getInstance("TripleDES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

        byte[] encryptedContent = cipher.doFinal(realContent);

        return new ContentStreamImpl(multipartFile.getOriginalFilename(),
                BigInteger.valueOf(encryptedContent.length), multipartFile.getContentType(),
                new ByteArrayInputStream(encryptedContent));
    }

    private ContentStream getAESContentStream(MultipartFile multipartFile, byte[] realContent) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKey secretKey = new SecretKeySpec(encryptionConfig.aesSecretKey.getBytes(), "AES");

        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] encryptedContent = cipher.doFinal(realContent);

        return new ContentStreamImpl(multipartFile.getOriginalFilename(),
                BigInteger.valueOf(encryptedContent.length), multipartFile.getContentType(), new ByteArrayInputStream(encryptedContent));
    }

    private Folder checkFolder(Folder parentFolder, String folderName) {
        for (CmisObject child : parentFolder.getChildren()) {
            if (child.getName().equals(folderName) && child instanceof Folder) {
                return (Folder) child;
            }
        }

        return null;

    }

    private Folder createFolder(Folder parentFolder, String folderName) {


        Map<String, Object> properties = new HashMap<>();

        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:folder");
        properties.put(PropertyIds.NAME, folderName);

        return parentFolder.createFolder(properties);

    }

    private Folder findByUserType(String userType) {

        for (CmisObject child : config.getEgpFolder().getChildren()) {
            if (child.getName().equals(userType)) return (Folder) child;
        }
        throw new RuntimeException("UserType folder not found");
    }

    public void deleteDocument(DeleteDocumentDTO dto) {

        Document documentByName = getDocument(dto);
        encryptionService.deleteDocument(documentByName);
        config.session.delete(documentByName);

    }

    private Document getDocumentByName(Folder parentFolder, String documentName) {

        for (CmisObject child : parentFolder.getChildren()) {
            if (child instanceof Document && child.getName().equals(documentName)) return (Document) child;
        }
        throw new RuntimeException("Document not found with name " + documentName);

    }

    @SneakyThrows
    public Document findByDto(DeleteDocumentDTO dto, String version) {

        Document documentByName = getDocument(dto);

        documentByName.getAllVersions().forEach(System.out::println);

        Document document = null;
        if (version != null) {
            document = (Document) config.session.getObject(documentByName.getId().substring(0, documentByName.getId().indexOf(";")) + ";" + version);
        } else {
            document = (Document) config.session.getObject(documentByName.getId());
        }
        return document;

    }

    private Document getDocument(DeleteDocumentDTO dto) {
        String folderName = dto.getFolderName();

        Folder userType = findByUserType(folderName);

        Folder commonFolder = checkFolder(userType, String.valueOf(dto.getCommonId()));

        if (Objects.isNull(commonFolder)) throw new RuntimeException("Folder with " + dto.getCommonId() + " not found");

        Folder userIdFolder = checkFolder(commonFolder, String.valueOf(dto.getUserId()));

        if (Objects.isNull(userIdFolder)) throw new RuntimeException("Folder with " + dto.getUserId() + " not found");

        return getDocumentByName(userIdFolder, dto.getDocumentName());
    }
}
