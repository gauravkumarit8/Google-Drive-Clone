package com.mountblue.googledrive.service;

import com.google.auth.Credentials;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobId;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import com.mountblue.googledrive.entity.File;
import com.mountblue.googledrive.entity.Folder;
import com.mountblue.googledrive.entity.Users;
import com.mountblue.googledrive.repository.FileRepository;
import com.mountblue.googledrive.repository.FolderRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class FolderService {

    private FolderRepository folderRepository;

    private FileRepository fileRepository;

    @Autowired
    public FolderService(FolderRepository folderRepository,FileRepository fileRepository){
        this.folderRepository=folderRepository;
        this.fileRepository=fileRepository;
    }

    public void save(Folder folder){
        folderRepository.save(folder);
    }

    public List<Folder> getAllFolders() {
        return folderRepository.findAll();
    }

    public Folder createFolder(String folderName, List<MultipartFile> files,Users user) throws IOException {
        Folder folder = new Folder();
        folder.setFolderName(folderName);

        // Save the folder first
        folderRepository.save(folder);

        for (MultipartFile file : files) {
            String fileName = file.getOriginalFilename();

            java.io.File tempFile = java.io.File.createTempFile("temp", null);
            file.transferTo(tempFile.toPath());


            // Upload the file to Firebase Storage
            try (FileInputStream serviceAccount = new FileInputStream("./serviceAccountKey.json")) {
                Credentials credentials = GoogleCredentials.fromStream(serviceAccount);
                Storage storage = StorageOptions.newBuilder().setCredentials(credentials).build().getService();

                BlobId blobId = BlobId.of("drive-db-415a1.appspot.com", fileName);
                BlobInfo blobInfo = BlobInfo.newBuilder(blobId).setContentType("media").build();

                storage.create(blobInfo, Files.readAllBytes(tempFile.toPath()));
            }
            String DOWNLOAD_URL = "https://firebasestorage.googleapis.com/v0/b/drive-db-415a1/o/%s?alt=media";

            // Create a record in the database for the uploaded file
            File savefile = new File();
            System.out.println(fileName);
            savefile.setFileName(fileName);
            savefile.setFileType(file.getContentType());
            savefile.setLink(String.format(DOWNLOAD_URL, URLEncoder.encode(fileName, StandardCharsets.UTF_8)));  // You can set a proper link or leave it empty
            savefile.setSize(file.getSize());
            savefile.setFolder(folder);
            savefile.setUser(user);
            fileRepository.save(savefile);
            folder.getFiles().add(savefile);

        }


        folder = folderRepository.save(folder);

        return folder;
    }

    public List<File> getFilesInFolder(String search) {
        List<File> searchFile = new ArrayList<>();
        List<Folder> allFolder=folderRepository.findAll();
       // System.out.println(allFolder.get(0).getFiles().get(0).getFileName());
        for(Folder folder:allFolder){
            for(File file:folder.getFiles()){
                //System.out.println(file.getFileName());
                if(file.getFileName().indexOf(search)!=-1){
                    searchFile.add(file);
                }
            }
        }
        return  searchFile;
    }

    public String getFolderNameFromFilename(String filename) {
        System.out.println(filename);
        String foldername = filename.split("/")[0];
        System.out.println(foldername);
        return foldername;
    }

    public void deleteFolderById(Long folderId){
        folderRepository.deleteById(folderId);
    }

    public Folder getFolderById(Long folderId){
        return folderRepository.findById(folderId).get();
    }

    public List<Folder> getAllFoldersInOrder(Users user){
        return folderRepository.findFolderByUserInSort(user);
    }

}
