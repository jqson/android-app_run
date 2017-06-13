package com.example.run;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;


public class DisplayRunResultActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_run_result);

        Intent intent = getIntent();
        String filename = intent.getStringExtra(MapsActivity.EXTRA_MESSAGE);

        String resultText = "";

        try {
            InputStream inputStream = openFileInput(filename);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                    stringBuilder.append("\n");
                }

                inputStream.close();
                resultText = stringBuilder.toString();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        TextView textView = (TextView) findViewById(R.id.textView);
        textView.setText(resultText);
    }
}
