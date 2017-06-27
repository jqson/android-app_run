package com.example.run;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {
    static final String HISTORY_FILENAME = "history.run";
    static final String NO_GHOST_FILENAME = "NO_GHOST";

    private HistoryDbHelper mDbHelper;
    private List<RunHistory> mHistoryList;

    private ListView mHistoryListView;

    private  HistoryAdapter mItemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        // TODO immigrate to database

        mDbHelper = new HistoryDbHelper(this);

        //mDbHelper.clearHistory();
        //moveFileDataToDb();

        mHistoryListView = (ListView) findViewById(R.id.history_list);

        mHistoryList = mDbHelper.getAllHistory();

        initListView();
    }

    @Override
    protected  void onResume() {
        super.onResume();
        mHistoryList = mDbHelper.getAllHistory();
        mItemsAdapter.clear();
        mItemsAdapter.addAll(mHistoryList);
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


    private void moveFileDataToDb() {
        if (!historyExistence()) {
            // Create history file
            String fileHead = "filename,dateMs,time,dist\n";
            DataManager.writeFile(HISTORY_FILENAME, fileHead, this);
        }

        String fileListStr = DataManager.readFile(HISTORY_FILENAME, this);
        List<String> historyLineList = Arrays.asList(fileListStr.split("\n"));

        for (int i = 1; i < historyLineList.size(); i++) {
            String[] fileInfoArray = historyLineList.get(i).split(",");
            RunHistory newHistory = new RunHistory(
                    Long.valueOf(fileInfoArray[1]),
                    Integer.valueOf(fileInfoArray[2]),
                    Float.valueOf(fileInfoArray[3]),
                    String.valueOf(fileInfoArray[0]));
            mDbHelper.addHistory(newHistory);
        }
    }

    private void initListView() {
        mItemsAdapter = new HistoryAdapter(this, mHistoryList);
        mHistoryListView.setAdapter(mItemsAdapter);
        mHistoryListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                String filename = mHistoryList.get((int) id).getFilename();
                //showResult(filename);
                runWithGhost(filename);
            }
        });

        mHistoryListView.setEmptyView((TextView)findViewById(R.id.no_history));
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
