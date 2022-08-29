package com.example.firebasegsocapp;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.google.android.gms.tasks.*;
import com.google.firebase.storage.*;
import com.smarteist.autoimageslider.SliderView;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    Button uploadButton;
    Button viewButton;
    TextView learnMoreTxtView;
    TextView loginTxtView;
    Uri pdfUri = null;
    StorageReference storageReference;
    ArrayList<FirebaseFile> fileReferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        storageReference = FirebaseStorage.getInstance().getReference();
        fileReferences =  new ArrayList<>();

        uploadButton = findViewById(R.id.uploadButton);
        viewButton = findViewById(R.id.viewButton);
        learnMoreTxtView = findViewById(R.id.txtViewLearnMore);
        loginTxtView = findViewById(R.id.loginTxtView);

        SpannableString content = new SpannableString("Learn More");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        learnMoreTxtView.setText(content);

        uploadButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("application/pdf");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Document"), 12);
            }
        });

        viewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                storageReference.listAll().addOnSuccessListener(new OnSuccessListener<ListResult>() {
                    @Override
                    public void onSuccess(ListResult listResult) {
                        for(StorageReference fileReference : listResult.getItems()) {
                            String filePath = fileReference.getPath();
                            int cut = filePath.lastIndexOf(".");
                            String fileName = filePath.substring(1,cut);
                            String fileType = filePath.substring(cut+1);
                            fileReferences.add(new FirebaseFile(filePath, fileName, fileType));
                        }

                        Intent intent = new Intent(getApplicationContext(), ViewFilesActivity.class);
                        intent.putExtra("fileReferences", fileReferences);
                        startActivity(intent);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(MainActivity.this, "Process failed.", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // we are creating array list for storing our image urls.
        ArrayList<SliderData> sliderDataArrayList = new ArrayList<>();

        // initializing the slider view.
        SliderView sliderView = findViewById(R.id.slider);

        // adding the urls inside array list
        sliderDataArrayList.add(new SliderData("\"EAT is an additional JBOSS testsuite.\"", getResources().getIdentifier(getPackageName()+":drawable/image1", null, null)));
        sliderDataArrayList.add(new SliderData("\"Write your tests once and run them against any version of EAP and WILDFLY application server.\"", getResources().getIdentifier(getPackageName()+":drawable/image2", null, null)));
        sliderDataArrayList.add(new SliderData("\"Use this app to view files related to EAT.\"", getResources().getIdentifier(getPackageName()+":drawable/image3", null, null)));
        sliderDataArrayList.add(new SliderData("\"You can also upload new files.\"", getResources().getIdentifier(getPackageName()+":drawable/image4", null, null)));


        // passing this array list inside our adapter class.
        SliderAdapter adapter = new SliderAdapter(this, sliderDataArrayList);

        // below method is used to set auto cycle direction in left to
        // right direction you can change according to requirement.
        sliderView.setAutoCycleDirection(SliderView.LAYOUT_DIRECTION_LTR);

        // below method is used to
        // setadapter to sliderview.
        sliderView.setSliderAdapter(adapter);

        // below method is use to set
        // scroll time in seconds.
        sliderView.setScrollTimeInSec(5);

        // to set it scrollable automatically
        // we use below method.
        sliderView.setAutoCycle(true);

        // to start autocycle below method is used.
        sliderView.startAutoCycle();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && data.getData()!=null) {

            pdfUri = data.getData();
            String fileName = getFileName(pdfUri);
            Toast.makeText(this, fileName, Toast.LENGTH_SHORT).show();

            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Uploading");
            progressDialog.show();

            StorageReference reference = storageReference.child(fileName + ".pdf");
            Toast.makeText(MainActivity.this, reference.getName(), Toast.LENGTH_SHORT).show();

            reference.putFile(pdfUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Uploaded Succesfully", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() )/ taskSnapshot
                                .getTotalByteCount();
                        progressDialog.setMessage("Uploaded " + (int) progress + "%");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, "Failed", Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

    //method to compose the name of the uploaded files
    @SuppressLint("Range")
    private String getFileName(Uri fileUri) {

        String fileName = null;
        int cut;

        if(pdfUri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(pdfUri, null, null, null, null);
            try {
                if( cursor != null && cursor.moveToFirst()) {
                    fileName = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    cut = fileName.lastIndexOf('.');
                    if(cut != -1)
                        fileName = fileName.substring(0, cut);
                    return fileName;
                }
            } finally {
                cursor.close();
            }
        }
        if (fileName == null) {
            fileName = fileUri.getPath();
            cut = fileName.lastIndexOf('/');
            if (cut != -1) {
                fileName = fileName.substring(cut + 1);
            }
        }

        return fileName;
    }

}