package com.example.firebasegsocapp.activity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import com.example.firebasegsocapp.R;
import com.example.firebasegsocapp.adapter.SliderAdapter;
import com.example.firebasegsocapp.domain.SliderData;
import com.example.firebasegsocapp.domain.User;
import com.google.android.gms.auth.api.identity.*;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.*;
import com.google.firebase.auth.*;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.*;
import com.smarteist.autoimageslider.SliderView;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity {

    public static FirebaseAuth FIREBASE_AUTH = FirebaseAuth.getInstance();
    public static FirebaseFirestore FIREBASE_FIRESTORE = FirebaseFirestore.getInstance();
    public static StorageReference STORAGE_REFERENCE = FirebaseStorage.getInstance().getReference();

    private final int UPLOAD_FILES_ACTIVITY_CODE = 1;
    private final int ONE_TAP_REGISTRATION_CODE = 2;
    private final String[] MIME_TYPES = {
            "application/pdf", "application/xml", "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "application/vnd.ms-excel", "application/xhtml+xml", "text/plain", "application/rtf",
            "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "application/vnd.ms-powerpoint", "application/vnd.oasis.opendocument.text", "application/json",
            "application/msword", "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "text/csv", "text/html", "application/x-tex", "image/jpeg", "image/png"
    };
    private static int filesToUpload;
    private static int uploadedFiles;
    private ProgressDialog progressDialog;

    private Button btnUploadFile;
    private Button btnViewFiles;
    private TextView txtViewLearnMore;
    private TextView txtViewLogin;
    private SignInClient oneTapClient;
    private BeginSignInRequest signInRequest;

    public static FirebaseAuth getFirebaseAuth() {
        return FIREBASE_AUTH;
    }
    public static StorageReference getStorageReference() {
        return STORAGE_REFERENCE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        init();
        setListeners();
        configureSlideshow();
    }

    private void init() {

        progressDialog = new ProgressDialog(this);
        txtViewLogin = findViewById(R.id.txtViewLogin);
        btnUploadFile = findViewById(R.id.btnUploadFile);
        btnViewFiles = findViewById(R.id.btnViewFiles);

        txtViewLearnMore = findViewById(R.id.txtViewLearnMore);
        SpannableString content = new SpannableString("Learn More");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        txtViewLearnMore.setText(content);
    }

    private void setListeners() {
        getFirebaseAuth().addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull @NotNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = getFirebaseAuth().getCurrentUser();

                if(user != null && user.isEmailVerified()) {
                    txtViewLogin.setText("Logout");
                    txtViewLogin.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getFirebaseAuth().signOut();
                        }
                    });
                    return;
                }

                txtViewLogin.setText("Sign Up | Sign In");
                txtViewLogin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //Use custom form for registering
                        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                        startActivity(intent);

                        //Use Google One-Tap
                        //initOneTapRegistration();
                    }
                });

            }
        });

        btnUploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent;
                if(getFirebaseAuth().getCurrentUser()!=null && getFirebaseAuth().getCurrentUser().isEmailVerified()){
                    intent = new Intent();
                    intent.setType("*/*");
                    intent.putExtra(Intent.EXTRA_MIME_TYPES, MIME_TYPES);
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                    intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Document"), UPLOAD_FILES_ACTIVITY_CODE);
                    return;
                }

                //Use custom form for registering
                intent = new Intent(getApplicationContext(), LoginActivity.class);
                startActivity(intent);

                //Use Google One-Tap
                //initOneTapRegistration();
            }
        });

        btnViewFiles.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ViewFilesActivity.class);
                startActivity(intent);
            }
        });

        txtViewLearnMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), LearnMoreActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initOneTapRegistration(){
        oneTapClient = Identity.getSignInClient(this);
        signInRequest = BeginSignInRequest.builder()
                .setGoogleIdTokenRequestOptions(BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                        .setSupported(true)
                        .setServerClientId(getString(R.string.default_web_client_id))
                        .setFilterByAuthorizedAccounts(false)
                        .build())
                .setAutoSelectEnabled(false)
                .build();

        oneTapClient.beginSignIn(signInRequest)
                .addOnSuccessListener(this, new OnSuccessListener<BeginSignInResult>() {
                    @Override
                    public void onSuccess(BeginSignInResult beginSignInResult) {
                        try {
                            startIntentSenderForResult(
                                    beginSignInResult.getPendingIntent().getIntentSender(), ONE_TAP_REGISTRATION_CODE,
                                    null, 0, 0, 0);
                        } catch (IntentSender.SendIntentException e) {
                            Toast.makeText(MainActivity.this, "Couldn't start One Tap UI: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void configureSlideshow(){
        ArrayList<SliderData> sliderDataArrayList = new ArrayList<>();

        SliderView sliderView = findViewById(R.id.slider);

        sliderDataArrayList.add(new SliderData("\"EAT is an additional JBOSS testsuite.\"",
                getResources().getIdentifier(getPackageName()+":drawable/image1", null, null)));
        sliderDataArrayList.add(new SliderData("\"This app contains Proposals and Documentation of EAT multi-version programs.\"",
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
                if(resultCode == RESULT_OK) {
                    //Handle multiple files
                    if(data.getClipData() != null){
                        progressDialog.setMessage("Uploading files ...");
                        progressDialog.show();

                        filesToUpload = data.getClipData().getItemCount();
                        for(int i=0; i<filesToUpload; i++){
                            Uri fileUri = data.getClipData().getItemAt(i).getUri();
                            uploadFile(fileUri);
                        }
                    }
                    //Handle single file
                    else if(data.getData() != null){
                        progressDialog.setMessage("Uploading file ...");
                        progressDialog.show();

                        filesToUpload = 1;
                        Uri fileUri = data.getData();
                        uploadFile(fileUri);
                    }
                }
                break;
            /*case ONE_TAP_REGISTRATION_CODE:
                try {
                    SignInCredential credential = oneTapClient.getSignInCredentialFromIntent(data);
                    String idToken = credential.getGoogleIdToken();
                    String fullName = credential.getDisplayName();
                    String email = credential.getId();
                    AuthCredential firebaseCredential = GoogleAuthProvider.getCredential(idToken, null);
                        getFirebaseAuth().signInWithCredential(firebaseCredential)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        // Sign in success, update UI with the signed-in user's information
                                        Toast.makeText(MainActivity.this, "signInWithCredential:success", Toast.LENGTH_SHORT).show();
                                        generateUserInDatabase(idToken, email, fullName);
                                        //updateRegisterDependentElements();
                                    } else {
                                        // If sign in fails, display a message to the user.
                                        Toast.makeText(MainActivity.this, "signInWithCredential:failure", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } catch (ApiException e) {
                    Toast.makeText(this, "ERROR: " + e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                }
                break;*/
        }
    }

    private void uploadFile(Uri fileUri){
        uploadedFiles = 0;
        final String dialogTitle;
        final String dialogSuccessMessage;
        final String dialogErrorMessage;
        if(filesToUpload > 1){
            dialogTitle = "Files Uploaded";
            dialogSuccessMessage = "Your files are undergoing a review process.\nOnce they are approved, they will be available on the app.";
            dialogErrorMessage = "Files upload failed. Try again.";
        } else{
            dialogTitle = "File Uploaded";
            dialogSuccessMessage = "Your file is undergoing a review process.\nOnce it is approved, it will be available on the app.";
            dialogErrorMessage = "File upload failed. Try again.";
        }
        HashMap<String, String> fileInfo = new HashMap<>(getFileInfo(fileUri));
        String fileName = fileInfo.get("fileName");
        String fileExtension = fileInfo.get("fileExtension");
        StorageReference reference = getStorageReference().child("pending-files").child(fileName + fileExtension);
        reference.putFile(fileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        uploadedFiles++;
                        if(uploadedFiles == filesToUpload){
                            progressDialog.dismiss();
                            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                            builder.setTitle(dialogTitle)
                                    .setMessage(dialogSuccessMessage)
                                    .setCancelable(false)
                                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    });
                            AlertDialog alert = builder.create();
                            alert.show();
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(MainActivity.this, dialogErrorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //method to compose the name of the uploaded files
    @SuppressLint("Range")
    private HashMap<String,String> getFileInfo(Uri fileUri) {
        HashMap<String,String> fileInfo = new HashMap<>();
        String filePath;
        String fileName = null;
        String fileExtension = null;
        int cut;

        if(fileUri.getScheme().equals("content")) {
            Cursor cursor = getContentResolver().query(fileUri, null, null, null, null);
            try {
                if( cursor != null && cursor.moveToFirst()) {
                    filePath = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                    cut = filePath.lastIndexOf('.');
                    if(cut != -1) {
                        fileName = filePath.substring(0, cut);
                        fileExtension = filePath.substring(cut);
                    }
                    fileInfo.put("fileName", fileName);
                    fileInfo.put("fileExtension", fileExtension);
                }
            } finally {
                cursor.close();
            }
        }
        if (fileName == null) {
            filePath = fileUri.getPath();
            cut = filePath.lastIndexOf('/');
            if (cut != -1) {
                fileName = filePath.substring(cut + 1);
                fileInfo.put("fileName", fileName);
                fileInfo.put("fileExtension", "");
            }
        }

        return fileInfo;
    }

}