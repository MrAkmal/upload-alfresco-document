package com.example.uploadalfrescodocument.alfresco;


import com.example.uploadalfrescodocument.config.AlfrescoConfig;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Service
public class AlfrescoService {

    private final AlfrescoConfig config;

    @Autowired
    public AlfrescoService(AlfrescoConfig config) {
        this.config = config;
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

        ContentStream contentStream =
                new ContentStreamImpl(originalFilename, BigInteger.valueOf(contentLength), multipartFile.getContentType(), inputStream);


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

    public Document findByDto(DeleteDocumentDTO dto,String version) {

        Document documentByName = getDocument(dto);

        if (version != null)
            return (Document) config.session.getObject(config.session.createObjectId(documentByName.getId().substring(0,documentByName.getId().indexOf(";"))) + ";" + version);
        return (Document) config.session.getObject(config.session.createObjectId(documentByName.getId()));

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
