package com.example.firebasegsocapp;

import android.content.Intent;
import android.os.Bundle;
import android.webkit.WebView;
import androidx.appcompat.app.AppCompatActivity;

public class ViewSingleFileActivity extends AppCompatActivity {

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activty_view_single_file);

        webView = (WebView) findViewById(R.id.webView);

        viewFile();
    }

    private void viewFile(){
        Intent intent = getIntent();
        String fileUri = intent.getStringExtra("fileUri");
        System.out.println("OPAAAAA " + fileUri);
        webView.loadUrl("https://www.tutorialspoint.com/android/android_webview_layout.htm");
    }

}
