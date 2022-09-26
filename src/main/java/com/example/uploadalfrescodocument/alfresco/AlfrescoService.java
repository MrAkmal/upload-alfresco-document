package com.example.uploadalfrescodocument.alfresco;


import com.example.uploadalfrescodocument.config.AlfrescoConfig;
import com.example.uploadalfrescodocument.config.EncryptionConfig;
import com.example.uploadalfrescodocument.dto.*;
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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayInputStream;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class AlfrescoService {

    private final AlfrescoConfig config;

    private final RestTemplate restTemplate;
    private final EncryptionConfig encryptionConfig;

    private final EncryptionService encryptionService;
    private final KeyPair keyPair;

    @Autowired
    public AlfrescoService(AlfrescoConfig config, RestTemplate restTemplate, EncryptionConfig encryptionConfig, EncryptionService encryptionService, KeyPair keyPair) {
        this.config = config;
        this.restTemplate = restTemplate;
        this.encryptionConfig = encryptionConfig;
        this.encryptionService = encryptionService;
        this.keyPair = keyPair;
    }

    public String uploadFile(FileUploadDTO dto, FileDTO fileDTO) {


        Folder userTypeFolder = findByUserType(dto.getUserType());

        if (userTypeFolder.getName().equals(config.disputeDocumentFolderName)
                || userTypeFolder.getName().equals(config.amendmentDocumentFolderName)) {

            return uploadFileBasedOnUserType(dto, userTypeFolder, fileDTO);
        } else {
            throw new RuntimeException("Wrong userType");
        }

    }


    private String uploadFileBasedOnUserType(FileUploadDTO dto, Folder userTypeFolder, FileDTO fileDTO) {


        Folder disputeIdFolder = checkFolder(userTypeFolder, String.valueOf(dto.getCommonId()));


        if (Objects.isNull(disputeIdFolder)) {
            disputeIdFolder = createFolder(userTypeFolder, String.valueOf(dto.getCommonId()));
        }

        Folder userIdFolder = checkFolder(disputeIdFolder, String.valueOf(dto.getUserId()));

        if (Objects.isNull(userIdFolder)) {
            userIdFolder = createFolder(disputeIdFolder, String.valueOf(dto.getUserId()));
        }

        return uploadDocument(userIdFolder, dto, fileDTO);


    }

    @SneakyThrows
    private String uploadDocument(Folder parentFolder, FileUploadDTO dto, FileDTO fileDTO) {

        String originalFilename = fileDTO.getFileOriginalName();

        Map<String, Object> properties = new HashMap<>();

        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        properties.put(PropertyIds.NAME, originalFilename);


        byte[] realContent = fileDTO.getContent();

        ContentStream contentStream = null;
        if (dto.getAlgorithm().equals("AES")) {
            contentStream = getAESContentStream(fileDTO, realContent);
        } else if (dto.getAlgorithm().equals("RSA")) {
            contentStream = getRSAContentStream(fileDTO, realContent);
        } else if (dto.getAlgorithm().equals("TripleDES")) {
            contentStream = getDESContentStream(fileDTO, realContent);
        } else {
            contentStream = new ContentStreamImpl(fileDTO.getFileOriginalName(),
                    BigInteger.valueOf(realContent.length), fileDTO.getContentType(), new ByteArrayInputStream(realContent));
        }


        Document document = parentFolder.createDocument(properties, contentStream, VersioningState.MAJOR);

        String documentVersion = document.getId().substring(document.getId().indexOf(";") + 1);

        String alfrescoRootPath = config.baseFolder + "/" + dto.getUserType() + "/" + dto.getCommonId() + "/" + dto.getUserId();


        BackUpCreateDTO backUpCreateDTO = new BackUpCreateDTO(document.getId(),
                documentVersion,
                alfrescoRootPath,
                dto.getFileDescription(), dto.getAlgorithm());


        backUpServerFileUpload(backUpCreateDTO, document);

        return document.getId();

    }


    @SneakyThrows
    private void backUpServerFileUpload(BackUpCreateDTO dto, Document file) {

        String backUpURI = "http://localhost:1515/v1/back_up";

        MultiValueMap<String, Object> multipartRequest = new LinkedMultiValueMap<>();

        HttpHeaders requestHeaders = new HttpHeaders();
        requestHeaders.setContentType(MediaType.MULTIPART_FORM_DATA);//Main request's headers

        HttpHeaders requestHeadersAttachment = new HttpHeaders();
        requestHeadersAttachment.setContentType(MediaType.parseMediaType(file.getContentStreamMimeType()));// extract mediatype from file extension

        HttpEntity<ByteArrayResource> attachmentPart;
        ByteArrayResource fileAsResource = new ByteArrayResource(file.getContentStream().getStream().readAllBytes()) {
            @Override
            public String getFilename() {
                return file.getName();
            }
        };
        attachmentPart = new HttpEntity<>(fileAsResource, requestHeadersAttachment);

        multipartRequest.set("file", attachmentPart);

        HttpHeaders requestHeadersJSON = new HttpHeaders();
        requestHeadersJSON.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<BackUpCreateDTO> requestEntityJSON = new HttpEntity<>(dto, requestHeadersJSON);

        multipartRequest.set("dto", requestEntityJSON);

        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(multipartRequest, requestHeaders);//final request

        ResponseEntity<Void> response = restTemplate.exchange(backUpURI, HttpMethod.POST, requestEntity, Void.class);

        System.out.println("response.getStatusCode() = " + response.getStatusCode());
    }


    @SneakyThrows
    private ContentStream getRSAContentStream(FileDTO multipartFile, byte[] realContent) {


        PublicKey aPublic = keyPair.getPublic();
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.ENCRYPT_MODE, aPublic);

        byte[] encryptedContent = cipher.doFinal(realContent);

        return new ContentStreamImpl(multipartFile.getFileOriginalName(),
                BigInteger.valueOf(encryptedContent.length), multipartFile.getContentType(),
                new ByteArrayInputStream(encryptedContent));
    }

    @SneakyThrows
    private ContentStream getDESContentStream(FileDTO multipartFile, byte[] realContent) {


        SecretKeySpec secretKeySpec = new SecretKeySpec(encryptionConfig.tdesSecretKey.getBytes(), "TripleDES");

        Cipher cipher = Cipher.getInstance("TripleDES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

        byte[] encryptedContent = cipher.doFinal(realContent);

        return new ContentStreamImpl(multipartFile.getFileOriginalName(),
                BigInteger.valueOf(encryptedContent.length), multipartFile.getContentType(),
                new ByteArrayInputStream(encryptedContent));
    }

    private ContentStream getAESContentStream(FileDTO fileDTO, byte[] realContent) throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
        Cipher cipher = Cipher.getInstance("AES");
        SecretKey secretKey = new SecretKeySpec(encryptionConfig.aesSecretKey.getBytes(), "AES");

        cipher.init(Cipher.ENCRYPT_MODE, secretKey);

        byte[] encryptedContent = cipher.doFinal(realContent);

        return new ContentStreamImpl(fileDTO.getFileOriginalName(),
                BigInteger.valueOf(encryptedContent.length), fileDTO.getContentType(), new ByteArrayInputStream(encryptedContent));
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

        if (userType.equals(config.disputeDocumentFolderName)
                || userType.equals(config.amendmentDocumentFolderName)) {
            return createFolder(config.getEgpFolder(), userType);
        }

        throw new RuntimeException("Folder not found");
    }

    public void deleteDocument(DeleteDocumentDTO dto) {

        Document documentByName = getDocument(dto);
        encryptionService.deleteDocument(documentByName);
        config.session.delete(documentByName);

    }

    public Document getDocumentByName(Folder parentFolder, String documentName) {

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

    public Document getDocument(DeleteDocumentDTO dto) {
        String folderName = dto.getFolderName();

        Folder userType = findByUserType(folderName);

        Folder commonFolder = checkFolder(userType, String.valueOf(dto.getCommonId()));

        if (Objects.isNull(commonFolder)) throw new RuntimeException("Folder with " + dto.getCommonId() + " not found");

        Folder userIdFolder = checkFolder(commonFolder, String.valueOf(dto.getUserId()));

        if (Objects.isNull(userIdFolder)) throw new RuntimeException("Folder with " + dto.getUserId() + " not found");

        return getDocumentByName(userIdFolder, dto.getDocumentName());
    }

    @SneakyThrows
    public void updateFile(List<RestoreDTO> forUpdate, String documentId) {
        System.out.println("------------Update---------");
        System.out.println("documentId = " + documentId);
        System.out.println("forUpdate = " + forUpdate);
        System.out.println("------------End------------");

        if (documentId.indexOf(";") > 0) {
            documentId = documentId.substring(0, documentId.indexOf(";"));
        }

        Document document = findByDocument(documentId);

        if (Objects.isNull(document)) {
            throw new RuntimeException("Document not found with this " + documentId);
        }

        for (RestoreDTO restoreDTO : forUpdate) {
            Map<String, Object> properties = new HashMap<>();

            System.out.println("------------------------");
            System.out.println("restoreDTO.getFileDTO().getFileOriginalName() = " + restoreDTO.getFileDTO().getFileOriginalName());
            System.out.println("------------------------");

            properties.put(PropertyIds.NAME, restoreDTO.getFileDTO().getFileOriginalName());


            byte[] content = restoreDTO.getFileDTO().getContent();

            ContentStream contentStream = null;
            if (restoreDTO.getAlgorithm().equals("AES")) {
                contentStream = getAESContentStream(restoreDTO.getFileDTO(), content);
            } else if (restoreDTO.getAlgorithm().equals("RSA")) {
                contentStream = getRSAContentStream(restoreDTO.getFileDTO(), content);
            } else if (restoreDTO.getAlgorithm().equals("TripleDES")) {
                contentStream = getDESContentStream(restoreDTO.getFileDTO(), content);
            } else {
                contentStream = new ContentStreamImpl(restoreDTO.getFileDTO().getFileOriginalName(),
                        BigInteger.valueOf(content.length), restoreDTO.getFileDTO().getContentType(), new ByteArrayInputStream(content));
            }


            document.setContentStream(contentStream, true);

            Document document1 = findByDocument(documentId);

            Document updatedDocument = (Document) document1.updateProperties(properties);

            encryptionService.saveEncryption(updatedDocument.getId(), restoreDTO.getAlgorithm());

            String documentVersion = updatedDocument.getId().substring(updatedDocument.getId().indexOf(";") + 1);

            String alfrescoRootPath = config.baseFolder + "/" + restoreDTO.getUserType() + "/" +
                    restoreDTO.getCommonId() + "/" + restoreDTO.getUserId();


            BackUpCreateDTO backUpCreateDTO = new BackUpCreateDTO(
                    updatedDocument.getId(),
                    documentVersion,
                    alfrescoRootPath,
                    restoreDTO.getFileDescription(),
                    restoreDTO.getAlgorithm()
            );


            backUpServerFileUpload(backUpCreateDTO, updatedDocument);

        }

    }

    public Document findByDocument(String documentId) {
        return (Document) config.session.getObject(config.session.createObjectId(documentId));
    }
}
