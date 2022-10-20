package com.example.firebasegsocapp.adapter;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import com.example.firebasegsocapp.R;
import com.example.firebasegsocapp.activity.ViewFilesActivity;
import com.example.firebasegsocapp.domain.FirebaseFolder;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class FolderViewRenderer extends ViewRenderer<FirebaseFolder, FolderViewHolder>{

    static int filesToDownload ;
    static int downloadedFiles ;
    private ProgressDialog progressDialog;

    public FolderViewRenderer(final int type, final Context context) {
        super(type, context);
        progressDialog = new ProgressDialog(context);
    }

    @NonNull
    @Override
    public FolderViewHolder createViewHolder(@Nullable final ViewGroup parent, String filesViewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View folderView;
        if(filesViewType.equals("grid"))
            folderView = inflater.inflate(R.layout.grid_item_folder, parent, false);
        else
            folderView = inflater.inflate(R.layout.item_folder, parent, false);

        return new FolderViewHolder(folderView);
    }

    @Override
    public void bindView(@NonNull final FirebaseFolder fbFolder, @NonNull final FolderViewHolder folderViewHolder) {
        FirebaseFolder firebaseFolder = fbFolder;

        TextView textView = folderViewHolder.nameTextView;
        CardView cardView = folderViewHolder.cardView;
        TextView downloadView = folderViewHolder.downloadView;

        textView.setText(fbFolder.getReferenceName());
        textView.setSelected(true);

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ViewFilesActivity.class);
                intent.putExtra("folder-name", fbFolder.getReferencePath());
                context.startActivity(intent);
            }
        });

        downloadView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onClick(View v) {
                checkPermissions();
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setMessage("Do you want to download this folder?")
                        .setCancelable(false)
                        .setPositiveButton("Download", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                downloadedFiles = 0;
                                filesToDownload = 0;
                                progressDialog.setMessage("Downloading folder...");
                                progressDialog.show();
                                File downloadsDirectory = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS);
                                File eatProposalsDir = new File(downloadsDirectory+File.separator+"EAT-PROPOSALS");
                                if(!eatProposalsDir.exists())
                                    eatProposalsDir.mkdirs();

                                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(fbFolder.getReferencePath());
                                downloadFiles(storageReference, eatProposalsDir);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.setTitle("Download Folder");
                alert.show();
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.R)
    private void checkPermissions(){
        if(!Environment.isExternalStorageManager()){
            Intent permissionIntent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            context.startActivity(permissionIntent);
        }
    }

    private void downloadFiles(StorageReference storageRef, File destinationDir) {
        storageRef.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
            @Override
            public void onSuccess(ListResult listResult) {
                File newFolderDir = new File(destinationDir+File.separator+storageRef.getName());
                if(!newFolderDir.exists())
                    newFolderDir.mkdirs();

                //Download Files
                filesToDownload+=listResult.getItems().size();
                if(filesToDownload==0){
                    Toast.makeText(context, "Folder was downloaded successfully!", Toast.LENGTH_SHORT).show();
                    progressDialog.dismiss();
                } else {
                    for (StorageReference fileRef : listResult.getItems()) {
                        File localFile = new File(newFolderDir, fileRef.getName());

                        fileRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                downloadedFiles += 1;
                                if (filesToDownload == downloadedFiles) {
                                    Toast.makeText(context, "Folder was downloaded successfully!", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                }
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull @NotNull Exception e) {
                                progressDialog.dismiss();
                                Toast.makeText(context, e.getCause().toString(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                }
                //Handle Folders
                for(StorageReference folderPref : listResult.getPrefixes()){
                    StorageReference nestedFolderRef = storageRef.child(folderPref.getName());
                    downloadFiles(nestedFolderRef, newFolderDir);
                }
            }
        });
    }
}
