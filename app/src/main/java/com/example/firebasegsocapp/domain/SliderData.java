package com.example.firebasegsocapp.domain;

public class SliderData {

    private int resourceId;
    private String caption;

    public SliderData(String caption, int resourceId) {
        this.caption = caption;
        this.resourceId = resourceId;
    }

    public int getResourceId() {
        return resourceId;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
    }

}
