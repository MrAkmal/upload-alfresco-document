package com.example.uploadalfrescodocument.amendment;

import com.example.uploadalfrescodocument.dto.CommonDTO;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AmendmentDocumentRepository extends JpaRepository<AmendmentDocument, Long> {

    @Query("select new com.example.uploadalfrescodocument.dto.CommonDTO(a.id,a.documentName,a.documentDescription,a.documentSize,a.uploadedDate,a.uploadedBy,a.amendmentId) from AmendmentDocument a")
    List<CommonDTO> getAll();


}
