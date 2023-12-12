package com.mountblue.googledrive.controller;

import com.mountblue.googledrive.entity.File;
import com.mountblue.googledrive.entity.Folder;
import com.mountblue.googledrive.entity.ParentFolder;
import com.mountblue.googledrive.entity.Users;
import com.mountblue.googledrive.service.ParentFolderService;
import com.mountblue.googledrive.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Controller
public class StarredController {
    @Autowired
    private ParentFolderService parentFolderService;
    @Autowired
    private UserService userService;

    @GetMapping("/Starred")
    public String starred(Model model, Principal principal){

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) principal;

        // Retrieve user attributes from the OAuth2 token
        Map<String, Object> userAttributes = oauthToken.getPrincipal().getAttributes();

        String userEmail = (String) userAttributes.get("email");
        Users user = userService.getUserByEmail(userEmail);


        List<ParentFolder> parentFolders = parentFolderService.getParentFoldersByUserEmail(userEmail);
        ParentFolder parentFolder = parentFolderService.getParentFolderByName("Starred",user);

        List<Folder> folders = parentFolder.getFolders();

        List<File> files= parentFolder.getFiles();

        Iterator<File> iterator = files.iterator();
        while (iterator.hasNext()) {
            File file = iterator.next();
            if (!file.isStarred()) {
                iterator.remove();  // Safe removal using Iterator
            }
        }

        Iterator<Folder> iteratorFolder = folders.iterator();
        while (iteratorFolder.hasNext()) {
            Folder folder = iteratorFolder.next();
            if (!folder.isStarred()) {
                iteratorFolder.remove();  // Safe removal using Iterator
            }
        }

        model.addAttribute("parentFolderName","My Drive");
        model.addAttribute("parentFolders",parentFolders);
        model.addAttribute("folders",folders);
        model.addAttribute("files",files);
        return "starred";
    }
}
