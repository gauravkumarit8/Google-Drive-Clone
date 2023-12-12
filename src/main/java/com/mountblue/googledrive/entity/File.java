package com.mountblue.googledrive.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.util.Date;

@Entity
public class File {

    @Id
    @GeneratedValue(strategy =GenerationType.IDENTITY)
    private Long id;
    private String fileName;
    private String fileType;
    private String link;
    private Long size;

    private boolean isStarred;

    @CreationTimestamp
    private Date uploadDate;

    @ManyToOne
    @JoinColumn(name = "file_id")
    private Folder folder;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private Users user;

    public File(Users user) {
        this.user = user;
    }

    public Users getUser() {
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public File(Folder folder) {
        this.folder = folder;
    }

    public Folder getFolder() {
        return folder;
    }

    public void setFolder(Folder folder) {
        this.folder = folder;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public Long getSize() {
        return size;
    }

    public void setSize(Long size) {
        this.size = size;
    }

    public Date getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(Date uploadDate) {
        this.uploadDate = uploadDate;
    }

    public boolean isStarred() {
        return isStarred;
    }

    public void setStarred(boolean starred) {
        isStarred = starred;
    }

    public File() {
    }

    public File(Long id, String fileName, String fileType, String link, Long size, Date uploadDate) {
        this.id = id;
        this.fileName = fileName;
        this.fileType = fileType;
        this.link = link;
        this.size = size;
        this.uploadDate = uploadDate;
    }
}
