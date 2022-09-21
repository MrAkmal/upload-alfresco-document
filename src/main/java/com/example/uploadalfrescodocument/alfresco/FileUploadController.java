package com.example.uploadalfrescodocument.alfresco;


import com.example.uploadalfrescodocument.alfresco.dto.FileUploadDTO;
import com.example.uploadalfrescodocument.alfresco.dto.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
        return service.uploadFile(dto );
    }


    @PostMapping(value = "/test",consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResponseData> test(@RequestBody MultipartFile file,
                                             @RequestParam  String fileDescription,
                                             @RequestParam Long userId,
                                             @RequestParam Long disputeId,
                                             @RequestParam String userType,
                                             @RequestParam String documentType) {
        return service.uploadFile(new FileUploadDTO(file,fileDescription,userId,disputeId,userType,documentType));
    }

}
