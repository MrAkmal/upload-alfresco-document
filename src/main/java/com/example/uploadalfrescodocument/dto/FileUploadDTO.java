package com.example.uploadalfrescodocument.dto;


import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FileUploadDTO {


    @NotNull
    private MultipartFile file;

    @NotBlank
    private String fileDescription;

    @NotNull
    private Long userId;

    @NotNull
    private Long commonId;//maybe amendment or dispute

    @NotNull
    private String userType;


    private String algorithm;

    public FileUploadDTO(String fileDescription, Long userId, Long commonId, String userType, String algorithm) {
        this.fileDescription = fileDescription;
        this.userId = userId;
        this.commonId = commonId;
        this.userType = userType;
        this.algorithm = algorithm;
    }
}
