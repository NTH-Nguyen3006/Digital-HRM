package com.company.hrm.module.storage.repository;

import com.company.hrm.module.storage.entity.SysStoredFile;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SysStoredFileRepository extends JpaRepository<SysStoredFile, Long> {
    Optional<SysStoredFile> findByFileKeyAndDeletedFalse(String fileKey);
    boolean existsByFileKeyAndDeletedFalse(String fileKey);
}
