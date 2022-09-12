package com.example.firebasegsocapp.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.firebasegsocapp.adapter.FileViewRenderer;
import com.example.firebasegsocapp.adapter.FilesAdapter;
import com.example.firebasegsocapp.adapter.FolderViewRenderer;
import com.example.firebasegsocapp.domain.FirebaseFile;
import com.example.firebasegsocapp.R;
import com.example.firebasegsocapp.domain.FirebaseFolder;
import com.example.firebasegsocapp.domain.FirebaseReference;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import org.jetbrains.annotations.NotNull;

import java.util.*;

import static com.example.firebasegsocapp.activity.MainActivity.getFirebaseAuth;
import static com.example.firebasegsocapp.activity.MainActivity.getStorageReference;

public class ViewFilesActivity<T extends FirebaseReference> extends AppCompatActivity {

    private final String[] sortItems = new String[]{"File Name", "File Size", "Upload Date", "File Type"};
    private final int[] checkedItem = {-1};

    private static String viewType = "list";
    private final int LOGIN_ACTIVITY_CODE = 1;
    private static int parsedReferences = 0;
    private static int referencesToParse;
    private static ProgressDialog progressDialog;

    private StorageReference storageReference;
    private List<T> firebaseReferences;
    private List<FirebaseFile> firebaseFiles;
    private List<FirebaseFolder> firebaseFolders;
    private RecyclerView rvFiles;
    private FilesAdapter mRecyclerViewAdapter;
    private TextView txtViewLogin;
    private TextView txtViewSortFiles;
    private TextView txtViewChangeViewFiles;
    private RelativeLayout layoutFileOptions;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_files);

        init();
        setListeners();
        updateRegisterDependentElements();
        getFilesFromFirebase();
    }

    private void init(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Loading files...");
        storageReference = getStorageReference();
        firebaseReferences = new ArrayList<>();
        firebaseFiles= new ArrayList<>();
        firebaseFolders = new ArrayList<>();
        mRecyclerViewAdapter = new FilesAdapter(firebaseReferences);
        rvFiles = findViewById(R.id.rvFiles);
        txtViewLogin = findViewById((R.id.txtViewLogin));
        txtViewSortFiles = findViewById(R.id.txtViewSortFiles);
        txtViewChangeViewFiles = findViewById(R.id.txtViewChangeFilesView);
        layoutFileOptions = findViewById(R.id.layoutNoFilesFound);
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

        txtViewChangeViewFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(viewType.equals("list")){
                    txtViewChangeViewFiles.setText("List View");
                    viewType = "grid";
                    configureRecyclerView();
                    return;
                }
                txtViewChangeViewFiles.setText("Grid View");
                viewType = "list";
                configureRecyclerView();
            }
        });
    }

    private void updateRegisterDependentElements(){
        if(getFirebaseAuth().getCurrentUser() != null && getFirebaseAuth().getCurrentUser().isEmailVerified()) {
            txtViewLogin.setText("Logout");
            txtViewLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    getFirebaseAuth().signOut();
                    updateRegisterDependentElements();
                }
            });
            return;
        }

        txtViewLogin.setText("Sign Up | Sign In");
        txtViewLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Use custom form for registering
                Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivityForResult(intent, LOGIN_ACTIVITY_CODE);
            }
        });

    }

    private void getFilesFromFirebase(){
        progressDialog.show();
        storageReference.child("accepted-files").listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                referencesToParse = listResult.getItems().size() + listResult.getPrefixes().size();
                if(referencesToParse==0){
                    txtViewSortFiles.setEnabled(false);
                    txtViewSortFiles.setTextColor(getResources().getColor(R.color.cultured));
                    txtViewChangeViewFiles.setEnabled(false);
                    txtViewChangeViewFiles.setTextColor(getResources().getColor(R.color.cultured));
                    layoutFileOptions.setVisibility(View.VISIBLE);

                    progressDialog.dismiss();
                    return;
                }

                //Parse folders
                for(StorageReference folderReference : listResult.getPrefixes()) {
                    String folderPath = folderReference.getPath();
                    FirebaseFolder firebaseFolder = new FirebaseFolder();
                    firebaseFolder.setReferencePath(folderPath);
                    firebaseFolder.setReferenceName(folderPath);
                    firebaseFolders.add(firebaseFolder);
                    parsedReferences++;
                    if(parsedReferences == referencesToParse) {
                        firebaseReferences.addAll((ArrayList<T>)firebaseFolders);
                        firebaseReferences.addAll((ArrayList<T>)firebaseFiles);
                        registerRenderers();
                        configureRecyclerView();
                    }
                }
                //Parse files
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

        fileReference.getMetadata().addOnSuccessListener(new OnSuccessListener<StorageMetadata>() {
            @Override
            public void onSuccess(StorageMetadata storageMetadata) {
                FirebaseFile firebaseFile = new FirebaseFile();
                firebaseFile.setReferencePath(filePath);
                firebaseFile.setReferenceName(filePath);
                firebaseFile.setReferenceType(filePath);
                firebaseFile.setReferenceSize(storageMetadata.getSizeBytes());
                firebaseFile.setCreationTime(storageMetadata.getCreationTimeMillis());

                firebaseFiles.add(firebaseFile);
                parsedReferences++;
                if(parsedReferences == referencesToParse) {
                    firebaseReferences.addAll((ArrayList<T>)firebaseFolders);
                    firebaseReferences.addAll((ArrayList<T>)firebaseFiles);
                    registerRenderers();
                    configureRecyclerView();
                }
            }
        });
    }

    private void registerRenderers(){
        mRecyclerViewAdapter.registerRenderer(new FileViewRenderer(0, this));
        mRecyclerViewAdapter.registerRenderer(new FolderViewRenderer(1, this));
    }

    private void configureRecyclerView(){

        mRecyclerViewAdapter.setViewType(viewType);

        if(viewType.equals("list"))
            rvFiles.setLayoutManager(new GridLayoutManager(this, 1));
        else
            rvFiles.setLayoutManager(new GridLayoutManager(this, 2));

        rvFiles.setAdapter(mRecyclerViewAdapter);
        progressDialog.dismiss();
    }

    private void sortFiles(int sortSelection){
        switch (sortSelection){
            case 0:
                Collections.sort(firebaseFiles, new Comparator<FirebaseFile>() {
                    @Override
                    public int compare(FirebaseFile file1, FirebaseFile file2) {
                        return file1.getReferenceName().compareTo(file2.getReferenceName());
                    }
                });
                Collections.sort(firebaseFolders, new Comparator<FirebaseFolder>() {
                    @Override
                    public int compare(FirebaseFolder folder1, FirebaseFolder folder2) {
                        return folder1.getReferenceName().compareTo(folder2.getReferenceName());
                    }
                });
                refreshListAdapterState();
                break;
            case 1:
                Collections.sort(firebaseFiles, new Comparator<FirebaseFile>() {
                    @Override
                    public int compare(FirebaseFile file1, FirebaseFile file2) {
                        return file1.getReferenceSize().compareTo(file2.getReferenceSize());
                    }
                });
                refreshListAdapterState();
                break;
            case 2:
                Collections.sort(firebaseFiles, new Comparator<FirebaseFile>(){
                    @Override
                    public int compare(FirebaseFile file1, FirebaseFile file2){
                        return file1.getCreationTime().compareTo(file2.getCreationTime());
                    }
                });
                refreshListAdapterState();
                break;
            case 3:
                Collections.sort(firebaseFiles, new Comparator<FirebaseFile>() {
                    @Override
                    public int compare(FirebaseFile file1, FirebaseFile file2) {
                        return file1.getReferenceType().compareTo(file2.getReferenceType());
                    }
                });
                refreshListAdapterState();
                break;
        }
    }

    private void refreshListAdapterState(){
        firebaseReferences.clear();
        firebaseReferences.addAll((ArrayList<T>)firebaseFolders);
        firebaseReferences.addAll((ArrayList<T>)firebaseFiles);
        mRecyclerViewAdapter.notifyDataSetChanged();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case LOGIN_ACTIVITY_CODE:
                if (resultCode == RESULT_OK)
                    updateRegisterDependentElements();
                break;
        }
    }

    @Override
    public void onBackPressed(){
        super.onBackPressed();
        referencesToParse = 0;
        parsedReferences = 0;
    }
}
