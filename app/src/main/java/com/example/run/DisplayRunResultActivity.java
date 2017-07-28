package com.example.run;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.TextView;

import java.io.File;


public class DisplayRunResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_run_result);

        Bundle extras = getIntent().getExtras();
        String filename  = extras.getString(MapsActivity.EXTRA_MESSAGE_FILENAME);

        String resultText = DataManager.readFile(filename, this);

        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(resultText);
    }
}
