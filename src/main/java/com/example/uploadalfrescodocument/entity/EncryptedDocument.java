package com.example.uploadalfrescodocument.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "encrypted_document")
public class EncryptedDocument {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String documentId;


    private String algorithm;


    public EncryptedDocument(String documentId, String algorithm) {
        this.documentId = documentId;
        this.algorithm = algorithm;
    }
}
