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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.*;
import com.smarteist.autoimageslider.SliderView;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private final int REGISTER_ACTIVITY_CODE = 1;
    private final int UPLOAD_FILES_ACTIVITY_CODE = 2;
    public static FirebaseAuth FIREBASE_AUTH = FirebaseAuth.getInstance();

    Button btnUploadFile;
    Button btnViewFiles;
    TextView txtViewLearnMore;
    TextView txtViewLogin;
    Uri pdfUri;
    StorageReference storageReference;
    ArrayList<FirebaseFile> fileReferences;

    public static FirebaseAuth getFirebaseAuth() {
        return FIREBASE_AUTH;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        updateRegisterDependentElements();
        setListeners();
        configureSlideshow();
    }

    private void init() {
        storageReference = FirebaseStorage.getInstance().getReference();
        fileReferences =  new ArrayList<>();

        txtViewLogin = findViewById(R.id.txtViewLogin);
        btnUploadFile = findViewById(R.id.btnUploadFile);
        btnViewFiles = findViewById(R.id.btnViewFiles);
        txtViewLearnMore = findViewById(R.id.txtViewLearnMore);
        SpannableString content = new SpannableString("Learn More");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        txtViewLearnMore.setText(content);
    }

    private void updateRegisterDependentElements() {
        if(getFirebaseAuth().getCurrentUser()!=null){
            txtViewLogin.setText("Logout");
            txtViewLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    FIREBASE_AUTH.signOut();
                    updateRegisterDependentElements();
                }
            });
        } else {
            txtViewLogin.setText("Sign Up | Sign In");
            txtViewLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                    intent.putExtra("type", "login");
                    startActivityForResult(intent, REGISTER_ACTIVITY_CODE);
                }
            });
        }
    }

    private void setListeners() {
        btnUploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if(FIREBASE_AUTH.getCurrentUser()!=null){
                    intent = new Intent();
                    intent.setType("application/pdf");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Document"), UPLOAD_FILES_ACTIVITY_CODE);
                }
                else{
                    intent = new Intent(getApplicationContext(), LoginActivity.class);
                    intent.putExtra("type", "login");
                    startActivityForResult(intent, REGISTER_ACTIVITY_CODE);
                }
            }
        });

        btnViewFiles.setOnClickListener(new View.OnClickListener() {
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
    }

    private void configureSlideshow(){
        ArrayList<SliderData> sliderDataArrayList = new ArrayList<>();

        SliderView sliderView = findViewById(R.id.slider);

        sliderDataArrayList.add(new SliderData("\"EAT is an additional JBOSS testsuite.\"",
                getResources().getIdentifier(getPackageName()+":drawable/image1", null, null)));
        sliderDataArrayList.add(new SliderData("\"Write your tests once and run them against any version of EAP and WILDFLY application server.\"",
                getResources().getIdentifier(getPackageName()+":drawable/image2", null, null)));
        sliderDataArrayList.add(new SliderData("\"Use this app to view files related to EAT.\"",
                getResources().getIdentifier(getPackageName()+":drawable/image3", null, null)));
        sliderDataArrayList.add(new SliderData("\"You can also upload new files.\"",
                getResources().getIdentifier(getPackageName()+":drawable/image4", null, null)));

        SliderAdapter adapter = new SliderAdapter(this, sliderDataArrayList);

        sliderView.setAutoCycleDirection(SliderView.LAYOUT_DIRECTION_LTR);

        sliderView.setSliderAdapter(adapter);

        sliderView.setScrollTimeInSec(5);

        sliderView.setAutoCycle(true);

        sliderView.startAutoCycle();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case UPLOAD_FILES_ACTIVITY_CODE:
                if(resultCode == RESULT_OK && data.getData()!=null) {
                    pdfUri = data.getData();
                    String fileName = getFileName(pdfUri);
                    Toast.makeText(this, fileName, Toast.LENGTH_SHORT).show();

                    final ProgressDialog progressDialog = new ProgressDialog(this);
                    progressDialog.setMessage("Uploading");
                    progressDialog.show();

                    StorageReference reference = storageReference.child(fileName + ".pdf");
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
                                    Toast.makeText(MainActivity.this, "File upload failed.Try again", Toast.LENGTH_SHORT).show();
                                }
                            });
                }
                break;

            case REGISTER_ACTIVITY_CODE:
                if(resultCode == RESULT_OK)
                    updateRegisterDependentElements();
                break;
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