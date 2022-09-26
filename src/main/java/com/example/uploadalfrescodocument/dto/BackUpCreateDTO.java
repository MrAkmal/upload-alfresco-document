package com.example.uploadalfrescodocument.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BackUpCreateDTO {


    private String documentId;

    private String documentVersion;

    private String alfrescoRootPath;


    private String documentDescription;

    private String algorithm;


}
