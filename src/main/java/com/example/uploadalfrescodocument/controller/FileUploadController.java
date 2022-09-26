package com.example.uploadalfrescodocument.controller;


import com.example.uploadalfrescodocument.alfresco.FileUploadService;
import com.example.uploadalfrescodocument.dto.*;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/v1/file")
@CrossOrigin("*")
public class FileUploadController {


    private final FileUploadService service;

    @Autowired
    public FileUploadController(FileUploadService service) {
        this.service = service;
    }

    @SneakyThrows
    @PostMapping(value = "/upload")
    public ResponseEntity<ResponseData> uploadFile(@ModelAttribute FileUploadDTO dto) {
        FileDTO build = FileDTO.builder()
                .fileOriginalName(dto.getFile().getOriginalFilename())
                .contentType(dto.getFile().getContentType())
                .size(String.valueOf(dto.getFile().getSize()))
                .content(dto.getFile().getBytes())
                .build();

        return service.uploadFile(dto, build);
    }

    @SneakyThrows
    @PutMapping(value = "/update")
    public ResponseEntity<ResponseData> updateFile(@ModelAttribute FileUploadUpdateDTO dto) {
        FileDTO build = FileDTO.builder()
                .fileOriginalName(dto.getFile().getOriginalFilename())
                .contentType(dto.getFile().getContentType())
                .size(String.valueOf(dto.getFile().getSize()))
                .content(dto.getFile().getBytes())
                .build();

        return service.updateFile(dto, build);
    }


    @SneakyThrows
    @PostMapping("/restore")
    public ResponseEntity<Void> saveFile(@RequestBody List<RestoreDTO> restoreDTOS) {

        service.restoreFiles(restoreDTOS);
        return new ResponseEntity<>(HttpStatus.OK);
    }


}
