package com.mountblue.googledrive.repository;

import com.mountblue.googledrive.entity.File;
import com.mountblue.googledrive.entity.Folder;
import com.mountblue.googledrive.entity.Users;
import jakarta.persistence.TemporalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Temporal;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Repository
public interface FileRepository extends JpaRepository<File,Long> {

    List<File> findByFolderId(Long folderId);
    @Query("SELECT f FROM File f WHERE LOWER(f.fileName) LIKE LOWER(CONCAT('%', :search, '%'))")
    Set<File> findAllByFileNameContainingIgnoreCase(String search);


    List<File> findByUserEmail(String userEmail);

    @Query("SELECT f FROM File f " +
            "WHERE (:minSize IS NULL OR f.size >= :minSize) " +
            "AND (:maxSize IS NULL OR f.size <= :maxSize) " +
            "AND (:fileName IS NULL OR LOWER(f.fileName) LIKE LOWER(CONCAT('%', :fileName, '%'))) " +
            "AND (:fileType IS NULL OR f.fileType = :fileType)")
    Set<File> findFilteredFiles(
            @Param("minSize") Long minSize,
            @Param("maxSize") Long maxSize,
            @Param("fileName") String fileName,
            @Param("fileType") String fileType
    );

    @Query("SELECT f.fileType from File f")
    public Set<String> findAllFileTypes();

    @Query("SELECT f FROM File f WHERE f.user=:user ORDER BY f.uploadDate desc")
    public List<File> findFilesByUserInSort(@Param("user") Users user);

    @Query("SELECT f FROM File f where f.folder=:newFolder")
    public List<File> findFilesByFolderId(@Param("newFolder") Folder folder);
}
