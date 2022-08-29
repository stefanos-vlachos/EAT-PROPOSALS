package com.example.firebasegsocapp;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    EditText edtTextUsername;
    EditText edtTextPassword;
    Button btnLogin;
    TextView txtViewCreateAccount;
    TextView txtViewErrorLogin;
    TextView txtViewCancelLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        edtTextUsername = findViewById(R.id.userNameEdtText);
        edtTextPassword = findViewById(R.id.edtTextPassword);
        btnLogin = findViewById(R.id.buttonLogin);
        txtViewCreateAccount = findViewById(R.id.txtViewCreateAccount);
        txtViewErrorLogin = findViewById(R.id.txtViewErrorLogin);
        txtViewCancelLogin = findViewById(R.id.txtViewCancelLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String username = edtTextUsername.getText().toString();
                String password = edtTextPassword.getText().toString();

                if(!TextUtils.isEmpty(username) && !TextUtils.isEmpty(password)){
                    //Check in database
                } else {
                    txtViewErrorLogin.setText("Username and Password can not be empty.");
                }
            }
        });

        txtViewCreateAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //start profile creation
            }
        });

        txtViewCancelLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

    }
}
