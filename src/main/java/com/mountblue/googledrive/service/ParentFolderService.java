package com.mountblue.googledrive.service;

import com.mountblue.googledrive.entity.ParentFolder;
import com.mountblue.googledrive.entity.Users;
import com.mountblue.googledrive.repository.ParentFolderRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ParentFolderService {

    private ParentFolderRepository parentFolderRepository;

    public ParentFolderService(ParentFolderRepository parentFolderRepository) {
        this.parentFolderRepository = parentFolderRepository;
    }

    public List<ParentFolder> getAllParentFolders(){
        return parentFolderRepository.findAll();
    }

    public ParentFolder getParentFolderByName(String name, Users user){
        return parentFolderRepository.findByName(name,user);
    }

    public void save(ParentFolder parentFolder){
        parentFolderRepository.save(parentFolder);
    }

    public List<ParentFolder> getParentFoldersByUserEmail(String userEmail) {
        return parentFolderRepository.findByUserEmail(userEmail);
    }
}
