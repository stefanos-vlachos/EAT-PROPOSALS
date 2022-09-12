package com.example.firebasegsocapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import com.example.firebasegsocapp.R;
import com.example.firebasegsocapp.activity.ViewFilesActivity;
import com.example.firebasegsocapp.domain.FirebaseFolder;

public class FolderViewRenderer extends ViewRenderer<FirebaseFolder, FolderViewHolder>{

    public FolderViewRenderer(final int type, final Context context) {
        super(type, context);
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

    }
}
