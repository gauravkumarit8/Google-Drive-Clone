package com.mountblue.googledrive.repository;

import com.mountblue.googledrive.entity.ParentFolder;
import com.mountblue.googledrive.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ParentFolderRepository extends JpaRepository<ParentFolder,Long> {

    @Query("SELECT p FROM ParentFolder p WHERE p.name=:name AND p.user=:user")
    public ParentFolder findByName(@Param("name") String name, @Param("user") Users user);

    List<ParentFolder> findByUserEmail(String userEmail);

}
