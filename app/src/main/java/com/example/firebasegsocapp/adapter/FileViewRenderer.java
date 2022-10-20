package com.example.firebasegsocapp.adapter;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.cardview.widget.CardView;
import com.example.firebasegsocapp.R;
import com.example.firebasegsocapp.domain.FirebaseFile;
import com.example.firebasegsocapp.service.FirebaseFilesService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.ListResult;
import com.google.firebase.storage.StorageReference;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class FileViewRenderer extends ViewRenderer<FirebaseFile, FileViewHolder> {

    private final FirebaseFilesService FIREBASE_FILES_SERVICE = new FirebaseFilesService();
    private static HashMap<String, Bitmap> fileExtensionImages;
    private static HashMap<String, String> fileExtensionsMimes;
    private ProgressDialog progressDialog;

    public FileViewRenderer(final int type, final Context context) {
        super(type, context);
        fileExtensionImages = new HashMap<String, Bitmap>(FIREBASE_FILES_SERVICE.getFileExtensionsImages());
        fileExtensionsMimes = new HashMap<String, String>(FIREBASE_FILES_SERVICE.getFileExtensionsMimes());
        progressDialog = new ProgressDialog(context);
    }

    @Override public
    void bindView(@NonNull final FirebaseFile fbFile, @NonNull final FileViewHolder fileViewHolder) {
        FirebaseFile firebaseFile = fbFile;

        TextView textView = fileViewHolder.nameTextView;
        ImageView imageView = fileViewHolder.imageView;
        TextView downloadView = fileViewHolder.downloadView;
        TextView fileSizeView = fileViewHolder.fileSizeView;
        TextView createdOnView = fileViewHolder.createdOnView;
        CardView cardView = fileViewHolder.cardView;

        setImageForFileExtension(imageView, firebaseFile.getReferenceType());

        textView.setText(firebaseFile.getReferenceName() + firebaseFile.getReferenceType());
        textView.setSelected(true);

        fileSizeView.setText(firebaseFile.getReferenceSize());
        fileSizeView.setSelected(true);

        createdOnView.setText(firebaseFile.getCreationTime());
        createdOnView.setSelected(true);

        downloadView.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onClick(View v) {
                checkPermissions();
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setMessage("Do you want to download this file?")
                        .setCancelable(false)
                        .setPositiveButton("Download", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                progressDialog.setMessage("Downloading file...");
                                progressDialog.show();
                                File downloadsDirectory = Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS);
                                File eatProposalsDir = new File(downloadsDirectory+File.separator+"EAT-PROPOSALS");
                                if(!eatProposalsDir.exists())
                                    eatProposalsDir.mkdirs();
                                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(firebaseFile.getReferencePath());
                                downloadFile(storageReference, eatProposalsDir);
                            }
                        })
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.setTitle("Download File");
                alert.show();
            }
        });

        cardView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(firebaseFile.getReferencePath());
                storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String url = uri.toString();

                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(url), fileExtensionsMimes.get(firebaseFile.getReferenceType()));
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    Intent newIntent = Intent.createChooser(intent, "Open File");
                    try {
                        context.startActivity(newIntent);
                    } catch (ActivityNotFoundException e) {
                    }

                });
            }
        });
    }

    private void downloadFile(StorageReference storageRef, File destinationDir) {
        String[] arrOfStrings = storageRef.getPath().split("/", 3);
        String substring = arrOfStrings[2];
        String newFilePath = substring.substring(0, substring.lastIndexOf("/"));
        String newFileDir = destinationDir + File.separator + newFilePath;

        File newFolderDir = new File(newFileDir);
        if(!newFolderDir.exists())
            newFolderDir.mkdirs();

        File localFile = new File(newFolderDir, storageRef.getName());
        storageRef.getFile(localFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(context, "File was downloaded successfully!", Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(context, e.getCause().toString(), Toast.LENGTH_SHORT).show();
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

    @NonNull
    @Override
    public FileViewHolder createViewHolder(@Nullable final ViewGroup parent, String filesViewType) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View fileView;
        if(filesViewType.equals("grid"))
            fileView = inflater.inflate(R.layout.grid_item_file, parent, false);
        else
            fileView = inflater.inflate(R.layout.item_file, parent, false);

        return new FileViewHolder(fileView);
    }

    private void downloadFile(Context context, String fileName, String fileExtension, String destinationDirectory, String url) {
        DownloadManager downloadmanager = (DownloadManager) context.
                getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, destinationDirectory, fileName);

        downloadmanager.enqueue(request);
    }

    private void setImageForFileExtension(ImageView imageView, String fileType) {
        if(fileExtensionImages.get(fileType)==null) {
            imageView.setImageResource(R.drawable.document_icon);
            return;
        }
        imageView.setImageBitmap(fileExtensionImages.get(fileType));
    }
}
