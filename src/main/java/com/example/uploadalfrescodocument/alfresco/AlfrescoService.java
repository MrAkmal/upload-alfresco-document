package com.example.uploadalfrescodocument.alfresco;


import com.example.uploadalfrescodocument.config.AlfrescoConfig;
import com.example.uploadalfrescodocument.config.EncryptionConfig;
import com.example.uploadalfrescodocument.dto.DeleteDocumentDTO;
import com.example.uploadalfrescodocument.dto.FileUploadDTO;
import lombok.SneakyThrows;
import org.apache.chemistry.opencmis.client.api.CmisObject;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.Folder;
import org.apache.chemistry.opencmis.commons.PropertyIds;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.chemistry.opencmis.commons.enums.VersioningState;
import org.apache.chemistry.opencmis.commons.impl.dataobjects.ContentStreamImpl;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class AlfrescoService {

    private final AlfrescoConfig config;
    private final EncryptionConfig encryptionConfig;


    @Autowired
    public AlfrescoService(AlfrescoConfig config, EncryptionConfig encryptionConfig) {
        this.config = config;
        this.encryptionConfig = encryptionConfig;
    }

    public void uploadFile(FileUploadDTO dto) {


        Folder userTypeFolder = findByUserType(dto.getUserType());

        if (userTypeFolder.getName().equals(config.disputeDocumentFolderName)
                || userTypeFolder.getName().equals(config.amendmentDocumentFolderName)) {
            uploadFileBasedOnUserType(dto, userTypeFolder);
        } else {
            throw new RuntimeException("Wrong userType");
        }

    }


    private void uploadFileBasedOnUserType(FileUploadDTO dto, Folder userTypeFolder) {


        Folder disputeIdFolder = checkFolder(userTypeFolder, String.valueOf(dto.getCommonId()));


        if (Objects.isNull(disputeIdFolder)) {
            disputeIdFolder = createFolder(userTypeFolder, String.valueOf(dto.getCommonId()));
        }

        Folder userIdFolder = checkFolder(disputeIdFolder, String.valueOf(dto.getUserId()));

        if (Objects.isNull(userIdFolder)) {
            userIdFolder = createFolder(disputeIdFolder, String.valueOf(dto.getUserId()));
        }

        uploadDocument(userIdFolder, dto);


    }

    @SneakyThrows
    private void uploadDocument(Folder parentFolder, FileUploadDTO dto) {

        String originalFilename = dto.getFile().getOriginalFilename();

        Map<String, Object> properties = new HashMap<>();

        properties.put(PropertyIds.OBJECT_TYPE_ID, "cmis:document");
        properties.put(PropertyIds.NAME, originalFilename);


        MultipartFile multipartFile = dto.getFile();

        long contentLength = multipartFile.getBytes().length;


        InputStream inputStream = multipartFile.getInputStream();
        byte[] encryptedContent = encryptionConfig.encrypt(inputStream, "AES");



        System.out.println("*****************************************");
        System.out.println("encryptedInputStream = " + encryptedContent);
        System.out.println("*****************************************");

        ContentStream contentStream =
                new ContentStreamImpl(originalFilename, BigInteger.valueOf(encryptedContent.length),
                        multipartFile.getContentType(), new ByteArrayInputStream(encryptedContent));

        String s1 = IOUtils.toString(contentStream.getStream(), StandardCharsets.UTF_8);

        System.out.println("contentStream.getFileName() = " + contentStream.getFileName());
        System.out.println("contentStream.getMimeType() = " + contentStream.getMimeType());
        System.out.println("contentStream.getLength() = " + contentStream.getLength());

        System.out.println("454464*****************************************");

        System.out.println("s1 = " + s1);
        System.out.println("454464*****************************************");


        parentFolder.createDocument(properties, contentStream, VersioningState.MAJOR);

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

        System.out.println("documentByName.getId() = " + documentByName.getId());

        documentByName.getAllVersions().forEach(System.out::println);

        CmisObject object = config.session.getObject(documentByName.getId());
        System.out.println("object.getName() = " + object.getName());

        Document document = null;
        if (version != null) {
            document = (Document) config.session.getObject(documentByName.getId().substring(0, documentByName.getId().indexOf(";")) + ";" + version);
        } else {
            document = (Document) config.session.getObject(documentByName.getId());
        }

        InputStream stream = document.getContentStream().getStream();

        System.out.println("IOUtils.toString(stream,StandardCharsets.UTF_8) = " + IOUtils.toString(stream, StandardCharsets.UTF_8));

        System.out.println("stream.readAllBytes() = " + stream.readAllBytes());
        System.out.println("stream.readAllBytes().length = " + stream.readAllBytes().length);

        byte[] aes = encryptionConfig.decrypt("AES", encryptionConfig.aesSecretKey, stream.readAllBytes());

        System.out.println("IOUtils.toString(aes) = " + IOUtils.toString(aes));

        InputStream inputStream1 = new ByteArrayInputStream(aes);


        String str = IOUtils.toString(inputStream1, StandardCharsets.UTF_8);

        System.out.println("str = " + str);

        document.setContentStream(
                new ContentStreamImpl(
                        document.getName(),
                        BigInteger.valueOf(aes.length),
                        document.getContentStreamMimeType(), inputStream1),
                true);

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
