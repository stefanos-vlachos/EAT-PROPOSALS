package com.example.firebasegsocapp.domain;

import org.joda.time.DateTimeFieldType;
import org.joda.time.Instant;
import java.util.Formatter;

public class FirebaseFile extends FirebaseReference{

    private String referenceSize;
    private String creationTime;
    final static int TYPE = 0;

    public FirebaseFile(){
        super();
    }

    @Override
    public void setReferenceName(String filePath) {
        this.referenceName = filePath.substring(filePath.lastIndexOf("/" )+1, filePath.lastIndexOf("."));
    }

    @Override
    public void setReferenceType(String filePath) {
        this.referenceType = filePath.substring(filePath.lastIndexOf("."));
    }

    public String getReferenceSize() {
        return referenceSize;
    }

    public void setReferenceSize(Long bytes) {
        Formatter fm = new Formatter();
        if (bytes / 1024.0 < 1) {
            this.referenceSize = bytes + "bytes";
            return;
        }
        else if(bytes / (1024.0*1024.0) < 1){
            this.referenceSize = fm.format("%.2f", bytes / 1024.0) + "kB";
            return;
        }
        else if(bytes / (1024.0*1024.0*1024.0) < 1){
            this.referenceSize = fm.format("%.2f", bytes / (1024.0*1024.0)) + "MB";
            return;
        }
        this.referenceSize = fm.format("%.2f", bytes / (1024.0*1024.0*1024.0)) + "GB";
    }

    public String getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(Long milliseconds) {
        Instant instantFromEpochMilli = Instant.ofEpochMilli(milliseconds);
        int year = instantFromEpochMilli.get(DateTimeFieldType.year());
        int month = instantFromEpochMilli.get(DateTimeFieldType.monthOfYear());
        int day = instantFromEpochMilli.get(DateTimeFieldType.dayOfMonth());
        int hour = instantFromEpochMilli.get(DateTimeFieldType.hourOfDay());
        int minute = instantFromEpochMilli.get(DateTimeFieldType.minuteOfHour());

        this.creationTime = day + "/" + month + "/" + year + " " + hour + ":" + minute;
    }

    public int getType() {
        return TYPE;
    }

    @Override
    public String toString() {
        return "FirebaseFile{" +
                "filePath='" + referencePath + '\'' +
                ", fileName='" + referenceName + '\'' +
                ", fileType='" + referenceType + '\'' +
                ", fileSize='" + referenceSize + '\'' +
                ", creationTime='" + creationTime + '\'' +
                '}';
    }
}

