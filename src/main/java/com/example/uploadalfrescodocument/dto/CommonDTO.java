package com.example.uploadalfrescodocument.dto;


import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CommonDTO {

    private Long id;

    private String documentName;

    private String documentDescription;

    private String documentSize;

    private LocalDateTime uploadedDate;

    private Long uploadedBy;

    private Long commonId;

    private List<String> versions;

    public CommonDTO(Long id, String documentName, String documentDescription, String documentSize, LocalDateTime uploadedDate, Long uploadedBy, Long commonId) {
        this.id = id;
        this.documentName = documentName;
        this.documentDescription = documentDescription;
        this.documentSize = documentSize;
        this.uploadedDate = uploadedDate;
        this.uploadedBy = uploadedBy;
        this.commonId = commonId;
    }
}
