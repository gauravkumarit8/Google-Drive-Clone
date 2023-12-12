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
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
public class TrashController {

    @Autowired
    private FileService fileService;
    @Autowired
    private FolderService folderService;
    @Autowired
    private ParentFolderService parentFolderService;
    @Autowired
    private UserService userService;

    @GetMapping("/trash")
    public String openTrash(Model model, Principal principal){

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) principal;

        // Retrieve user attributes from the OAuth2 token
        Map<String, Object> userAttributes = oauthToken.getPrincipal().getAttributes();

        String userEmail = (String) userAttributes.get("email");
        Users user = userService.getUserByEmail(userEmail);


        List<ParentFolder> parentFolders = parentFolderService.getParentFoldersByUserEmail(userEmail);
        ParentFolder parentFolder = parentFolderService.getParentFolderByName("trash",user);

        List<Folder> folders = parentFolder.getFolders();
        List<File> files = parentFolder.getFiles();

        model.addAttribute("parentFolderName","My Drive");
        model.addAttribute("parentFolders",parentFolders);
        model.addAttribute("folders",folders);
        model.addAttribute("files",files);

        return "trash";
    }

    @PostMapping("/moveFileToTrash")
    public String trash(@RequestParam("fileId") Long fileId,
                        @RequestParam(name = "parentFolder", defaultValue = "My Drive") String parentFolderName,
                        Principal principal){

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) principal;

        // Retrieve user attributes from the OAuth2 token
        Map<String, Object> userAttributes = oauthToken.getPrincipal().getAttributes();

        String userEmail = (String) userAttributes.get("email");
        Users user = userService.getUserByEmail(userEmail);


        File file = fileService.getFileById(fileId);
        ParentFolder parentFolder = parentFolderService.getParentFolderByName(parentFolderName,user);
        parentFolder.getFiles().remove((File) file);

        ParentFolder trashFolder = parentFolderService.getParentFolderByName("trash",user);
        trashFolder.getFiles().add((File) file);

        parentFolderService.save(parentFolder);
        parentFolderService.save(trashFolder);

        return "redirect:/"+parentFolderName;
    }

    @PostMapping("/moveFileOutOfTrash")
    public String unTrash(@RequestParam("fileId") Long fileId,
                          @RequestParam(name = "parentFolder",defaultValue = "My Drive") String parentFolderName,
                          Principal principal){

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) principal;

        // Retrieve user attributes from the OAuth2 token
        Map<String, Object> userAttributes = oauthToken.getPrincipal().getAttributes();

        String userEmail = (String) userAttributes.get("email");
        Users user = userService.getUserByEmail(userEmail);


        File file = fileService.getFileById(fileId);
        ParentFolder parentFolder = parentFolderService.getParentFolderByName(parentFolderName,user);
        parentFolder.getFiles().remove((File) file);

        ParentFolder trashFolder = parentFolderService.getParentFolderByName("My Drive",user);
        trashFolder.getFiles().add((File) file);

        parentFolderService.save(parentFolder);
        parentFolderService.save(trashFolder);

        return "redirect:/"+parentFolderName;
    }

    @PostMapping("/moveFolderToTrash")
    public String moveFolderToTrash(@RequestParam("folderId") Long folderId,
                                    @RequestParam(name = "parentFolder",defaultValue = "My Drive") String parentFolderName,
                                    Principal principal){

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) principal;

        // Retrieve user attributes from the OAuth2 token
        Map<String, Object> userAttributes = oauthToken.getPrincipal().getAttributes();

        String userEmail = (String) userAttributes.get("email");
        Users user = userService.getUserByEmail(userEmail);


        Folder folder = folderService.getFolderById(folderId);
        ParentFolder parentFolder = parentFolderService.getParentFolderByName(parentFolderName,user);
        parentFolder.getFolders().remove(folder);

        ParentFolder trashFolder = parentFolderService.getParentFolderByName("trash",user);
        trashFolder.getFolders().add(folder);

        parentFolderService.save(parentFolder);
        parentFolderService.save(trashFolder);

        return "redirect:/"+parentFolderName;
    }

    @PostMapping("/moveFolderOutOfTrash")
    public String moveFolderOutOfTrash(@RequestParam("folderId") Long folderId,
                                       @RequestParam(name = "parentFolder",defaultValue = "My Drive") String parentFolderName,
                                       Principal principal){

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) principal;

        // Retrieve user attributes from the OAuth2 token
        Map<String, Object> userAttributes = oauthToken.getPrincipal().getAttributes();

        String userEmail = (String) userAttributes.get("email");
        Users user = userService.getUserByEmail(userEmail);


        Folder folder = folderService.getFolderById(folderId);
        ParentFolder parentFolder = parentFolderService.getParentFolderByName(parentFolderName,user);
        parentFolder.getFolders().remove(folder);

        ParentFolder trashFolder = parentFolderService.getParentFolderByName("My Drive",user);
        trashFolder.getFolders().add(folder);

        parentFolderService.save(parentFolder);
        parentFolderService.save(trashFolder);

        return "redirect:/"+parentFolderName;
    }


}