package com.example.easydatalabeler;

import android.net.Uri;

import com.google.firebase.storage.StorageReference;

import java.io.File;

public class Image {
    public String LabeledBy;
    public StorageReference filePath;
    public String downloadUrl;
    public String labelDownloadUrl;
    public boolean isLabeled;
    public File labelsFile;
    public Uri location;
    public String loc;
    public String Time_of_creation;
    public String Time_of_label;
    public String key;

    /*public Image(String downloadUrl, boolean isLabeled, String key, String time_of_creation) {
        this.downloadUrl = downloadUrl;
        this.isLabeled = isLabeled;
        Time_of_creation = time_of_creation;
        this.key = key;
    }*/

    public Image(){

    }
}
