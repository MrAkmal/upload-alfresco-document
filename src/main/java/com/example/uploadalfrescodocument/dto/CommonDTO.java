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

}
