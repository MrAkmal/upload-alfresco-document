package com.example.uploadalfrescodocument.entity;


import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
@Setter
@NoArgsConstructor
@Builder
@Entity
@Table(name = "dispute_document")
public class DisputeDocument {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String documentName;

    @Column(nullable = false)
    private String documentDescription;

    @Column(nullable = false)
    private String documentSize;

    @Column(nullable = false)
    private LocalDateTime uploadedDate;

    @Column(nullable = false)
    private Long uploadedBy;

    private Long disputeId;


}
