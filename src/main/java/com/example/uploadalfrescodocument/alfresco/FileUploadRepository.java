package com.example.uploadalfrescodocument.alfresco;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FileUploadRepository extends JpaRepository<FileUploadEntity,Long> {
}
