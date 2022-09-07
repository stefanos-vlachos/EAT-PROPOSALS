package com.example.firebasegsocapp.domain;

import java.io.Serializable;

public class FirebaseFile implements Serializable {

    private String filePath;
    private String fileName;
    private String fileType;
    private String fileSize;
    private String creationTime;

    public FirebaseFile(String filePath, String fileName, String fileType, String fileSize, String creationTime) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileType = fileType;
        this.fileSize = fileSize;
        this.creationTime = creationTime;
    }
    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String fileReference) {
        this.filePath = fileReference;
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

    public String getFileSize() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize = fileSize;
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(String uploadedBy) {
        this.creationTime = uploadedBy;
    }
}
