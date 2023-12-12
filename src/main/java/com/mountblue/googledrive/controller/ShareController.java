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
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Controller
public class ShareController {

    @Autowired
    private FileService fileService;
    @Autowired
    private FolderService folderService;
    @Autowired
    private UserService userService;
    @Autowired
    private ParentFolderService parentFolderService;


    @PostMapping("/shareFile")
    public String shareFile(@RequestParam("fileId")String id,@RequestParam("email")String email,Principal principal){
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) principal;
        Map<String, Object> userAttributes = oauthToken.getPrincipal().getAttributes();
        String userEmail = (String) userAttributes.get("email");

        System.out.println(id+" "+email);
        Long fileId = Long.parseLong(id);
        File file = fileService.getFileById(fileId);
        File newFile = new File();
        newFile.setFileName(file.getFileName());
        newFile.setFileType(file.getFileType());
        newFile.setLink(file.getLink());
        newFile.setSize(file.getSize());
        LocalDateTime localDateTime = LocalDateTime.now();

        ZoneId zoneId = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zoneId).toInstant();

        // Convert Instant to Date
        Date date = Date.from(instant);
        newFile.setUploadDate(date);
       // newFile.setUploadDate(new Date());
        Users user = file.getUser();
        if(user!=null && (user.getEmail().equals(email) || (userEmail.equals(email)))){
            return "same-email";
        }
        if(userService.getUserByEmail(email)==null){
            return "invalid-user";
        }
        if(user==null){

            Users newUser = userService.getUserByEmail(userEmail);
            file.setUser(newUser);
            fileService.save(file);
        }
        Users fileSharedUser = userService.getUserByEmail(email);
        ParentFolder sharedParentFolder = parentFolderService.getParentFolderByName("Shared With Me",fileSharedUser);
        if(sharedParentFolder.getFiles()==null){
            List<File> sharedFiles = new ArrayList<>();
            sharedFiles.add(newFile);
            sharedParentFolder.setFiles(sharedFiles);
            parentFolderService.save(sharedParentFolder);
            userService.saveUser(fileSharedUser);
        }
        else{
            sharedParentFolder.getFiles().add(newFile);
            parentFolderService.save(sharedParentFolder);
            userService.saveUser(fileSharedUser);
        }
        newFile.setUser(userService.getUserByEmail(email));
        fileService.save(newFile);
        return "redirect:/";
    }

    @GetMapping("Shared With Me")
    public String sharedWithMe(Principal principal, Model model){
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) principal;

        // Retrieve user attributes from the OAuth2 token
        Map<String, Object> userAttributes = oauthToken.getPrincipal().getAttributes();

        String userEmail = (String) userAttributes.get("email");

        Users user = userService.getUserByEmail(userEmail);

        ParentFolder sharedParentFolder = parentFolderService.getParentFolderByName("Shared With Me",user);
        List<File> files = sharedParentFolder.getFiles();
        List<Folder> folders = sharedParentFolder.getFolders();
        model.addAttribute("files",files);
        model.addAttribute("folders",folders);
        return "files";
    }


    @PostMapping("/shareFolder")
    public String shareFolder(@RequestParam("folderId")String id,@RequestParam("email")String email,Principal principal){
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) principal;
        Map<String, Object> userAttributes = oauthToken.getPrincipal().getAttributes();
        String userEmail = (String) userAttributes.get("email");

        System.out.println(id);
        Long folderId = Long.parseLong(id);
        Folder folder = folderService.getFolderById(folderId);
        Folder newFolder = new Folder();
        newFolder.setFolderName(folder.getFolderName());
        newFolder.setCreatedAt(folder.getCreatedAt());
        Users user = folder.getUser();

        if(user!=null && (user.getEmail().equals(email) || (userEmail.equals(email)))){
            return "same-email";
        }
        if(userService.getUserByEmail(email)==null){
            return "invalid-user";
        }
        if(user==null){

            Users newUser = userService.getUserByEmail(userEmail);
            folder.setUser(newUser);
            folderService.save(folder);
        }

        Users folderSharedUser = userService.getUserByEmail(email);
        ParentFolder sharedParentFolder = parentFolderService.getParentFolderByName("Shared With Me",folderSharedUser);

        List<File> newFiles = new ArrayList<>();
        for(File file:folder.getFiles()){
            File newFile = new File();
            newFile.setFileName(file.getFileName());
            newFile.setFileType(file.getFileType());
            newFile.setLink(file.getLink());
            newFile.setSize(file.getSize());
            newFile.setUploadDate(file.getUploadDate());
            newFile.setUser(folderSharedUser);
            newFile.setFolder(newFolder);
            newFiles.add(newFile);
        }
        newFolder.setFiles(newFiles);
        if(sharedParentFolder.getFolders()==null){
            List<Folder> sharedFolders = new ArrayList<>();
            sharedFolders.add(newFolder);
            sharedParentFolder.setFolders(sharedFolders);
            parentFolderService.save(sharedParentFolder);
            userService.saveUser(folderSharedUser);
        }
        else{
            sharedParentFolder.getFolders().add(newFolder);
            parentFolderService.save(sharedParentFolder);
            userService.saveUser(folderSharedUser);
        }
        return "redirect:/";
    }


}
