package com.example.uploadalfrescodocument.dispute;

import com.example.uploadalfrescodocument.dto.CommonDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DisputeDocumentRepository extends JpaRepository<DisputeDocument,Long> {

    @Query("select new com.example.uploadalfrescodocument.dto.CommonDTO(a.id,a.documentName,a.documentDescription,a.documentSize,a.uploadedDate,a.uploadedBy,a.disputeId) from DisputeDocument a")
    List<CommonDTO> getAll();



}
