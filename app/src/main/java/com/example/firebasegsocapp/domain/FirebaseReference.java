package com.example.firebasegsocapp.domain;

import java.io.Serializable;

public abstract class FirebaseReference implements Serializable{

    protected String referencePath;
    protected String referenceName;
    protected String referenceType;

    public FirebaseReference(){}

    public String getReferencePath() {
        return referencePath;
    }

    public void setReferencePath(String referencePath) {
        this.referencePath = referencePath;
    }

    public String getReferenceName() {
        return referenceName;
    }

    public void setReferenceName(String referenceName) {
        this.referenceName = referenceName;
    }

    public String getReferenceType() {
        return referenceType;
    }

    public void setReferenceType(String referenceType) {
        this.referenceType = referenceType;
    }

    public abstract int getType();
}
