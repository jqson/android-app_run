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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    static final String HISTORY_FILENAME = "history.run";
    static final String NO_GHOST_FILENAME = "NO_GHOST";

    private static final String DATE_TIME_PATTERN = "EEE, d MMM yy h:mm a";

    private HistoryDbHelper mDbHelper;
    private List<RunHistory> mHistoryList;

    private ListView mHistoryListView;

    private List<String> mHistoryLineList;
    private List<String> mDisplayList;
    private List<String> mFilenameList;

    private  ArrayAdapter<String> mItemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // TODO immigrate to database

        mDbHelper = new HistoryDbHelper(this);
        mHistoryListView = (ListView) findViewById(R.id.history_list);

        loadHistory();

        initListView();
    }

    @Override
    protected  void onResume() {
        super.onResume();
        loadHistory();
        mItemsAdapter.clear();
        mItemsAdapter.addAll(mDisplayList);
    }

    @Override
    protected void onDestroy() {
        mDbHelper.close();
        super.onDestroy();
    }

    private boolean historyExistence() {
        File file = getBaseContext().getFileStreamPath(HISTORY_FILENAME);
        return file.exists();
    }

    private void loadHistory() {
        if (!historyExistence()) {
            // Create history file
            String fileHead = "filename,dateMs,time,dist\n";
            DataManager.writeFile(HISTORY_FILENAME, fileHead, this);
        }

        String fileListStr = DataManager.readFile(HISTORY_FILENAME, this);
        mHistoryLineList = Arrays.asList(fileListStr.split("\n"));

        // generate history filename list and text display list
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_PATTERN);

        mFilenameList = new ArrayList<>();
        mDisplayList = new ArrayList<>();
        for (int i = mHistoryLineList.size() - 1; i > 0; i--) {
            String[] fileInfoArray = mHistoryLineList.get(i).split(",");
            mFilenameList.add(fileInfoArray[0]);
            mDisplayList.add(sdf.format(new Date(Long.valueOf(fileInfoArray[1]))).toString());
        }

        // database
        mHistoryList = mDbHelper.getAllHistory();
    }

    private void initListView() {
        // TODO adapt history list

        mItemsAdapter = new ArrayAdapter<>(this, R.layout.history_list_item, mDisplayList);
        mHistoryListView.setAdapter(mItemsAdapter);
        mHistoryListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                String filename = mFilenameList.get((int) id);
                //showResult(filename);
                runWithGhost(filename);
            }
        });
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

    public void runWithoutGhost(View view) {
        runWithGhost(NO_GHOST_FILENAME);
    }
}
