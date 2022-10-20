package com.example.firebasegsocapp.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.firebasegsocapp.R;

import static android.text.Layout.JUSTIFICATION_MODE_INTER_WORD;


public class LearnMoreActivity extends AppCompatActivity {

    TextView txtViewReturn;
    TextView txtViewEAT;
    TextView txtViewGSOC;
    TextView txtViewPanSot;
    TextView txtViewSteVla;
    TextView txtViewEmail;
    TextView txtViewParagraphs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learn_more);

        init();
        setListeners();
    }

    private void init(){
        txtViewReturn = findViewById(R.id.txtViewReturn);
        txtViewEAT = findViewById(R.id.txtViewEAT);
        txtViewGSOC = findViewById(R.id.txtViewGSOC);
        txtViewPanSot = findViewById(R.id.txtViewPanSot);
        txtViewSteVla = findViewById(R.id.txtViewSteVla);
        txtViewEmail = findViewById(R.id.txtViewEmail);
        txtViewParagraphs = findViewById(R.id.txtViewParagraphs);

        SpannableString content;

        content = new SpannableString("- EAT on GitHub");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        txtViewEAT.setText(content);

        content = new SpannableString("- Full GSoC 2022 submission");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        txtViewGSOC.setText(content);

        content = new SpannableString("- Panagiotis Sotiropoulos on GitHub");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        txtViewPanSot.setText(content);

        content = new SpannableString("- Stefanos Vlachos on GitHub");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        txtViewSteVla.setText(content);

        content = new SpannableString("eat.testsuite@gmail.com");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        txtViewEmail.setText(content);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            txtViewParagraphs.setJustificationMode(JUSTIFICATION_MODE_INTER_WORD);
        }
    }

    private void setListeners(){
        txtViewReturn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        txtViewEAT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/EAT-JBCOMMUNITY/EAT"));
                startActivity(browserIntent);
            }
        });

        txtViewGSOC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/stefanos-vlachos/GSoC-2021_EAT_JBoss-Community"));
                startActivity(browserIntent);
            }
        });

        txtViewPanSot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/EAT-JBCOMMUNITY"));
                startActivity(browserIntent);
            }
        });

        txtViewSteVla.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/stefanos-vlachos"));
                startActivity(browserIntent);
            }
        });

        txtViewEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent email= new Intent(Intent.ACTION_SENDTO);
                email.setData(Uri.parse("mailto:eat.testsuite@gmail.com"));
                email.putExtra(Intent.EXTRA_SUBJECT, "Subject");
                email.putExtra(Intent.EXTRA_TEXT, "My Email message");
                startActivity(email);
            }
        });
    }
}
