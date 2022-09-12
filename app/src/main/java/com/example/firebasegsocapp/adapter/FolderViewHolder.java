package com.example.firebasegsocapp.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.firebasegsocapp.R;

public class FolderViewHolder extends RecyclerView.ViewHolder{

    public TextView nameTextView;

    public ImageView imageView;

    public FolderViewHolder(View itemView) {
        super(itemView);

        nameTextView = itemView.findViewById(R.id.txtName);
        imageView = itemView.findViewById(R.id.imageView);
    }
}
