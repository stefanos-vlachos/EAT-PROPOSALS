package com.example.firebasegsocapp.adapter;

import android.util.SparseArray;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.firebasegsocapp.domain.FirebaseReference;
import org.jetbrains.annotations.NotNull;
import java.util.List;

public class FilesAdapter<T extends FirebaseReference> extends RecyclerView.Adapter{

    @NonNull
    private final SparseArray<ViewRenderer> viewRenderers = new SparseArray<>();
    private List<T> firebaseReferences;
    private String filesViewType;

    public FilesAdapter(List<T> files) {
        firebaseReferences = files;
    }

    public void setViewType (String viewType) {
        this.filesViewType = viewType;
    }

    @NonNull
    @NotNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int type) {
        final ViewRenderer renderer = viewRenderers.get(type);
        if (renderer != null) {
            return renderer.createViewHolder(parent, filesViewType);
        }
        throw new RuntimeException("Not supported Item View Type: " + type);
    }

    @Override
    public void onBindViewHolder(@NonNull @NotNull RecyclerView.ViewHolder holder, int position) {
        final FirebaseReference item = getItem(position);
        final ViewRenderer renderer = viewRenderers.get(item.getType());
        if (renderer != null) {
            renderer.bindView(item, holder);
        } else {
            throw new RuntimeException("Not supported View Holder: " + holder);
        }
    }

    public void registerRenderer(@NonNull final ViewRenderer renderer) {
        final int type = renderer.getType();
        if (viewRenderers.get(type) == null) {
            viewRenderers.put(type, renderer);
        } else {
            throw new RuntimeException("ViewRenderer already exist with this type: " + type);
        }
    }

    @Override
    public int getItemViewType(final int position) {
        final FirebaseReference item = getItem(position);
        return item.getType();
    }

    private	FirebaseReference getItem(final int position) {
        return firebaseReferences.get(position);
    }

    @Override
    public int getItemCount() {
        return firebaseReferences.size();
    }

}
