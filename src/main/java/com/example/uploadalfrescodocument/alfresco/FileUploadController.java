package com.example.uploadalfrescodocument.alfresco;


import com.example.uploadalfrescodocument.alfresco.dto.FileUploadDTO;
import com.example.uploadalfrescodocument.alfresco.dto.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/file")
public class FileUploadController {


    private final FileUploadService service;

    @Autowired
    public FileUploadController(FileUploadService service) {
        this.service = service;
    }


    @PostMapping("/upload")
    public ResponseEntity<ResponseData> uploadFile(@RequestBody FileUploadDTO dto) {
        return service.uploadFile(dto);
    }

}
