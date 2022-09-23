package com.example.uploadalfrescodocument.alfresco;

import com.example.uploadalfrescodocument.entity.AmendmentDocument;
import com.example.uploadalfrescodocument.repository.AmendmentDocumentRepository;
import com.example.uploadalfrescodocument.config.AlfrescoConfig;
import com.example.uploadalfrescodocument.entity.EncryptedDocument;
import com.example.uploadalfrescodocument.repository.EncryptedDocumentRepository;
import com.example.uploadalfrescodocument.entity.DisputeDocument;
import com.example.uploadalfrescodocument.repository.DisputeDocumentRepository;
import com.example.uploadalfrescodocument.dto.FileUploadDTO;
import com.example.uploadalfrescodocument.dto.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
public class FileUploadService {


    private final DisputeDocumentRepository disputeRepository;


    private final AmendmentDocumentRepository amendmentRepository;
    private final AlfrescoService alfrescoService;

    private final AlfrescoConfig alfrescoConfig;


    private final EncryptedDocumentRepository encryptedDocumentRepository;

    @Autowired
    public FileUploadService(DisputeDocumentRepository disputeRepository, AmendmentDocumentRepository amendmentRepository, AlfrescoService alfrescoService, AlfrescoConfig alfrescoConfig, EncryptedDocumentRepository encryptedDocumentRepository) {
        this.disputeRepository = disputeRepository;
        this.amendmentRepository = amendmentRepository;
        this.alfrescoService = alfrescoService;
        this.alfrescoConfig = alfrescoConfig;
        this.encryptedDocumentRepository = encryptedDocumentRepository;
    }


    public ResponseEntity<ResponseData> uploadFile(FileUploadDTO dto) {

        MultipartFile file = dto.getFile();

        String documentId = alfrescoService.uploadFile(dto);

        encryptedDocumentRepository.save(new EncryptedDocument(documentId,dto.getAlgorithm()));


        if (dto.getUserType().equals(alfrescoConfig.disputeDocumentFolderName)) {
            DisputeDocument disputeDocument = DisputeDocument.builder()
                    .documentName(file.getOriginalFilename())
                    .documentDescription(dto.getFileDescription())
                    .documentSize(String.valueOf(file.getSize()))
                    .uploadedDate(LocalDateTime.now())
                    .uploadedBy(dto.getUserId())
                    .disputeId(dto.getCommonId())
                    .build();

            disputeRepository.save(disputeDocument);
            return new ResponseEntity<>(new ResponseData(HttpStatus.OK.value(), "success"), HttpStatus.OK);
        } else if (dto.getUserType().equals(alfrescoConfig.amendmentDocumentFolderName)) {
            AmendmentDocument amendmentDocument = AmendmentDocument.builder()
                    .documentName(file.getOriginalFilename())
                    .documentDescription(dto.getFileDescription())
                    .documentSize(String.valueOf(file.getSize()))
                    .uploadedDate(LocalDateTime.now())
                    .uploadedBy(dto.getUserId())
                    .amendmentId(dto.getCommonId())
                    .build();
            amendmentRepository.save(amendmentDocument);
            return new ResponseEntity<>(new ResponseData(HttpStatus.OK.value(), "success"), HttpStatus.OK);
        } else {
            throw new RuntimeException("UserType not valid");
        }

    }

}
