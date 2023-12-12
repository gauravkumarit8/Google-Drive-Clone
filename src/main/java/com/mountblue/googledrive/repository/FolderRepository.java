package com.mountblue.googledrive.repository;

import com.mountblue.googledrive.entity.File;
import com.mountblue.googledrive.entity.Folder;
import com.mountblue.googledrive.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FolderRepository extends JpaRepository<Folder,Long> {

    @Query("SELECT f FROM Folder f WHERE f.user=:user ORDER BY f.createdAt DESC")
    public List<Folder> findFolderByUserInSort(@Param("user") Users user);

    @Query("SELECT f FROM Folder f WHERE LOWER(f.folderName) LIKE LOWER(CONCAT('%', :search, '%'))")
    List<Folder> searchAllFiles(String search);

}
