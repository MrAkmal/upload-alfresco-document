package com.example.uploadalfrescodocument.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
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


}
