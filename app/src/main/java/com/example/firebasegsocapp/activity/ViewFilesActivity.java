package com.example.firebasegsocapp.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
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

import java.util.*;
import java.util.stream.Collectors;

import static com.example.firebasegsocapp.activity.MainActivity.getStorageReference;

public class ViewFilesActivity extends AppCompatActivity {

    private static int filesToParse;
    private static int parsedFiles = 0;
    private static ProgressDialog progressDialog;

    private StorageReference storageReference;
    private List<FirebaseFile> firebaseFiles;
    private RecyclerView rvFiles;
    private FilesAdapter adapter;
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
                    sortFiles(which);
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
        storageReference.child("accepted-files").listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                filesToParse = listResult.getItems().size();
                if(filesToParse==0){
                    txtViewSortFiles.setVisibility(View.INVISIBLE);
                    progressDialog.dismiss();
                    return;
                }
                txtViewSortFiles.setVisibility(View.VISIBLE);
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
        adapter = new FilesAdapter(firebaseFiles);
        rvFiles.setAdapter(adapter);
        progressDialog.dismiss();
    }

    private void sortFiles(int sortSelection){

        switch (sortSelection){
            case 0:
                Toast.makeText(this, "Sorting Name", Toast.LENGTH_SHORT).show();
                Collections.sort(firebaseFiles, new Comparator<FirebaseFile>() {
                    @Override
                    public int compare(FirebaseFile file1, FirebaseFile file2) {
                        return file1.getFileName().compareTo(file2.getFileName());
                    }
                });
                adapter.notifyDataSetChanged();
                break;
            case 1:
                Toast.makeText(this, "Sorting Size", Toast.LENGTH_SHORT).show();
                Collections.sort(firebaseFiles, new Comparator<FirebaseFile>() {
                    @Override
                    public int compare(FirebaseFile file1, FirebaseFile file2) {
                        return Integer.compare(Integer.parseInt(file1.getFileSize()), Integer.parseInt(file2.getFileSize()));
                    }
                });
                adapter.notifyDataSetChanged();
                break;
            case 2:
                Collections.sort(firebaseFiles, new Comparator<FirebaseFile>(){
                    @Override
                    public int compare(FirebaseFile file1, FirebaseFile file2){
                        return file1.getCreationTime().compareTo(file2.getCreationTime());
                    }
                });
                adapter.notifyDataSetChanged();
                break;
            case 3:
                Toast.makeText(this, "Sorting Type", Toast.LENGTH_SHORT).show();
                Collections.sort(firebaseFiles, new Comparator<FirebaseFile>() {
                    @Override
                    public int compare(FirebaseFile file1, FirebaseFile file2) {
                        return file1.getFileType().compareTo(file2.getFileType());
                    }
                });
                adapter.notifyDataSetChanged();
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
