package com.example.uploadalfrescodocument.alfresco;

import com.example.uploadalfrescodocument.alfresco.dto.FileUploadDTO;
import com.example.uploadalfrescodocument.alfresco.dto.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.Objects;

@Service
public class FileUploadService {


    private final FileUploadRepository repository;
    private final AlfrescoService alfrescoService;

    @Autowired
    public FileUploadService(FileUploadRepository repository, AlfrescoService alfrescoService) {
        this.repository = repository;
        this.alfrescoService = alfrescoService;
    }


    public ResponseEntity<ResponseData> uploadFile(FileUploadDTO dto) {


        MultipartFile file = dto.getFile();

        alfrescoService.uploadFile(dto);

        FileUploadEntity fileUploadEntity = FileUploadEntity.builder()
                .documentName(file.getOriginalFilename())
                .documentDescription(dto.getFileDescription())
                .documentSize(String.valueOf(file.getSize()))
                .uploadedDate(LocalDateTime.now())
                .uploadedBy(dto.getUserId())
                .disputeId(Objects.isNull(dto.getDisputeId()) ? 0 : dto.getDisputeId())
                .build();

        repository.save(fileUploadEntity);
        return new ResponseEntity<>(new ResponseData(HttpStatus.OK.value(), "success"), HttpStatus.OK);
    }
}
