package com.example.firebasegsocapp;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import org.jetbrains.annotations.NotNull;

public class LoginActivity extends AppCompatActivity {

    EditText edtTextUsername;
    EditText edtTextPassword;
    Button btnLogin;
    TextView txtViewCreateAccount;
    TextView txtViewErrorLogin;
    TextView txtViewCancelLogin;
    TextView loginHeaderTxtView;
    TextView txtViewShowPassword;
    //FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();

        Intent intent = getIntent();
        String activityType = intent.getStringExtra("type");
        setActivityState(activityType);

    }

    private void init() {
        edtTextUsername = findViewById(R.id.userNameEdtText);
        edtTextPassword = findViewById(R.id.edtTextPassword);
        btnLogin = findViewById(R.id.buttonLogin);
        txtViewCreateAccount = findViewById(R.id.txtViewCreateAccount);
        txtViewErrorLogin = findViewById(R.id.txtViewErrorLogin);
        txtViewCancelLogin = findViewById(R.id.txtViewCancelLogin);
        loginHeaderTxtView = findViewById(R.id.loginHeaderTxtView);
        txtViewShowPassword = findViewById(R.id.txtViewShowPassword);
    }

    private void setActivityState(String activityType) {
        txtViewErrorLogin.setText("");
        edtTextUsername.setText("");
        edtTextPassword.setText("");
        if(txtViewShowPassword.getText().equals("HIDE")){
            edtTextPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
            txtViewShowPassword.setText("SHOW");
        }

        txtViewCancelLogin.setOnClickListener(new View.OnClickListener() {
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

        if (activityType.equals("login")) {
            loginHeaderTxtView.setText("Login");
            btnLogin.setText("Login");
            txtViewCreateAccount.setText("New user?\nCreate an account.");

            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String username = edtTextUsername.getText().toString();
                    String password = edtTextPassword.getText().toString();
                    if(checkEmptyInput(username, password))
                        signIn(username, password);
                }
            });

            txtViewCreateAccount.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    setActivityState("create");
                }
            });

            return;
        }

        loginHeaderTxtView.setText("Create new account");
        btnLogin.setText("Create account");
        txtViewCreateAccount.setText("Already have an account?\nSign in.");

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = edtTextUsername.getText().toString();
                String password = edtTextPassword.getText().toString();
                if(checkEmptyInput(username, password))
                    createAccount(username, password);
            }
        });

        txtViewCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setActivityState("login");
            }
        });
    }

    private boolean checkEmptyInput (String username, String password) {
        if(TextUtils.isEmpty(username) || TextUtils.isEmpty(password)){
            txtViewErrorLogin.setText("Username and Password can not be empty.");
            return false;
        }
        return true;
    }

    private void createAccount (String email, String password) {
        MainActivity.FIREBASE_AUTH.createUserWithEmailAndPassword(email, password).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<AuthResult> task) {
                if(task.isSuccessful()) {
                    Intent returnIntent = new Intent();
                    setResult(Activity.RESULT_OK,returnIntent);
                    finish();
                }
                else {
                    txtViewErrorLogin.setText(task.getException().getMessage());
                }
            }
        });
    }

    private void signIn(String email, String password) {
        MainActivity.FIREBASE_AUTH.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Intent returnIntent = new Intent();
                            setResult(Activity.RESULT_OK,returnIntent);
                            finish();
                        } else
                            txtViewErrorLogin.setText(task.getException().getMessage());
                    }
                });
    }

}
