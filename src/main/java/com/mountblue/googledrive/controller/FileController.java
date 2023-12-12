package com.mountblue.googledrive.controller;

import com.google.api.core.ApiFuture;
import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteResult;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.google.firebase.cloud.FirestoreClient;
import com.mountblue.googledrive.entity.File;
import com.mountblue.googledrive.entity.Folder;
import com.mountblue.googledrive.entity.ParentFolder;
import com.mountblue.googledrive.entity.Users;
import com.mountblue.googledrive.service.FileService;
import com.mountblue.googledrive.service.FolderService;
import com.mountblue.googledrive.service.ParentFolderService;
import com.mountblue.googledrive.service.UserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.*;

@Controller
public class FileController {

    private FileService fileService;
    @Autowired
    private ParentFolderService parentFolderService;
    @Autowired
    private UserService userService;
    @Autowired
    private FolderService folderService;

    @Autowired
    public FileController(FileService fileService,FolderService folderService) {
        this.fileService = fileService;
        this.folderService=folderService;
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile multipartFile,
                                   @RequestParam(name = "parentFolder",defaultValue = "My Drive") String parentFolderName,
                                   Model model, Principal principal) {
        try {

            OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) principal;
            Map<String, Object> userAttributes = oauthToken.getPrincipal().getAttributes();
            String userEmail = (String) userAttributes.get("email");
            Users user = userService.getUserByEmail(userEmail);

            String fileName = multipartFile.getOriginalFilename();
//            fileName = UUID.randomUUID().toString().concat(fileService.getExtension(fileName));  // to generated random string values for file name.
            File newFile = fileService.uploadFile(multipartFile, fileName);
            newFile.setUser(user);
            ParentFolder parentFolder = parentFolderService.getParentFolderByName(parentFolderName, user);
            System.out.println(parentFolderName + " " + user + " " + userEmail);
            parentFolder.getFiles().add(newFile);
            parentFolderService.save(parentFolder);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return "redirect:/" + parentFolderName;
    }

    @GetMapping("/downloadFile")
    public void downloadFile(@RequestParam("fileId") Long fileId, HttpServletResponse response) throws IOException {
        String fileName = fileService.getFileById(fileId).getFileName();
        String destFileName = UUID.randomUUID().toString();
        String destFilePath = "Z:\\New folder\\" + destFileName;
        // Download file from Firebase Storage
        Credentials credentials = GoogleCredentials.fromStream(new FileInputStream("./serviceAccountKey.json"));
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        Blob blob = storage.get(BlobId.of("drive-db-415a1.appspot.com", fileName));
        blob.downloadTo(Paths.get(destFilePath));
        // Set up HTTP headers for the response
        response.setContentType("application/octet-stream");
        response.setHeader("Content-Disposition", "attachment; filename=" + destFileName);
        // Copy the file content to the response output stream
        Files.copy(Paths.get(destFilePath), response.getOutputStream());
        // Delete the temporary file after sending it to the client
        Files.deleteIfExists(Paths.get(destFilePath));
    }

    @PostMapping("/deleteFile")
    public String deleteFile(@RequestParam("fileId") Long fileId,
                             @RequestParam(name = "parentFolder",defaultValue = "My Drive") String parentFolderName,
                             Principal principal) throws IOException {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) principal;
        // Retrieve user attributes from the OAuth2 token
        Map<String, Object> userAttributes = oauthToken.getPrincipal().getAttributes();
        String userEmail = (String) userAttributes.get("email");
        Users user = userService.getUserByEmail(userEmail);

        File file = fileService.getFileById(fileId);
        ParentFolder parentFolder = parentFolderService.getParentFolderByName(parentFolderName, user);
        parentFolder.getFiles().remove((File) file);


        fileService.deleteFileById(fileId);
        parentFolderService.save(parentFolder);

        return "redirect:/" + parentFolderName;
    }

    @GetMapping("/search")
    public String getSearchResult(@ModelAttribute("search") String search,
                                  Principal principal,
                                  Model model) {
        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) principal;
        Map<String, Object> userAttributes = oauthToken.getPrincipal().getAttributes();
        String userEmail = (String) userAttributes.get("email");
        Users user = userService.getUserByEmail(userEmail);

        Set<File> searchedFiles = fileService.searchFile(search);
        List<File> folderFiles=folderService.getFilesInFolder(search);
        System.out.println(folderFiles);
        searchedFiles.addAll(folderFiles);
        System.out.println(searchedFiles);

        Iterator<File> iterator = searchedFiles.iterator();
        while (iterator.hasNext()) {
            File file = iterator.next();
            if (!file.getUser().equals( user)) {
                iterator.remove();  // Safe removal using Iterator
            }
        }

//        searchedFiles.addAll(folderFiles);
        System.out.println(searchedFiles);
        model.addAttribute("userName",(String) userAttributes.get("name"));
        model.addAttribute("user",search);
        model.addAttribute("files", searchedFiles);

        return "home";
    }

    @PostMapping("/starFile")
    public String starFile(@RequestParam Long fileId,
                           @RequestParam(name = "parentFolder",defaultValue = "My Drive") String parentFolderName,
                           Principal principal,
                           Model model) {

        OAuth2AuthenticationToken oauthToken = (OAuth2AuthenticationToken) principal;
        Map<String, Object> userAttributes = oauthToken.getPrincipal().getAttributes();
        String userEmail = (String) userAttributes.get("email");
        Users user = userService.getUserByEmail(userEmail);

        File file = fileService.getFileById(fileId);
        ParentFolder parentFolder = parentFolderService.getParentFolderByName("Starred", user);
        parentFolder.getFiles().add(file);
        parentFolderService.save(parentFolder);
        if (!file.isStarred()) {
            file.setStarred(true);
        } else {
            file.setStarred(false);
        }
        fileService.save(file);

        return "redirect:/" + parentFolderName;
    }

    @GetMapping("/openFile/{fileId}")
    public void openFile(@PathVariable("fileId") Long fileId, HttpServletResponse response) throws IOException {
        File file = fileService.getFileById(fileId);
        String fileName = file.getFileName();

        // Download file from Firebase Storage
        Credentials credentials = GoogleCredentials.fromStream(new FileInputStream("./serviceAccountKey.json"));
        Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();
        Blob blob = storage.get(BlobId.of("drive-db-415a1.appspot.com", fileName));

        //String contentType = getContentTypeByFileExtension(file.getFileType());
        String contentType = file.getFileType();
        System.out.println(contentType);
        if(contentType.equals("application/octet-stream") || contentType.equals("application/sql") ||
            contentType.equals("text/csv") ||  contentType.equals("text/markdown") || contentType.equals("application/rtf") ||
                contentType.equals("text/html") || contentType.equals("application/xml") ||
                contentType.equals("application/json") || contentType.equals("text/css") || contentType.equals("application/javascript") ||
                contentType.equals("text/x-java-source") || contentType.equals("text/x-python") || contentType.equals("text/x-c") ||
                contentType.equals("text/x-c++") || contentType.equals("text/x-ruby") || contentType.equals("text/php") ||
                contentType.equals("application/x-sh") || contentType.equals("application/bat") ||
                contentType.equals("application/powershell") || contentType.equals("application/x-yaml") ||
                contentType.equals("text/x-java")){

            contentType= "text/plain";

        }else if(contentType.equals("video/3gpp") || contentType.equals("video/x-msvideo") ||
                contentType.equals("video/x-matroska") || contentType.equals("video/x-ms-wmv") || contentType.equals("video/x-flv") ||
                contentType.equals("video/quicktime") || contentType.equals("video/webm") || contentType.equals("video/dvd") ||
                contentType.equals("video/ogg") || contentType.equals("video/mpeg")){

            contentType= "video/mp4";
        }
        response.setContentType(contentType);
        //response.setContentType(file.getFileType());
        response.setHeader("Content-Disposition", "inline; filename=" + file.getFileName());
        // Copy the file content to the response output stream
        blob.downloadTo(response.getOutputStream());
    }

}
