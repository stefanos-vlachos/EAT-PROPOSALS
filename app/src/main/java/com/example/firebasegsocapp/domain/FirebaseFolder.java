package com.example.firebasegsocapp.domain;


public class FirebaseFolder extends FirebaseReference{

    final static int TYPE = 1;

    public  FirebaseFolder(){
        super();
        this.referenceType="folder";
    }

    @Override
    public void setReferenceName(String referencePath) {
        this.referenceName = referencePath.substring(referencePath.lastIndexOf("/")+1);
    }

    @Override
    public int getType(){
        return this.TYPE;
    }
}
