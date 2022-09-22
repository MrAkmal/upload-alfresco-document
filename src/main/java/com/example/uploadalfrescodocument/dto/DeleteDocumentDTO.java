package com.example.uploadalfrescodocument.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeleteDocumentDTO {


    private Long commonId;

    private Long userId;

    private String documentName;

    private String folderName;




}
