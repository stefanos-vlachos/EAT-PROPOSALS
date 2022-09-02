package com.example.firebasegsocapp.activity;

import android.os.Bundle;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.firebasegsocapp.R;


public class LearnMoreActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_more);

        WebView browser = findViewById(R.id.webview);
        browser.loadUrl("https://github.com/EAT-JBCOMMUNITY/EAT#readme");
    }
}
