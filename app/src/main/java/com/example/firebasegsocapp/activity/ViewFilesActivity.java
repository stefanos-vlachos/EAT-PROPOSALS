package com.example.firebasegsocapp.activity;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.firebasegsocapp.adapter.FilesAdapter;
import com.example.firebasegsocapp.FirebaseFile;
import com.example.firebasegsocapp.R;

import java.util.ArrayList;

public class ViewFilesActivity extends AppCompatActivity {

    private ArrayList<FirebaseFile> firebaseFiles;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_files);


        Intent intent = getIntent();
        firebaseFiles = (ArrayList<FirebaseFile>) intent.getSerializableExtra("fileReferences");

        RecyclerView rvFiles = findViewById(R.id.rvFiles);
        rvFiles.setLayoutManager(new GridLayoutManager(this, 3));
        FilesAdapter adapter = new FilesAdapter(firebaseFiles);
        rvFiles.setAdapter(adapter);
    }

}
