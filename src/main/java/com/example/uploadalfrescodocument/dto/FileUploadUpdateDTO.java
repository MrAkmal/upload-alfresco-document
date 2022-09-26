package com.example.uploadalfrescodocument.dto;

import lombok.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;


@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class FileUploadUpdateDTO {

    private Long id;

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
