package com.mountblue.googledrive.controller;

import com.mountblue.googledrive.entity.File;
import com.mountblue.googledrive.entity.Folder;
import com.mountblue.googledrive.entity.ParentFolder;
import com.mountblue.googledrive.entity.Users;
import com.mountblue.googledrive.service.FileService;
import com.mountblue.googledrive.service.FolderService;
import com.mountblue.googledrive.service.ParentFolderService;
import com.mountblue.googledrive.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Controller
public class RecentController {

    @Autowired
    private ParentFolderService parentFolderService;

    @Autowired
    private FileService fileService;

    @Autowired
    private FolderService folderService;
    @Autowired
    private UserService userService;


    @GetMapping("/Recent")
    public String Recent(Model model, Principal principal){

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) principal;

        // Retrieve user attributes from the OAuth2 token
        Map<String, Object> userAttributes = oauthToken.getPrincipal().getAttributes();

        String userEmail = (String) userAttributes.get("email");
        Users user = userService.getUserByEmail(userEmail);


        List<ParentFolder> parentFolders = parentFolderService.getParentFoldersByUserEmail(userEmail);
        ParentFolder parentFolder = parentFolderService.getParentFolderByName("Recent",user);

        ParentFolder trashFolder = parentFolderService.getParentFolderByName("trash",user);

        List<File> files= fileService.getAllFilesInOrder(user);

        List<Folder> folders= folderService.getAllFoldersInOrder(user);
        System.out.println(folders);

        for(Folder folder:trashFolder.getFolders()){
            if(folders!=null && folders.contains(folder)){
                folders.remove(folder);
            }
        }

        for(File file:trashFolder.getFiles()){
            if(files!=null && files.contains(file)){
                files.remove(file);
            }
        }

        Iterator<File> iterator = files.iterator();
        while (iterator.hasNext()) {
            File file = iterator.next();
            if (file.getFolder()!=null) {
                iterator.remove();  // Safe removal using Iterator
            }
        }

        model.addAttribute("parentFolderName","Recent");
        model.addAttribute("parentFolders",parentFolders);
        model.addAttribute("folders",folders);
        model.addAttribute("files",files);
        return "recent";
    }
}
