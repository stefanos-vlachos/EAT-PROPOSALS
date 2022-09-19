package com.example.firebasegsocapp.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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

    private TextInputEditText edtTextLoginEmail;
    private TextInputEditText edtTextLoginPassword;
    private Button buttonLogin;
    private TextView txtViewLoginAltMethod;
    private TextView txtViewCancelLogin;
    private TextView txtViewLoginTitle;
    private TextView txtViewLoginShowPassword;
    private TextView txtViewForgotPassword;
    private FirebaseAuth firebaseAuth;
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        init();
        setListeners();
    }

    @Override
    public void onBackPressed() {
        finish();
    }

    private void init() {
        progressDialog = new ProgressDialog(this);
        firebaseAuth = MainActivity.getFirebaseAuth();
        txtViewCancelLogin = findViewById(R.id.txtViewCancelLogin);
        txtViewLoginTitle = findViewById(R.id.txtViewLoginTitle);
        edtTextLoginEmail = findViewById(R.id.edtTextLoginEmail);
        edtTextLoginPassword = findViewById(R.id.edtTextLoginPassword);
        txtViewLoginShowPassword = findViewById(R.id.txtViewLoginShowPassword);
        txtViewForgotPassword = findViewById(R.id.textViewForgotPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        txtViewLoginAltMethod = findViewById(R.id.txtViewLoginAltMethod);
    }

    private void setListeners() {
        txtViewCancelLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
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
                if(checkUserInput(username, password)) {
                    progressDialog.setMessage("Logging in ...");
                    progressDialog.show();
                    signIn(username, password);
                }
            }
        });

        txtViewLoginAltMethod.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("message", "switch_signup");
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });

        txtViewForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ResetPasswordActivity.class);
                startActivity(intent);
            }
        });
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
                                                resultIntent.putExtra("message","login_complete");
                                                setResult(Activity.RESULT_OK, resultIntent);
                                                progressDialog.dismiss();
                                                finish();
                                            }
                                        })
                                        .setNegativeButton("Send E-mail again", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                FirebaseUser user = firebaseAuth.getCurrentUser();
                                                user.sendEmailVerification()
                                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull @NotNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    progressDialog.dismiss();
                                                                    getFirebaseAuth().signOut();
                                                                    Intent resultIntent = new Intent();
                                                                    resultIntent.putExtra("message", "login_complete");
                                                                    setResult(Activity.RESULT_OK, resultIntent);

                                                                    finish();
                                                                }
                                                            }
                                                        })
                                                        .addOnFailureListener(new OnFailureListener() {
                                                            @Override
                                                            public void onFailure(@NonNull @NotNull Exception e) {
                                                                progressDialog.dismiss();
                                                                Toast.makeText(LoginActivity.this, "Process failed. Try again.", Toast.LENGTH_SHORT).show();
                                                            }
                                                        });
                                            }
                                        });
                                AlertDialog alert = builder.create();
                                alert.show();
                                return;
                            }
                            checkIfFirstLogin(email, user.getUid());
                        } else {
                            progressDialog.dismiss();
                            handleLoginErrors(task.getException().getLocalizedMessage());
                        }
                    }
                });
    }

    private void checkIfFirstLogin(String email, String updatedId){
        FIREBASE_FIRESTORE.collection("pending_users").document(email).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull @NotNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot doc = task.getResult();
                    if(doc.exists())
                        storeVerifiedUser(updatedId, doc.getString("email"), doc.getString("username"));
                    else{
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("message","login_complete");
                        setResult(Activity.RESULT_OK, resultIntent);
                        progressDialog.dismiss();
                        finish();
                    }
                }
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull @NotNull Exception e) {
                progressDialog.dismiss();
                Toast.makeText(LoginActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void storeVerifiedUser(String userIdToken, String userEmail, String userFullName){
        //Store in Firebase Cloud Firestore
        FIREBASE_FIRESTORE.collection("users").document(userEmail)
                .set(new User(userIdToken, userEmail, userFullName))
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        FIREBASE_FIRESTORE.collection("pending_users").document(userEmail).delete();
                        Intent resultIntent = new Intent();
                        resultIntent.putExtra("message","login_complete");
                        setResult(Activity.RESULT_OK, resultIntent);
                        progressDialog.dismiss();
                        finish();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull @NotNull Exception e) {
                        progressDialog.dismiss();
                        Toast.makeText(LoginActivity.this, e.getLocalizedMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
