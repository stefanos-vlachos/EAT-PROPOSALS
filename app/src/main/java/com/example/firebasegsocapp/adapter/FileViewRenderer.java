package com.example.firebasegsocapp.adapter;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import com.example.firebasegsocapp.R;
import com.example.firebasegsocapp.domain.FirebaseFile;
import com.example.firebasegsocapp.service.FirebaseFilesService;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class FileViewRenderer extends ViewRenderer<FirebaseFile, FileViewHolder> {

    private final FirebaseFilesService FIREBASE_FILES_SERVICE = new FirebaseFilesService();
    private static HashMap<String, Bitmap> fileExtensionImages;
    private static HashMap<String, String> fileExtensionsMimes;

    public FileViewRenderer(final int type, final Context context) {
        super(type, context);
        fileExtensionImages = new HashMap<String, Bitmap>(FIREBASE_FILES_SERVICE.getFileExtensionsImages());
        fileExtensionsMimes = new HashMap<String, String>(FIREBASE_FILES_SERVICE.getFileExtensionsMimes());
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
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setMessage("Do you want to download this file?")
                        .setCancelable(false)
                        .setPositiveButton("Download", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(firebaseFile.getReferencePath());
                                storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                    String url = uri.toString();
                                    downloadFile(v.getContext(), firebaseFile.getReferenceName(), firebaseFile.getReferenceType(), DIRECTORY_DOWNLOADS, url);
                                }).addOnFailureListener(e -> {
                                    System.out.println("Error");
                                });
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
