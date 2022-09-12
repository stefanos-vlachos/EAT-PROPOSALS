package com.example.firebasegsocapp.adapter;

import android.content.Context;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import com.example.firebasegsocapp.domain.FirebaseReference;

public abstract class ViewRenderer <FR extends FirebaseReference, VH extends RecyclerView.ViewHolder> {

    int type;

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    Context context;

    public ViewRenderer(int type, Context context) {
        this.type = type;
        this.context = context;
    }

    public abstract void bindView(@NonNull FR fileReference, @NonNull VH viewHolder);

    @NonNull
    public abstract	VH createViewHolder(@Nullable ViewGroup parent, String fileViewType);

}
