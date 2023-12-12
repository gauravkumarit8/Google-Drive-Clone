package com.mountblue.googledrive.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Users {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<File> files = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Folder> folders = new ArrayList<>();

    @OneToMany(mappedBy = "user",cascade = CascadeType.ALL)
    private List<ParentFolder> parentFolders = new ArrayList<>();

    public Users(List<ParentFolder> parentFolders) {
        this.parentFolders = parentFolders;
    }

    public List<ParentFolder> getParentFolders() {
        return parentFolders;
    }

    public void setParentFolders(List<ParentFolder> parentFolders) {
        this.parentFolders = parentFolders;
    }

    public Long getId() {
        return id;
    }

    public Users() {
    }

    public Users(Long id, String email, List<File> files, List<Folder> folders) {
        this.id = id;
        this.email = email;
        this.files = files;
        this.folders = folders;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public List<File> getFiles() {
        return files;
    }

    public void setFiles(List<File> files) {
        this.files = files;
    }

    public List<Folder> getFolders() {
        return folders;
    }

    public void setFolders(List<Folder> folders) {
        this.folders = folders;
    }
}
