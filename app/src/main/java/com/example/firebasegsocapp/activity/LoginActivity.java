package com.example.firebasegsocapp.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.firebasegsocapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import org.jetbrains.annotations.NotNull;

import static android.content.ContentValues.TAG;

public class LoginActivity extends AppCompatActivity {

    EditText edtTextEmail;
    EditText edtTextPassword;
    Button btnAuth;
    TextView txtViewAltMethod;
    TextView txtViewErrorAuth;
    TextView txtViewCancelAuth;
    TextView txtViewAuthHeader;
    TextView txtViewShowPassword;
    FirebaseAuth firebaseAuth;

    private static final int REQ_ONE_TAP = 2;  // Can be any integer unique to the Activity.
    private boolean showOneTapUI = true;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();

        Intent intent = getIntent();
        String activityType = intent.getStringExtra("type");
        refreshActivityState(activityType);

    }

    private void init() {
        firebaseAuth = MainActivity.getFirebaseAuth();
        edtTextEmail = findViewById(R.id.edtTextEmail);
        edtTextPassword = findViewById(R.id.edtTextPassword);
        btnAuth = findViewById(R.id.buttonAuth);
        txtViewAltMethod = findViewById(R.id.txtViewAltMethod);
        txtViewErrorAuth = findViewById(R.id.txtViewErrorAuth);
        txtViewCancelAuth = findViewById(R.id.txtViewCancelAuth);
        txtViewAuthHeader = findViewById(R.id.txtViewAuthHeader);
        txtViewShowPassword = findViewById(R.id.txtViewShowPassword);
    }

    private void refreshActivityState(String activityType) {
        //Reset fields
        edtTextEmail.setText("");
        edtTextPassword.setText("");

        if(txtViewShowPassword.getText().equals("HIDE")){
            edtTextPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            txtViewShowPassword.setText("SHOW");
        }

        txtViewErrorAuth.setText("");

        txtViewCancelAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        txtViewShowPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(txtViewShowPassword.getText().equals("SHOW")){
                    edtTextPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    txtViewShowPassword.setText("HIDE");
                }
                else if(txtViewShowPassword.getText().equals("HIDE")){
                    edtTextPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                    txtViewShowPassword.setText("SHOW");
                }
            }
        });

        //Choose between Login and Profile Creation
        if (activityType.equals("login")) {
            txtViewAuthHeader.setText("Login");
            btnAuth.setText("Login");
            btnAuth.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String username = edtTextEmail.getText().toString();
                    String password = edtTextPassword.getText().toString();
                    if(checkEmptyInput(username, password))
                        signIn(username, password);
                }
            });
            txtViewAltMethod.setText("New user?\nCreate an account.");
            txtViewAltMethod.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    refreshActivityState("create");
                }
            });

            return;
        }

        txtViewAuthHeader.setText("Create new account");
        btnAuth.setText("Create account");
        btnAuth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = edtTextEmail.getText().toString();
                String password = edtTextPassword.getText().toString();
                if(checkEmptyInput(username, password))
                    createAccount(username, password);
            }
        });
        txtViewAltMethod.setText("Already have an account?\nSign in.");
        txtViewAltMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshActivityState("login");
            }
        });
    }

    private boolean checkEmptyInput (String username, String password) {
        if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password)){
            txtViewErrorAuth.setText("Username and Password can not be empty.");
            return false;
        }
        return true;
    }

    private void createAccount (String email, String password) {

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    sendEmailVerification();
                    Intent returnIntent = new Intent();
                    setResult(Activity.RESULT_OK,returnIntent);
                    finish();
                }
                else {
                    txtViewErrorAuth.setText(task.getException().getMessage());
                }
            }
        });
    }

    private void signIn(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            sendEmailVerification();
                            Intent returnIntent = new Intent();
                            setResult(Activity.RESULT_OK,returnIntent);
                            finish();
                        } else
                            txtViewErrorAuth.setText(task.getException().getMessage());
                    }
                });
    }
    
    private void sendEmailVerification() {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        user.sendEmailVerification()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull @NotNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG,"Verification sent.");
                        }
                    }
                });
    }

}
