package com.example.run;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ListView;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class HistoryActivity extends AppCompatActivity {

    static final String HISTORY_FILENAME = "history.run";
    static final long NEW_ROUTE_FLAG = -1;
    static final long NO_ROUTE_FLAG = -2;

    private boolean mShowRunHistory = false;

    private DatabaseHelper mDbHelper;
    private List<RunHistory> mRunList;
    private List<Route> mRouteList;

    private ListView mHistoryListView;

    private RunHistoryAdapter mRunItemsAdapter;
    private RouteAdapter mRouteItemsAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        mDbHelper = new DatabaseHelper(this);

        mHistoryListView = (ListView) findViewById(R.id.history_list);

        if (mShowRunHistory) {
            initRunListView();
        } else {
            initRouteListView();
        }
    }

    @Override
    protected  void onResume() {
        super.onResume();

        if (mShowRunHistory) {
            mRunList = mDbHelper.getAllHistory();
            mRunItemsAdapter.clear();
            mRunItemsAdapter.addAll(mRunList);
        } else {
            mRouteList = mDbHelper.getAllRoute();
            mRouteItemsAdapter.clear();
            mRouteItemsAdapter.addAll(mRouteList);
        }
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

    private void initRunListView() {
        mRunList = mDbHelper.getAllHistory();
        mRunItemsAdapter = new RunHistoryAdapter(this, mRunList);
        mHistoryListView.setAdapter(mRunItemsAdapter);
        mHistoryListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                long routeId = mDbHelper.getRouteId(mRunList.get((int) id).getId(), NO_ROUTE_FLAG);
                runOnRoute(routeId, mRunList.get((int) id).getFilename());
            }
        });

        mHistoryListView.setEmptyView(findViewById(R.id.no_history));
    }

    private void initRouteListView() {
        mRouteList = mDbHelper.getAllRoute();
        mRouteItemsAdapter = new RouteAdapter(this, mRouteList);
        mHistoryListView.setAdapter(mRouteItemsAdapter);
        mHistoryListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
                runOnRoute(mRouteList.get((int) id).getId(),
                        mRouteList.get((int) id).getFilename());
            }
        });

        mHistoryListView.setEmptyView(findViewById(R.id.no_history));
    }

    private void runOnRoute(long routeId, String filename) {
        Intent intent = new Intent(this, MapsActivity.class);
        Bundle extras = new Bundle();
        extras.putLong(MapsActivity.EXTRA_MESSAGE_ROUTEID, routeId);
        extras.putString(MapsActivity.EXTRA_MESSAGE_FILENAME, filename);
        intent.putExtras(extras);
        startActivity(intent);
    }

    public void runNewRoute(View view) {
        runOnRoute(NEW_ROUTE_FLAG, "");
    }

    public void showRoute(View view) {
        if (mShowRunHistory) {
            initRouteListView();
            mShowRunHistory = false;
        }
    }

    public void showAllRun(View view) {
        if (!mShowRunHistory) {
            initRunListView();
            mShowRunHistory = true;
        }
    }

    void updateDatabase() {
        //mDbHelper.clearTables();
        //moveFileDataToDb();

        //mDbHelper.tempClear();

        /*
        long[] id = {13};
        mDbHelper.newRoute(mDbHelper.getHistory(id[0]), "Route");
        for (int i = 1; i < id.length; i++) {
            mDbHelper.appendRunToRoute(mDbHelper.getHistory(id[i]), 3);
        }
        */
    }
}
