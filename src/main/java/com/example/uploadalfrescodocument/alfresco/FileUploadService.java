package com.example.uploadalfrescodocument.alfresco;

import com.example.uploadalfrescodocument.config.AlfrescoConfig;
import com.example.uploadalfrescodocument.dto.*;
import com.example.uploadalfrescodocument.entity.AmendmentDocument;
import com.example.uploadalfrescodocument.entity.DisputeDocument;
import com.example.uploadalfrescodocument.repository.AmendmentDocumentRepository;
import com.example.uploadalfrescodocument.repository.DisputeDocumentRepository;
import com.example.uploadalfrescodocument.service.EncryptionService;
import org.apache.chemistry.opencmis.client.api.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class FileUploadService {


    private final DisputeDocumentRepository disputeRepository;


    private final AmendmentDocumentRepository amendmentRepository;
    private final AlfrescoService alfrescoService;

    private final AlfrescoConfig alfrescoConfig;

    private final EncryptionService encryptionService;


    @Autowired
    public FileUploadService(DisputeDocumentRepository disputeRepository, AmendmentDocumentRepository amendmentRepository, AlfrescoService alfrescoService, AlfrescoConfig alfrescoConfig, EncryptionService encryptionService) {
        this.disputeRepository = disputeRepository;
        this.amendmentRepository = amendmentRepository;
        this.alfrescoService = alfrescoService;
        this.alfrescoConfig = alfrescoConfig;
        this.encryptionService = encryptionService;
    }


    public ResponseEntity<ResponseData> uploadFile(FileUploadDTO dto, FileDTO fileDTO) {

        String documentId = alfrescoService.uploadFile(dto, fileDTO);

        System.out.println("documentId = " + documentId);

        fileDTO.setDocumentId(documentId);

        return uploadFileToDB(dto, fileDTO);

    }

    private ResponseEntity<ResponseData> uploadFileToDB(FileUploadDTO dto, FileDTO file) {

        encryptionService.saveEncryption(file.getDocumentId(), dto.getAlgorithm());

        if (dto.getUserType().equals(alfrescoConfig.disputeDocumentFolderName)) {
            DisputeDocument disputeDocument = DisputeDocument.builder()
                    .documentName(file.getFileOriginalName())
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
                    .documentName(file.getFileOriginalName())
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


    public void restoreFiles(List<RestoreDTO> restoreDTOS) {

        restoreDTOS = restoreDTOS.stream()
                .sorted(Comparator.comparing(restoreDTO -> restoreDTO.getFileDTO().getFileVersion()))
                .toList();

        for (RestoreDTO restoreDTO : restoreDTOS) {
            System.out.println("restoreDTO = " + restoreDTO);
            System.out.println("restoreDTO.getFileDTO().getFileVersion() = " + restoreDTO.getFileDTO().getFileVersion());
            System.out.println("restoreDTO.getFileDTO().getDocumentId() = " + restoreDTO.getFileDTO().getDocumentId());
            System.out.println("restoreDTO.getFileDTO().getFileOriginalName() = " + restoreDTO.getFileDTO().getFileOriginalName());
        }

        for (RestoreDTO restoreDTO : restoreDTOS) {

            if (restoreDTO.getFileDTO().getFileVersion().equals("1.0")) {

                String newDocumentId = uploadDocument(restoreDTO);

                System.out.println("----------------------------------");
                System.out.println("newDocumentId = " + newDocumentId);
                System.out.println("----------------------------------");
                System.out.println("restoreDTO.getFileDescription() = " + restoreDTO.getFileDescription());
                System.out.println("----------------------------------");



                uploadFileToDB(new FileUploadDTO(
                                restoreDTO.getFileDescription(),
                                restoreDTO.getUserId(),
                                restoreDTO.getCommonId(),
                                restoreDTO.getUserType(),
                                restoreDTO.getAlgorithm()
                        ),
                        restoreDTO.getFileDTO()
                );

                List<RestoreDTO> forUpdate = new ArrayList<>();

                for (RestoreDTO dto : restoreDTOS) {
                    if (!restoreDTO.equals(dto) &&
                            dto.getFileDTO().getDocumentId().equals(restoreDTO.getFileDTO().getDocumentId())) {
                        forUpdate.add(dto);
                    }
                }

                updateDocument(forUpdate, newDocumentId);

                restoreDTOS.removeAll(forUpdate);


            } else {
                System.out.println("---------------------------------");
                System.out.println("restoreDTO.getFileDTO().getDocumentId() = " + restoreDTO.getFileDTO().getDocumentId());
                System.out.println("---------------------------------");
                updateDocument(List.of(restoreDTO), restoreDTO.getFileDTO().getDocumentId());
            }
        }


    }

    private void updateDocument(List<RestoreDTO> forUpdate, String documentId) {
        alfrescoService.updateFile(forUpdate, documentId);
    }

    private String uploadDocument(RestoreDTO restoreDTO) {

        FileUploadDTO dto = FileUploadDTO.builder()
                .userType(restoreDTO.getUserType())
                .fileDescription(restoreDTO.getFileDescription())
                .userId(restoreDTO.getUserId())
                .commonId(restoreDTO.getCommonId())
                .algorithm(restoreDTO.getAlgorithm())
                .build();

        return alfrescoService.uploadFile(dto, restoreDTO.getFileDTO());
    }


    public ResponseEntity<ResponseData> updateFile(FileUploadUpdateDTO dto, FileDTO build) {

        Document document = null;

        if (dto.getUserType().equals(alfrescoConfig.amendmentDocumentFolderName)) {

            Optional<AmendmentDocument> byId = amendmentRepository.findById(dto.getId());

            if (byId.isEmpty()) {
                throw new RuntimeException("File not found with " + dto.getId());
            }

            AmendmentDocument amendmentDocument = byId.get();

            document = alfrescoService.getDocument(new DeleteDocumentDTO(dto.getCommonId(), dto.getUserId(), amendmentDocument.getDocumentName(),
                    alfrescoConfig.amendmentDocumentFolderName));

            amendmentDocument.setDocumentDescription(dto.getFileDescription());
            amendmentDocument.setDocumentName(dto.getFile().getOriginalFilename());
            amendmentDocument.setUploadedDate(LocalDateTime.now());


            amendmentRepository.save(amendmentDocument);

        } else if (dto.getUserType().equals(alfrescoConfig.disputeDocumentFolderName)) {

            Optional<DisputeDocument> disputeOptional = disputeRepository.findById(dto.getId());

            if (disputeOptional.isEmpty()) {
                throw new RuntimeException("File not found with " + dto.getId());
            }

            DisputeDocument disputeDocument = disputeOptional.get();
            document = alfrescoService.getDocument(new DeleteDocumentDTO(dto.getCommonId(), dto.getUserId(),
                    disputeDocument.getDocumentName(),
                    alfrescoConfig.disputeDocumentFolderName));

            disputeDocument.setDocumentDescription(dto.getFileDescription());
            disputeDocument.setDocumentName(dto.getFile().getOriginalFilename());
            disputeDocument.setUploadedDate(LocalDateTime.now());

            disputeRepository.save(disputeDocument);
        }

        if (Objects.isNull(document)) throw new RuntimeException("Document not found with " + dto.getId());

        RestoreDTO restoreDTO = new RestoreDTO(build, dto.getFileDescription(), dto.getUserId(),
                dto.getCommonId(),
                dto.getUserType(),
                dto.getAlgorithm());

        System.out.println("document.getId() = " + document.getId());

        updateDocument(List.of(restoreDTO), document.getId());

        return new ResponseEntity<>(new ResponseData(HttpStatus.OK.value(), "success"), HttpStatus.OK);
    }
}
