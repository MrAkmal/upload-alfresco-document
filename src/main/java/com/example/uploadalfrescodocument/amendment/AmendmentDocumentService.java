package com.example.uploadalfrescodocument.amendment;

import com.example.uploadalfrescodocument.alfresco.AlfrescoService;
import com.example.uploadalfrescodocument.config.AlfrescoConfig;
import com.example.uploadalfrescodocument.dispute.DisputeDocument;
import com.example.uploadalfrescodocument.dto.CommonDTO;
import com.example.uploadalfrescodocument.dto.DeleteDocumentDTO;
import lombok.SneakyThrows;
import org.apache.chemistry.opencmis.client.api.Document;
import org.apache.chemistry.opencmis.client.api.DocumentProperties;
import org.apache.chemistry.opencmis.commons.data.ContentStream;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AmendmentDocumentService {


    private final AmendmentDocumentRepository repository;

    private final AlfrescoService alfrescoService;
    private final AlfrescoConfig alfrescoConfig;


    @Autowired
    public AmendmentDocumentService(AmendmentDocumentRepository repository, AlfrescoService alfrescoService, AlfrescoConfig alfrescoConfig) {
        this.repository = repository;
        this.alfrescoService = alfrescoService;
        this.alfrescoConfig = alfrescoConfig;
    }

    public ResponseEntity<List<CommonDTO>> getAll() {

        List<CommonDTO> commonDTO = repository.getAll();

        commonDTO.forEach(document -> {
            List<String> versions;
            Document documentById = alfrescoService.findByDto(new DeleteDocumentDTO(document.getCommonId(), document.getUploadedBy(), document.getDocumentName(), alfrescoConfig.amendmentDocumentFolderName), null);
            List<Document> allVersions = documentById.getAllVersions();
            versions = allVersions.stream().map(DocumentProperties::getVersionLabel).collect(Collectors.toList());
            document.setVersions(versions);
        });

        return new ResponseEntity<>(commonDTO, HttpStatus.OK);
    }

    public ResponseEntity<Void> delete(Long id) {

        Optional<AmendmentDocument> optionalAmendmentDocument = repository.findById(id);

        if (optionalAmendmentDocument.isEmpty()) throw new RuntimeException("AmendmentDocument not found");
        AmendmentDocument amendmentDocument = optionalAmendmentDocument.get();

        alfrescoService.deleteDocument(
                new DeleteDocumentDTO(amendmentDocument.getAmendmentId(), amendmentDocument.getUploadedBy(), amendmentDocument.getDocumentName(),alfrescoConfig.amendmentDocumentFolderName));

        repository.delete(amendmentDocument);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }


    @SneakyThrows
    public ResponseEntity download(Long id, String version) {

        Optional<AmendmentDocument> optionalAmendmentDocument = repository.findById(id);

        if (optionalAmendmentDocument.isEmpty()) throw new RuntimeException("AmendmentDocument not found");

        AmendmentDocument amendmentDocument = optionalAmendmentDocument.get();

        Document document = alfrescoService.findByDto(new DeleteDocumentDTO(amendmentDocument.getAmendmentId(), amendmentDocument.getUploadedBy(), amendmentDocument.getDocumentName(), alfrescoConfig.amendmentDocumentFolderName), version);

        ContentStream contentStream = document.getContentStream();

        InputStream inputStream = contentStream.getStream();

        byte[] bytes = IOUtils.toByteArray(inputStream);


        HttpHeaders responseHeaders = new HttpHeaders();
        responseHeaders.add("content-disposition", "attachment; filename=" +
                document.getName());
        responseHeaders.add("Content-Type", document.getContentStreamMimeType());
        responseHeaders.add("file-name",document.getName());

        return new ResponseEntity(bytes,responseHeaders,HttpStatus.OK);
    }
}
