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

import java.security.Principal;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import java.util.Map;
import java.util.stream.Collectors;

import java.util.Set;

@Controller
public class HomeController {

    private FileService fileService;
    private FolderService folderService;
    private ParentFolderService parentFolderService;

    private UserService userService;

    @Autowired
    public HomeController(FileService fileService, FolderService folderService,
                          ParentFolderService parentFolderService, UserService userService) {
        this.fileService = fileService;
        this.folderService = folderService;
        this.parentFolderService = parentFolderService;
        this.userService = userService;
    }

    @GetMapping("/start")
    public String getStarted() {
        return "start";
    }

    @GetMapping("/logout")
    public String logout() {
        return "start";
    }



    @GetMapping({"/", "/My Drive"})
    public String home(Model model, Principal principal) {
        if (principal instanceof OAuth2AuthenticationToken) {
            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) principal;

            // Retrieve user attributes from the OAuth2 token
            Map<String, Object> userAttributes = oauthToken.getPrincipal().getAttributes();

            String userEmail = (String) userAttributes.get("email");
            String userName = (String) userAttributes.get("name");
            String userPicture = (String) userAttributes.get("picture");

            Users user = userService.getUserByEmail(userEmail);

            if (user == null) {
                user = new Users();
                user.setEmail(userEmail);
                userService.saveUser(user);
            }

            // Retrieve user's folders and files based on the authenticated user's email
            List<ParentFolder> parentFolders = parentFolderService.getParentFoldersByUserEmail(userEmail);

            if(parentFolders==null || parentFolders.isEmpty()){

                List<ParentFolder> userParentFolder = new ArrayList<>();

                ParentFolder myDrive = new ParentFolder();
                myDrive.setName("My Drive");
                myDrive.setUser(user);
                parentFolderService.save(myDrive);
                userParentFolder.add(myDrive);

                ParentFolder sharedWithMe = new ParentFolder();
                sharedWithMe.setName("Shared With Me");
                sharedWithMe.setUser(user);
                parentFolderService.save(sharedWithMe);
                userParentFolder.add(sharedWithMe);

                ParentFolder recent = new ParentFolder();
                recent.setName("Recent");
                recent.setUser(user);
                parentFolderService.save(recent);
                userParentFolder.add(recent);

                ParentFolder starred = new ParentFolder();
                starred.setName("Starred");
                starred.setUser(user);
                parentFolderService.save(starred);
                userParentFolder.add(starred);

                ParentFolder trash = new ParentFolder();
                trash.setName("trash");
                trash.setUser(user);
                parentFolderService.save(trash);
                userParentFolder.add(trash);


                user.setParentFolders(userParentFolder);

                userService.saveUser(user);

                parentFolders = parentFolderService.getParentFoldersByUserEmail(userEmail);
                System.out.println(parentFolders);
            }

            ParentFolder starredFolder = parentFolderService.getParentFolderByName("Starred",user);

            List<Folder> folders = new ArrayList<>();
            List<File> files = new ArrayList<>();

            for (ParentFolder parentFolder : parentFolders) {
                if(parentFolder!=null && !parentFolder.getName().equals("trash")) {
                    folders.addAll(parentFolder.getFolders());
                    files.addAll(parentFolder.getFiles());
                }
            }
            // Add starred folders and files

            Iterator<File> iterator = files.iterator();
            while (iterator.hasNext()) {
                File file = iterator.next();
                if (file.getFolder() != null) {
                    iterator.remove();  // Safe removal using Iterator
                }
            }

            Set<String> fileTypes = fileService.getAllFileTypes();

            model.addAttribute("userName",userName);
            model.addAttribute("fileTypes",fileTypes);
            model.addAttribute("parentFolderName", "My Drive");
            model.addAttribute("parentFolders", parentFolders);
            model.addAttribute("folders", folders);
            model.addAttribute("files", files);
            return "home";
        }
        return "error";
    }

}