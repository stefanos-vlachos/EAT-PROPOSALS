package com.example.firebasegsocapp;

import java.io.Serializable;

public class FirebaseFile implements Serializable {

    private String filePath;
    private String fileName;
    private String fileType;

    public FirebaseFile(String filePath, String fileName, String fileType) {
        this.filePath = filePath;
        this.fileName = fileName;
        this.fileType = fileType;
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
}
