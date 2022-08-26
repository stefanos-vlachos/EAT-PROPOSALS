package com.example.firebasegsocapp;

import android.app.DownloadManager;
import android.content.Context;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import org.jetbrains.annotations.NotNull;
import java.util.List;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class FilesAdapter extends
        RecyclerView.Adapter<FilesAdapter.ViewHolder>{

    private List<FirebaseFile> firebaseFiles;

    public FilesAdapter(List<FirebaseFile> files) {
        firebaseFiles = files;
    }

    @NonNull
    @NotNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        // Inflate the custom layout
        View fileView = inflater.inflate(R.layout.item_file, parent, false);

        // Return a new holder instance
        ViewHolder viewHolder = new ViewHolder(fileView);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull ViewHolder holder, int position) {
        FirebaseFile firebaseFile = firebaseFiles.get(position);

        // Set item views based on your views and data model
        TextView textView = holder.nameTextView;
        textView.setText(firebaseFile.getFileName());
        Button button = holder.messageButton;
        button.setText("Download file");
        button.setEnabled(true);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                StorageReference storageReference = FirebaseStorage.getInstance().getReference().child(firebaseFile.getFilePath());
                storageReference.getDownloadUrl().addOnSuccessListener(uri -> {
                    String url = uri.toString();
                    downloadFile(v.getContext(), firebaseFile.getFileName(), firebaseFile.getFileType(), DIRECTORY_DOWNLOADS, url);
                }).addOnFailureListener(e -> {
                    System.out.println("Error");
                });

            }
        });
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
        public Button messageButton;

        // We also create a constructor that accepts the entire item row
        // and does the view lookups to find each subview
        public ViewHolder(View itemView) {
            // Stores the itemView in a public final member variable that can be used
            // to access the context from any ViewHolder instance.
            super(itemView);

            nameTextView = (TextView) itemView.findViewById(R.id.contact_name);
            messageButton = (Button) itemView.findViewById(R.id.message_button);
        }
    }

}
