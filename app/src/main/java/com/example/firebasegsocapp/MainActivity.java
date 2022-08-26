package com.example.firebasegsocapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.provider.OpenableColumns;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.gms.tasks.*;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

public class MainActivity extends AppCompatActivity {

    Button uploadButton;
    Button viewButton;
    Uri pdfUri = null;
    StorageReference storageReference;
    ArrayList<FirebaseFile> fileReferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storageReference = FirebaseStorage.getInstance().getReference();
        fileReferences =  new ArrayList<>();

        uploadButton = findViewById(R.id.uploadButton);
        viewButton = findViewById(R.id.viewButton);

        uploadButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("application/pdf");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Document"), 12);
            }
        });

        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storageReference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        for(StorageReference fileReference : listResult.getItems()) {
                            String filePath = fileReference.getPath();
                            int cut = filePath.lastIndexOf(".");
                            String fileName = filePath.substring(1,cut);
                            String fileType = filePath.substring(cut+1);
                            fileReferences.add(new FirebaseFile(filePath, fileName, fileType));
                        }

                        Intent intent = new Intent(getApplicationContext(), ViewFilesActivity.class);
                        intent.putExtra("fileReferences", fileReferences);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(MainActivity.this, "Process failed.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data.getData()!=null) {

            pdfUri = data.getData();
            String fileName = getFileName(pdfUri);
            Toast.makeText(this, fileName, Toast.LENGTH_SHORT).show();

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Uploading");
            progressDialog.show();

            StorageReference reference = storageReference.child(fileName + ".pdf");
            Toast.makeText(MainActivity.this, reference.getName(), Toast.LENGTH_SHORT).show();

            reference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Uploaded Succesfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() )/ taskSnapshot
                                .getTotalByteCount();
                        progressDialog.setMessage("Uploaded " + (int) progress + "%");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

    //method to compose the name of the uploaded files
    @SuppressLint("Range")
    private String getFileName(Uri fileUri) {

        String fileName = null;
        int cut;

        if(pdfUri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(pdfUri, null, null, null, null);
            try {
                if( cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    cut = fileName.lastIndexOf('.');
                    if(cut != -1)
                        fileName = fileName.substring(0, cut);
                    return fileName;
                }
            } finally {
                cursor.close();
            }
        }
        if (fileName == null) {
            fileName = fileUri.getPath();
            cut = fileName.lastIndexOf('/');
            if (cut != -1) {
                fileName = fileName.substring(cut + 1);
            }
        }

        return fileName;
    }

}