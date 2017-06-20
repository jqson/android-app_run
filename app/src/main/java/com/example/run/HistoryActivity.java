package com.example.run;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    final static String HISTORY_FILENAME = "history.run";

    private ListView mHistoryListView;
    private List<String> mFileList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        if (historyExistance()) {
            String fileListStr = DataManager.readFile(HISTORY_FILENAME, this);
            mFileList = Arrays.asList(fileListStr.split("\n"));
        } else {
            mFileList = new ArrayList<>();
            // TODO remove after init
            mFileList.add("sample1.csv");
            mFileList.add("sample2.csv");
            mFileList.add("sample3.csv");
            mFileList.add("sample4.csv");
        }

        ArrayAdapter<String> itemsAdapter =
                new ArrayAdapter<String>(this, R.layout.history_list_item, mFileList);
        mHistoryListView = (ListView) findViewById(R.id.history_list);
        mHistoryListView.setAdapter(itemsAdapter);

        mHistoryListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                String filename = mFileList.get((int) id);
                //showResult(filename);
                runWithGhost(filename);
            }
        });
    }

    private boolean historyExistance() {
        File file = getBaseContext().getFileStreamPath(HISTORY_FILENAME);
        return file.exists();
    }

    private void showResult(String filename) {
        Intent intent = new Intent(this, DisplayRunResultActivity.class);
        intent.putExtra(MapsActivity.EXTRA_MESSAGE, filename);
        startActivity(intent);
    }

    private void runWithGhost(String filename) {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra(MapsActivity.EXTRA_MESSAGE, filename);
        startActivity(intent);
    }
}
