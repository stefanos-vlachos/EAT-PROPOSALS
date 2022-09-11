package com.example.firebasegsocapp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.firebasegsocapp.R;
import com.example.firebasegsocapp.domain.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import org.jetbrains.annotations.NotNull;

import static com.example.firebasegsocapp.activity.MainActivity.FIREBASE_FIRESTORE;
import static com.example.firebasegsocapp.activity.MainActivity.getFirebaseAuth;

public class LoginActivity extends AppCompatActivity {

    TextInputEditText edtTextLoginEmail;
    TextInputEditText edtTextLoginPassword;
    Button buttonLogin;
    TextView txtViewLoginAltMethod;
    TextView txtViewCancelLogin;
    TextView txtViewLoginTitle;
    TextView txtViewLoginShowPassword;
    FirebaseAuth firebaseAuth;

    private static final int REQ_ONE_TAP = 2;  // Can be any integer unique to the Activity.
    private boolean showOneTapUI = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();
        setListeners();
    }

    @Override
    public void onBackPressed() {
        cancelActivity();
    }

    private void init() {
        firebaseAuth = MainActivity.getFirebaseAuth();
        txtViewCancelLogin = findViewById(R.id.txtViewCancelLogin);
        txtViewLoginTitle = findViewById(R.id.txtViewLoginTitle);
        edtTextLoginEmail = findViewById(R.id.edtTextLoginEmail);
        edtTextLoginPassword = findViewById(R.id.edtTextLoginPassword);
        txtViewLoginShowPassword = findViewById(R.id.txtViewLoginShowPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        txtViewLoginAltMethod = findViewById(R.id.txtViewLoginAltMethod);
    }

    private void setListeners() {
        txtViewCancelLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancelActivity();
            }
        });

        txtViewLoginShowPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txtViewLoginShowPassword.getText().equals("SHOW")){
                    edtTextLoginPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    txtViewLoginShowPassword.setText("HIDE");
                    return;
                }
                edtTextLoginPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                txtViewLoginShowPassword.setText("SHOW");
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = edtTextLoginEmail.getText().toString();
                String password = edtTextLoginPassword.getText().toString();
                if(checkUserInput(username, password))
                    signIn(username, password);
            }
        });

        txtViewLoginAltMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), CreateAccountActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void cancelActivity(){
        finish();
    }

    private boolean checkUserInput (String email, String password) {
        if(TextUtils.isEmpty(email) || TextUtils.isEmpty(password)){
            if(TextUtils.isEmpty(email))
                edtTextLoginEmail.setError("E-mail can not be empty");
            if(TextUtils.isEmpty(password))
                edtTextLoginPassword.setError("Password can not be empty");
            return false;
        }
        return true;
    }

    private void handleLoginErrors (String exception) {
        if(exception.equals("The email address is badly formatted"))
            edtTextLoginEmail.setError(exception);
        if(exception.contains("There is no user record corresponding")) {
            edtTextLoginEmail.setError((exception));
            txtViewLoginAltMethod.setTextColor(getResources().getColor(R.color.blood_red));
        }
        if(exception.contains("The password is invalid"))
            edtTextLoginPassword.setError(exception);
    }

    private void signIn(String email, String password) {

        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser user = getFirebaseAuth().getCurrentUser();
                            if(!user.isEmailVerified() ){
                                AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
                                builder.setTitle("E-mail Verification")
                                        .setMessage("A sent E-mail verification is still pending. Please verify your E-mail to activate your account.")
                                        .setCancelable(false)
                                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                getFirebaseAuth().signOut();
                                                Intent resultIntent = new Intent();
                                                setResult(Activity.RESULT_OK, resultIntent);
                                                finish();
                                            }
                                        });
                                AlertDialog alert = builder.create();
                                alert.show();
                                return;
                            }

                            checkIfFirstLogin(email, user.getUid());
                        } else
                            handleLoginErrors(task.getException().getLocalizedMessage());
                    }
                });
    }

    private void checkIfFirstLogin(String email, String updatedId){
        FIREBASE_FIRESTORE.collection("pending_users").document(email).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if(doc.exists()){
                        FIREBASE_FIRESTORE.collection("pending_users").document(email).delete();
                        storeVerifiedUser(updatedId, doc.getString("email"), doc.getString("username"));
                    }

                    getFirebaseAuth().getCurrentUser().reload();
                    Intent resultIntent = new Intent();
                    setResult(Activity.RESULT_OK, resultIntent);
                    finish();
                }
            }
        });
    }

    private void storeVerifiedUser(String userIdToken, String userEmail, String userFullName){
        //Store in Firebase Real Time Database
        /*FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference users = database.getReference("users");
        User user = new User(userIdToken, userEmail, userFullName);
        users.push().setValue(user);*/

        //Store in Firebase Cloud Firestore
        FIREBASE_FIRESTORE.collection("users").document(userEmail)
                .set(new User(userIdToken, userEmail, userFullName))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        Toast.makeText(LoginActivity.this, "User Storage Failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
