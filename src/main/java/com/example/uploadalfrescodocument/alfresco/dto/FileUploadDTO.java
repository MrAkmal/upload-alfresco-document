package com.example.uploadalfrescodocument.alfresco.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadDTO {


    private MultipartFile file;

    private String fileDescription;

    private Long userId;

    private Long disputeId;

    private String userType;

    private String documentType;


}
