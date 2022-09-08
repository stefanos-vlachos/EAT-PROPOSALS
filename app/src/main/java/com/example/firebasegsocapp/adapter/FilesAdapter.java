package com.example.firebasegsocapp.adapter;

import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.Context;

import android.content.DialogInterface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.firebasegsocapp.domain.FirebaseFile;
import com.example.firebasegsocapp.R;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class FilesAdapter extends
        RecyclerView.Adapter<FilesAdapter.ViewHolder>{

    private List<FirebaseFile> firebaseFiles;
    private static HashMap<String, Integer> fileExtensionImages;
    private String filesViewType;
    static {
        fileExtensionImages = new HashMap<>();
        fileExtensionImages.put(".doc", R.drawable.word_icon);
        fileExtensionImages.put(".docx", R.drawable.word_icon);
        fileExtensionImages.put(".pdf", R.drawable.pdf_icon);
        fileExtensionImages.put(".xml", R.drawable.xml_icon);
        fileExtensionImages.put(".xlsx", R.drawable.xml_icon);
        fileExtensionImages.put(".xls", R.drawable.xml_icon);
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

    public FilesAdapter(List<FirebaseFile> files, String viewType) {
        firebaseFiles = files;
        filesViewType = viewType;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View fileView;
        if(filesViewType.equals("grid"))
            fileView = inflater.inflate(R.layout.grid_item_file, parent, false);
        else
            fileView = inflater.inflate(R.layout.item_file, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(fileView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {

        FirebaseFile firebaseFile = firebaseFiles.get(position);

        TextView textView = holder.nameTextView;
        ImageView imageView = holder.imageView;
        TextView downloadView = holder.downloadView;
        TextView fileSizeView = holder.fileSizeView;
        TextView createdOnView = holder.createdOnView;

        setImageForFileExtension(imageView, firebaseFile.getFileType());

        textView.setText(firebaseFile.getFileName() + firebaseFile.getFileType());
        textView.setSelected(true);

        fileSizeView.setText(firebaseFile.getFileSize());
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
                                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(firebaseFile.getFilePath());
                                storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                                    String url = uri.toString();
                                    downloadFile(v.getContext(), firebaseFile.getFileName(), firebaseFile.getFileType(), DIRECTORY_DOWNLOADS, url);
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

    }

    private void setImageForFileExtension(ImageView imageView, String fileType) {
        imageView.setImageResource(fileExtensionImages.get(fileType));
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

    @Override
    public int getItemCount() {
        return firebaseFiles.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        // Your holder should contain a member variable
        // for any view that will be set as you render a row
        public TextView nameTextView;
        public ImageView imageView;
        public TextView downloadView;
        public TextView fileSizeView;
        public TextView createdOnView;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            nameTextView = itemView.findViewById(R.id.txtName);
            imageView = itemView.findViewById(R.id.imageView);
            downloadView = itemView.findViewById(R.id.txtViewDownloadFile);
            fileSizeView = itemView.findViewById(R.id.txtViewFileSizeValue);
            createdOnView = itemView.findViewById(R.id.txtViewCreationTimeValue);
        }
    }

}
