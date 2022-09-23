package com.example.uploadalfrescodocument.alfresco;


import com.example.uploadalfrescodocument.dto.FileUploadDTO;
import com.example.uploadalfrescodocument.dto.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.example.uploadalfrescodocument.EncryptTest.test;

@RestController
@RequestMapping("/v1/file")
@CrossOrigin("*")
public class FileUploadController {


    private final FileUploadService service;

    @Autowired
    public FileUploadController(FileUploadService service) {
        this.service = service;
    }


    @PostMapping(value = "/upload")
    public ResponseEntity<ResponseData> uploadFile(@ModelAttribute FileUploadDTO dto) {
        System.out.println("dto = " + dto);

        test(dto.getFile());
        return service.uploadFile(dto );
    }







}
