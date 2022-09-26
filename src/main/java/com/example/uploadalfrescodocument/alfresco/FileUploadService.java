package com.example.uploadalfrescodocument.alfresco;

import com.example.uploadalfrescodocument.config.AlfrescoConfig;
import com.example.uploadalfrescodocument.dto.BackUpCreateDTO;
import com.example.uploadalfrescodocument.dto.FileUploadDTO;
import com.example.uploadalfrescodocument.dto.ResponseData;
import com.example.uploadalfrescodocument.entity.AmendmentDocument;
import com.example.uploadalfrescodocument.entity.DisputeDocument;
import com.example.uploadalfrescodocument.entity.EncryptedDocument;
import com.example.uploadalfrescodocument.repository.AmendmentDocumentRepository;
import com.example.uploadalfrescodocument.repository.DisputeDocumentRepository;
import com.example.uploadalfrescodocument.repository.EncryptedDocumentRepository;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
public class FileUploadService {


    private final DisputeDocumentRepository disputeRepository;


    private final AmendmentDocumentRepository amendmentRepository;
    private final AlfrescoService alfrescoService;

    private final AlfrescoConfig alfrescoConfig;

    private final ObjectMapper mapper;


    private final RestTemplate restTemplate;
    private final EncryptedDocumentRepository encryptedDocumentRepository;

    @Autowired
    public FileUploadService(DisputeDocumentRepository disputeRepository, AmendmentDocumentRepository amendmentRepository, AlfrescoService alfrescoService, AlfrescoConfig alfrescoConfig, ObjectMapper mapper, RestTemplate restTemplate, EncryptedDocumentRepository encryptedDocumentRepository) {
        this.disputeRepository = disputeRepository;
        this.amendmentRepository = amendmentRepository;
        this.alfrescoService = alfrescoService;
        this.alfrescoConfig = alfrescoConfig;
        this.mapper = mapper;
        this.restTemplate = restTemplate;
        this.encryptedDocumentRepository = encryptedDocumentRepository;
    }


    public ResponseEntity<ResponseData> uploadFile(FileUploadDTO dto) {

        MultipartFile file = dto.getFile();

        String documentId = alfrescoService.uploadFile(dto);

        String documentVersion = documentId.substring(documentId.indexOf(";") + 1);

        String alfrescoRootPath = alfrescoConfig.baseFolder+"/"+dto.getUserType()+"/"+dto.getCommonId()+"/"+dto.getUserId();


        BackUpCreateDTO backUpCreateDTO = new BackUpCreateDTO(documentId,
                documentVersion,
                dto.getFile(),
                alfrescoRootPath,
                dto.getFileDescription(), dto.getAlgorithm());

        backUpServerFileUpload(backUpCreateDTO);

        encryptedDocumentRepository.save(new EncryptedDocument(documentId, dto.getAlgorithm()));


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

    @SneakyThrows
    private void backUpServerFileUpload(BackUpCreateDTO dto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        LinkedMultiValueMap<String, String> pdfHeaderMap = new LinkedMultiValueMap<>();
        pdfHeaderMap.add("Content-disposition", "form-data; name=filex; filename=" + dto.getFile().getOriginalFilename());
        pdfHeaderMap.add("Content-type", "application/pdf");
        HttpEntity<byte[]> doc = new HttpEntity<byte[]>(dto.getFile().getBytes(), pdfHeaderMap);

        LinkedMultiValueMap<String, Object> multipartReqMap = new LinkedMultiValueMap<>();
        multipartReqMap.add("filex", doc);

        HttpEntity<LinkedMultiValueMap<String, Object>> reqEntity = new HttpEntity<>(multipartReqMap, headers);
        ResponseEntity<Void> resE = restTemplate.exchange("http://localhost:1515/v1/back_up", HttpMethod.POST, reqEntity, Void.class);

//        restTemplate.postForEntity("http://localhost:1515/v1/back_up", entity, Void.class);
    }

}
