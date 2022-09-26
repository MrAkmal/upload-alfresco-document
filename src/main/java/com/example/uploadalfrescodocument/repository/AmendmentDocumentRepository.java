package com.example.uploadalfrescodocument.repository;

import com.example.uploadalfrescodocument.dto.CommonDTO;
import com.example.uploadalfrescodocument.entity.AmendmentDocument;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface AmendmentDocumentRepository extends JpaRepository<AmendmentDocument, Long> {

    @Query("select new com.example.uploadalfrescodocument.dto.CommonDTO(a.id,a.documentName,a.documentDescription,a.documentSize,a.uploadedDate,a.uploadedBy,a.amendmentId) from AmendmentDocument a")
    List<CommonDTO> getAll();



}
