package com.example.firebasegsocapp.service;

import com.example.firebasegsocapp.BuildConfig;
import com.example.firebasegsocapp.retrofit.RetrofitClient;
import okhttp3.ResponseBody;
import org.json.JSONException;
import org.json.JSONObject;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import java.io.IOException;

import static java.net.HttpURLConnection.HTTP_OK;

public class EmailService {

    private static final int SINGLE_FILE_RESOURCE_TYPE = 0;
    private static final int MULTIPLE_FILES_RESOURCE_TYPE = 1;
    private static final int FOLDER_RESOURCE_TYPE = 2;

    private static final String FROM = BuildConfig.MAILGUN_SEND_FROM;
    private static final String TO = BuildConfig.MAILGUN_SEND_TO;
    private static String emailSubject;
    private static String emailBody;
    private static int uploadType;

    private static EmailService mInstance;

    private EmailService() {
    }

    public void initEmail(int resourceType, String uploadedBy){
        switch (resourceType) {
            case SINGLE_FILE_RESOURCE_TYPE:
                uploadType = 0;
                emailSubject = "New file on EAT-PROPOSALS";
                emailBody = "A new file was uploaded by: " + uploadedBy + ". \n\n FILE: \n";
                break;
            case MULTIPLE_FILES_RESOURCE_TYPE:
                uploadType = 1;
                emailSubject = "New files on EAT-PROPOSALS";
                emailBody = "New files were uploaded by: " + uploadedBy + ". \n\n FILES: \n";
                break;
            case FOLDER_RESOURCE_TYPE:
                uploadType = 2;
                emailSubject = "New folder on EAT-PROPOSALS";
                emailBody = "A new folder was uploaded by: " + uploadedBy + ". \n\n CONTENTS: \n";
                break;
        }
    }

    public int getUploadType(){
        return uploadType;
    }

    public void addFileToEmailBody(String fileName, String fileExtension, String filePath){
        String newRecord = "\n\t ->  " + fileName + fileExtension + " (" + filePath.replace("%2F", "/") + " )";
        emailBody += newRecord;
    }

    public void sendEmail(){
        String subject = "New File for EAT-PROPOSALS";
        String body = "A new file was uploaded by ";
        RetrofitClient.getInstance()
                .getApi()
                .sendEmail(FROM, TO, emailSubject, emailBody)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.code() == HTTP_OK) {
                            try {
                                JSONObject obj = new JSONObject(response.body().string());
                            } catch (JSONException | IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                    }
                });
    }

    public static synchronized EmailService getInstance() {
        if (mInstance == null) {
            mInstance = new EmailService();
        }
        return mInstance;
    }

}