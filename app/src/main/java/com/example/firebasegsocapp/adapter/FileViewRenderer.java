package com.example.firebasegsocapp.adapter;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import com.example.firebasegsocapp.R;
import com.example.firebasegsocapp.domain.FirebaseFile;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.HashMap;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class FileViewRenderer extends ViewRenderer<FirebaseFile, FileViewHolder> {

    private static HashMap<String, Integer> fileExtensionImages;
    static {
        fileExtensionImages = new HashMap<>();
        fileExtensionImages.put(".doc", R.drawable.word_icon);
        fileExtensionImages.put(".docx", R.drawable.word_icon);
        fileExtensionImages.put(".pdf", R.drawable.pdf_icon);
        fileExtensionImages.put(".xml", R.drawable.xml_icon);
        fileExtensionImages.put(".xlsx", R.drawable.xls_icon);
        fileExtensionImages.put(".xls", R.drawable.xls_icon);
        fileExtensionImages.put(".txt", R.drawable.txt_icon);
        fileExtensionImages.put(".rtf", R.drawable.document_icon);
        fileExtensionImages.put(".ppt", R.drawable.ppt_icon);
        fileExtensionImages.put(".pptx", R.drawable.ppt_icon);
        fileExtensionImages.put(".odt", R.drawable.document_icon);
        fileExtensionImages.put(".json", R.drawable.json_icon);
        fileExtensionImages.put(".csv", R.drawable.csv_icon);
        fileExtensionImages.put(".html", R.drawable.html_icon);
        fileExtensionImages.put(".jpeg", R.drawable.image_icon);
        fileExtensionImages.put(".jpg", R.drawable.image_icon);
        fileExtensionImages.put(".png", R.drawable.image_icon);
        fileExtensionImages.put(".tex", R.drawable.document_icon);
    }

    private static HashMap<String, String> fileExtensionsMimes;
    static {
        fileExtensionsMimes = new HashMap<>();
        fileExtensionsMimes.put(".doc", "application/msword");
        fileExtensionsMimes.put(".docx", "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        fileExtensionsMimes.put(".pdf", "application/pdf");
        fileExtensionsMimes.put(".xml", "text/xml");
        fileExtensionsMimes.put(".xlsx", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        fileExtensionsMimes.put(".xls", "application/vnd.ms-excel");
        fileExtensionsMimes.put(".txt", "text/plain");
        fileExtensionsMimes.put(".rtf", "text/rtf");
        fileExtensionsMimes.put(".ppt", "application/vnd.ms-powerpoint");
        fileExtensionsMimes.put(".pptx", "application/vnd.openxmlformats-officedocument.presentationml.presentation");
        fileExtensionsMimes.put(".odt", "application/vnd.oasis.opendocument.text");
        fileExtensionsMimes.put(".json", "application/json");
        fileExtensionsMimes.put(".csv", "text/comma-separated-values");
        fileExtensionsMimes.put(".html", "text/html");
        fileExtensionsMimes.put(".jpeg", "image/jpeg");
        fileExtensionsMimes.put(".jpg", "image/jpeg");
        fileExtensionsMimes.put(".png", "image/png");
        fileExtensionsMimes.put(".tex", "text/x-tex");
    }


    public FileViewRenderer(final int type, final Context context) {
        super(type, context);
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
        imageView.setImageResource(fileExtensionImages.get(fileType));
    }
}
