package com.example.run;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;


public class DisplayRunResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_run_result);

        /*
        Intent intent = getIntent();
        String filename = intent.getStringExtra(MapsActivity.EXTRA_MESSAGE);

        String resultText = DataManager.readFile(filename, this);
        */
        File file = getFilesDir();

        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(TextUtils.join("\n", file.list()));
    }
}
