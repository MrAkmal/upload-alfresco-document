package com.example.uploadalfrescodocument.service;

import com.example.uploadalfrescodocument.alfresco.AlfrescoService;
import com.example.uploadalfrescodocument.config.AlfrescoConfig;
import com.example.uploadalfrescodocument.dto.CommonDTO;
import com.example.uploadalfrescodocument.dto.DeleteDocumentDTO;
import com.example.uploadalfrescodocument.entity.DisputeDocument;
import com.example.uploadalfrescodocument.repository.DisputeDocumentRepository;
import lombok.SneakyThrows;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.DocumentProperties;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DisputeDocumentService {


    private final DisputeDocumentRepository repository;

    private final AlfrescoService alfrescoService;
    private final AlfrescoConfig alfrescoConfig;

    private final EncryptionService encryptionService;

    public DisputeDocumentService(DisputeDocumentRepository repository,
                                  AlfrescoService alfrescoService, AlfrescoConfig alfrescoConfig, EncryptionService encryptionService) {
        this.repository = repository;
        this.alfrescoService = alfrescoService;
        this.alfrescoConfig = alfrescoConfig;
        this.encryptionService = encryptionService;
    }


    public ResponseEntity<List<CommonDTO>> getAll() {

        List<CommonDTO> commonDTO = repository.getAll();


        commonDTO.forEach(document -> {
            List<String> versions;
            Document documentById = alfrescoService.findByDto(new DeleteDocumentDTO(document.getCommonId(), document.getUploadedBy(), document.getDocumentName(), alfrescoConfig.disputeDocumentFolderName), null);
            List<Document> allVersions = documentById.getAllVersions();
            versions = allVersions.stream().map(DocumentProperties::getVersionLabel).collect(Collectors.toList());
            document.setVersions(versions);
        });

        return new ResponseEntity<>(commonDTO, HttpStatus.OK);
    }

    public ResponseEntity<Void> delete(Long id) {

        Optional<DisputeDocument> optionalDisputeDocument = repository.findById(id);

        if (optionalDisputeDocument.isEmpty()) throw new RuntimeException("DisputeDocument not found");
        DisputeDocument disputeDocument = optionalDisputeDocument.get();

        alfrescoService.deleteDocument(
                new DeleteDocumentDTO(disputeDocument.getDisputeId(), disputeDocument.getUploadedBy(), disputeDocument.getDocumentName(), alfrescoConfig.disputeDocumentFolderName));

        repository.delete(disputeDocument);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @SneakyThrows
    public ResponseEntity download(Long id, String version) {


        Optional<DisputeDocument> optionalDisputeDocument = repository.findById(id);

        if (optionalDisputeDocument.isEmpty()) throw new RuntimeException("DisputeDocument not found");

        DisputeDocument disputeDocument = optionalDisputeDocument.get();

        Document document = alfrescoService.findByDto(new DeleteDocumentDTO(disputeDocument.getDisputeId(), disputeDocument.getUploadedBy(), disputeDocument.getDocumentName(), alfrescoConfig.disputeDocumentFolderName), version);

        String encryptionAlgorithm = encryptionService.getAlgorithmByDocumentId(document.getId());

        byte[] decryptedContent = encryptionService.getDecryptedContentBytes(document, encryptionAlgorithm);


        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("content-disposition", "attachment; filename=" +
                document.getName());
        responseHeaders.add("Content-Type", document.getContentStreamMimeType());
        responseHeaders.add("file-name", document.getName());

        return new ResponseEntity(decryptedContent, responseHeaders, HttpStatus.OK);
    }


}
