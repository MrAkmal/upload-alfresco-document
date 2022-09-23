package com.example.uploadalfrescodocument.repository;


import com.example.uploadalfrescodocument.entity.EncryptedDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface EncryptedDocumentRepository extends JpaRepository<EncryptedDocument, Long> {


    Optional<EncryptedDocument> findByDocumentId(String documentId);


    @Transactional
    @Modifying
    void deleteEncryptedDocumentsByDocumentIdIsIn(List<String> documentId);
}
