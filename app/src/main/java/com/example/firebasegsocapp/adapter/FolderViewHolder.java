package com.example.firebasegsocapp.adapter;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.firebasegsocapp.R;

public class FolderViewHolder extends RecyclerView.ViewHolder{

    public CardView cardView;
    public TextView nameTextView;
    public TextView downloadView;

    public FolderViewHolder(View itemView) {
        super(itemView);

        nameTextView = itemView.findViewById(R.id.txtName);
        cardView = itemView.findViewById(R.id.cardView);
        downloadView = itemView.findViewById(R.id.txtViewDownloadFolder);

    }
}
