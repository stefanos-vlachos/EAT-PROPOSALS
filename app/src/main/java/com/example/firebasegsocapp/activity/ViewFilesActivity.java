package com.example.firebasegsocapp.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
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

    private static int filesToParse;
    private static int parsedFiles = 0;
    private static ProgressDialog progressDialog;

    private StorageReference storageReference;
    private ArrayList<FirebaseFile> firebaseFiles;
    private RecyclerView rvFiles;
    private TextView txtViewSortFiles;

    private final String[] sortItems = new String[]{"File Name", "File Size", "Upload Date", "File Type"};
    private final int[] checkedItem = {-1};

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_files);

        init();
        setListeners();
        getFilesFromFirebase();
    }

    private void init(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading files...");
        storageReference = getStorageReference();
        firebaseFiles= new ArrayList<>();
        rvFiles = findViewById(R.id.rvFiles);
        txtViewSortFiles = findViewById(R.id.txtViewSortFiles);
    }

    private void setListeners(){
        txtViewSortFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(ViewFilesActivity.this);
                builder.setTitle("Sort files by:");
                builder.setIcon(R.mipmap.ic_launcher);
                builder.setSingleChoiceItems(sortItems, checkedItem[0], (dialog, which) -> {
                    checkedItem[0] = which;
                });
                builder.setPositiveButton("Sort", (dialog, which) ->{
                    sortFiles(checkedItem[0]);
                    checkedItem[0]=-1;
                    dialog.dismiss();
                });
                builder.setNegativeButton("Cancel", (dialog,which)->{
                    checkedItem[0]=-1;
                });
                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
        });
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

    private void sortFiles(int sortSelection){
        switch (sortSelection){
            case 1:
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                break;
        }
    }
    @Override
    public void onBackPressed(){
        super.onBackPressed();
        filesToParse = 0;
        parsedFiles = 0;
    }
}
