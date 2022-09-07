package com.example.firebasegsocapp.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.firebasegsocapp.adapter.FilesAdapter;
import com.example.firebasegsocapp.domain.FirebaseFile;
import com.example.firebasegsocapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import static com.example.firebasegsocapp.activity.MainActivity.getStorageReference;

public class ViewFilesActivity extends AppCompatActivity {

    private StorageReference storageReference;
    private ArrayList<FirebaseFile> firebaseFiles;
    private RecyclerView rvFiles;
    private static int filesToParse;
    private static int parsedFiles = 0;
    private static ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_files);

        init();
        getFilesFromFirebase();
    }

    private void init(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading files...");
        storageReference = getStorageReference();
        firebaseFiles= new ArrayList<>();
        rvFiles = findViewById(R.id.rvFiles);
    }

    private void getFilesFromFirebase(){
        progressDialog.show();
        storageReference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                filesToParse = listResult.getItems().size();
                for(StorageReference fileReference : listResult.getItems()) {
                    getFileMetadata(fileReference);
                }
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
            }
        });
    }

    private void getFileMetadata(StorageReference fileReference){
        String filePath = fileReference.getPath();
        int cut = filePath.lastIndexOf(".");
        String fileName = filePath.substring(1,cut);
        String fileType = filePath.substring(cut+1);

        fileReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                String fileSize = String.valueOf(storageMetadata.getSizeBytes() / 1000);

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(storageMetadata.getCreationTimeMillis());

                int mYear = calendar.get(Calendar.YEAR);
                int mMonth = calendar.get(Calendar.MONTH);
                int mDay = calendar.get(Calendar.DAY_OF_MONTH);

                String creationTime = mDay + "/" + mMonth + "/" + mYear;
                firebaseFiles.add(new FirebaseFile(filePath, fileName, fileType, fileSize, creationTime));
                parsedFiles++;
                if(parsedFiles == filesToParse)
                    configureRecyclerView();
            }
        });
    }

    private void configureRecyclerView(){
        rvFiles.setLayoutManager(new GridLayoutManager(this, 1));
        FilesAdapter adapter = new FilesAdapter(firebaseFiles);
        rvFiles.setAdapter(adapter);
        progressDialog.dismiss();
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        filesToParse = 0;
        parsedFiles = 0;
    }
}
