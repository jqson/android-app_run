package com.example.run;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.run.DatabaseReaderContract.HistoryEntry;
import com.example.run.DatabaseReaderContract.RouteEntry;
import com.example.run.DatabaseReaderContract.RunRouteEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Qson on 6/23/2017.
 */

class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "RunHistory.db";

    private static final String DATABASE_ENABLE_FOREIGN_KEY = "PRAGMA foreign_keys=ON;";

    private static final String SQL_CREATE_TABLE_HISTORY =
            "CREATE TABLE " + HistoryEntry.TABLE_NAME + " (" +
                    HistoryEntry._ID + " INTEGER PRIMARY KEY," +
                    HistoryEntry.COLUMN_NAME_DATE + " INTEGER," +
                    HistoryEntry.COLUMN_NAME_TIME + " INTEGER," +
                    HistoryEntry.COLUMN_NAME_DISTANCE + " REAL," +
                    HistoryEntry.COLUMN_NAME_FILENAME + " TEXT)";

    private static final String SQL_DELETE_TABLE_HISTORY =
            "DROP TABLE IF EXISTS " + HistoryEntry.TABLE_NAME;

    private static final String SQL_CREATE_TABLE_ROUTE =
            "CREATE TABLE " + RouteEntry.TABLE_NAME + " (" +
                    RouteEntry._ID + " INTEGER PRIMARY KEY," +
                    RouteEntry.COLUMN_NAME_ROUTE_NAME + " TEXT," +
                    RouteEntry.COLUMN_NAME_COUNT + " INTEGER," +
                    RouteEntry.COLUMN_NAME_LAST_DATE + " INTEGER," +
                    RouteEntry.COLUMN_NAME_TIME + " INTEGER," +
                    RouteEntry.COLUMN_NAME_DISTANCE + " REAL," +
                    RouteEntry.COLUMN_NAME_FILENAME + " TEXT)";

    private static final String SQL_DELETE_TABLE_ROUTE =
            "DROP TABLE IF EXISTS " + RouteEntry.TABLE_NAME;

    private static final String SQL_CREATE_TABLE_RUN_ROUTE =
            "CREATE TABLE " + RunRouteEntry.TABLE_NAME + " (" +
                    RunRouteEntry._ID + " INTEGER PRIMARY KEY," +
                    RunRouteEntry.COLUMN_NAME_RUN_ID + " INTEGER REFERENCES " +
                    HistoryEntry.TABLE_NAME + " ON DELETE CASCADE ON UPDATE CASCADE," +
                    RunRouteEntry.COLUMN_NAME_ROUTE_ID + " INTEGER REFERENCES " +
                    RouteEntry.TABLE_NAME + " ON DELETE CASCADE ON UPDATE CASCADE)";

    private static final String SQL_DELETE_TABLE_RUN_ROUTE =
            "DROP TABLE IF EXISTS " + RunRouteEntry.TABLE_NAME;

    //private static final String DATABASE_ORDER_DATE_DESC =
    //        " ORDER BY " + HistoryEntry.COLUMN_NAME_DATE + " DESC";

    DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_TABLE_HISTORY);
        db.execSQL(SQL_CREATE_TABLE_ROUTE);
        db.execSQL(SQL_CREATE_TABLE_RUN_ROUTE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO
        clearTables();
    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (!db.isReadOnly()) {
            db.execSQL(DATABASE_ENABLE_FOREIGN_KEY);
        }
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    void clearTables() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(SQL_DELETE_TABLE_RUN_ROUTE);
        db.execSQL(SQL_DELETE_TABLE_ROUTE);
        db.execSQL(SQL_DELETE_TABLE_HISTORY);
        onCreate(db);
    }

    long addHistory(RunHistory history) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(HistoryEntry.COLUMN_NAME_DATE, history.getDateLong());
        values.put(HistoryEntry.COLUMN_NAME_TIME, history.getTime());
        values.put(HistoryEntry.COLUMN_NAME_DISTANCE, history.getDistance());
        values.put(HistoryEntry.COLUMN_NAME_FILENAME, history.getFilename());

        return db.insert(HistoryEntry.TABLE_NAME, null, values);
    }

    RunHistory getHistory(long historyId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + HistoryEntry.TABLE_NAME + " WHERE "
                + HistoryEntry._ID + " = " + historyId;

        Cursor c = db.rawQuery(selectQuery, null);

        if (c == null) {
            return null;
        }

        if (c.getCount() == 0) {
            c.close();
            return null;
        }

        c.moveToFirst();

        RunHistory history = new RunHistory(
                c.getInt(c.getColumnIndex(HistoryEntry._ID)),
                c.getLong(c.getColumnIndex(HistoryEntry.COLUMN_NAME_DATE)),
                c.getInt(c.getColumnIndex(HistoryEntry.COLUMN_NAME_TIME)),
                c.getFloat(c.getColumnIndex(HistoryEntry.COLUMN_NAME_DISTANCE)),
                c.getString(c.getColumnIndex(HistoryEntry.COLUMN_NAME_FILENAME)));

        c.close();

        return history;
    }

    List<RunHistory> getAllHistory() {
        List<RunHistory> histories = new ArrayList<RunHistory>();
        String selectQuery = "SELECT * FROM " + HistoryEntry.TABLE_NAME +
                " ORDER BY " + HistoryEntry.COLUMN_NAME_DATE + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                RunHistory history = new RunHistory(
                        c.getInt(c.getColumnIndex(HistoryEntry._ID)),
                        c.getLong(c.getColumnIndex(HistoryEntry.COLUMN_NAME_DATE)),
                        c.getInt(c.getColumnIndex(HistoryEntry.COLUMN_NAME_TIME)),
                        c.getFloat(c.getColumnIndex(HistoryEntry.COLUMN_NAME_DISTANCE)),
                        c.getString(c.getColumnIndex(HistoryEntry.COLUMN_NAME_FILENAME)));

                histories.add(history);
            } while (c.moveToNext());
        }

        c.close();

        return histories;
    }

    long deleteRun(long runId) {
        SQLiteDatabase db = this.getWritableDatabase();

        return db.delete(HistoryEntry.TABLE_NAME, HistoryEntry._ID + " = " + runId, null);
    }

    long newRoute(RunHistory history, String routeNamePrefix) {
        SQLiteDatabase db = this.getWritableDatabase();

        long runId = addHistory(history);

        ContentValues values = new ContentValues();
        values.put(RouteEntry.COLUMN_NAME_ROUTE_NAME, routeNamePrefix);
        values.put(RouteEntry.COLUMN_NAME_COUNT, 1);
        values.put(RouteEntry.COLUMN_NAME_LAST_DATE, history.getDateLong());
        values.put(RouteEntry.COLUMN_NAME_TIME, history.getTime());
        values.put(RouteEntry.COLUMN_NAME_DISTANCE, history.getDistance());
        values.put(RouteEntry.COLUMN_NAME_FILENAME, history.getFilename());

        long routeId = db.insert(RouteEntry.TABLE_NAME, null, values);

        String fullRouteName = routeNamePrefix + "-" + String.valueOf(routeId);
        updateRouteName(routeId, fullRouteName);

        addRunRoute(runId, routeId);

        return routeId;
    }

    int updateRouteName(long routeId, String routeName) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(RouteEntry.COLUMN_NAME_ROUTE_NAME, routeName);

        return db.update(RouteEntry.TABLE_NAME, values, RouteEntry._ID + " = ?",
                new String[] {String.valueOf(routeId)});
    }

    private long addRunRoute(long runId, long routeId) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(RunRouteEntry.COLUMN_NAME_RUN_ID, runId);
        values.put(RunRouteEntry.COLUMN_NAME_ROUTE_ID, routeId);

        return db.insert(RunRouteEntry.TABLE_NAME, null, values);
    }

    Route getRoute(long routeId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT * FROM " + RouteEntry.TABLE_NAME + " WHERE "
                + RouteEntry._ID + " = " + routeId;

        Cursor c = db.rawQuery(selectQuery, null);

        if (c == null) {
            return null;
        }

        if (c.getCount() == 0) {
            c.close();
            return null;
        }

        c.moveToFirst();

        Route route = new Route(
                c.getInt(c.getColumnIndex(RouteEntry._ID)),
                c.getString(c.getColumnIndex(RouteEntry.COLUMN_NAME_ROUTE_NAME)),
                c.getInt(c.getColumnIndex(RouteEntry.COLUMN_NAME_COUNT)),
                c.getLong(c.getColumnIndex(RouteEntry.COLUMN_NAME_LAST_DATE)),
                c.getInt(c.getColumnIndex(RouteEntry.COLUMN_NAME_TIME)),
                c.getFloat(c.getColumnIndex(RouteEntry.COLUMN_NAME_DISTANCE)),
                c.getString(c.getColumnIndex(RouteEntry.COLUMN_NAME_FILENAME)));

        c.close();

        return route;
    }

    private int getRunCount(long routeId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT COUNT(*) FROM " + RunRouteEntry.TABLE_NAME +
                " WHERE " + RunRouteEntry.COLUMN_NAME_ROUTE_ID + " = " + routeId;

        Cursor c = db.rawQuery(selectQuery, null);

        int count = 0;
        if (c != null) {
            if (c.moveToFirst()) {
                count = c.getInt(0);
            }
            c.close();
        }

        return count;
    }

    int appendRunToRoute(RunHistory history, long routeId) {
        SQLiteDatabase db = this.getWritableDatabase();

        long runId = addHistory(history);
        addRunRoute(runId, routeId);

        int runCount = getRunCount(routeId);

        ContentValues values = new ContentValues();
        values.put(RouteEntry.COLUMN_NAME_COUNT, runCount);
        values.put(RouteEntry.COLUMN_NAME_LAST_DATE, history.getDateLong());

        if (history.getTime() < getRoute(routeId).getBestTime()) {
            values.put(RouteEntry.COLUMN_NAME_TIME, history.getTime());
            values.put(RouteEntry.COLUMN_NAME_DISTANCE, history.getDistance());
            values.put(RouteEntry.COLUMN_NAME_FILENAME, history.getFilename());
        }

        return db.update(RouteEntry.TABLE_NAME, values, RouteEntry._ID + " = ?",
                new String[] {String.valueOf(routeId)});
    }

    List<Route> getAllRoute() {
        List<Route> routes = new ArrayList<Route>();
        String selectQuery = "SELECT * FROM " + RouteEntry.TABLE_NAME +
                " ORDER BY " + RouteEntry.COLUMN_NAME_LAST_DATE + " DESC";

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.rawQuery(selectQuery, null);

        // looping through all rows and adding to list
        if (c.moveToFirst()) {
            do {
                Route route = new Route(
                        c.getInt(c.getColumnIndex(RouteEntry._ID)),
                        c.getString(c.getColumnIndex(RouteEntry.COLUMN_NAME_ROUTE_NAME)),
                        c.getInt(c.getColumnIndex(RouteEntry.COLUMN_NAME_COUNT)),
                        c.getLong(c.getColumnIndex(RouteEntry.COLUMN_NAME_LAST_DATE)),
                        c.getInt(c.getColumnIndex(RouteEntry.COLUMN_NAME_TIME)),
                        c.getFloat(c.getColumnIndex(RouteEntry.COLUMN_NAME_DISTANCE)),
                        c.getString(c.getColumnIndex(RouteEntry.COLUMN_NAME_FILENAME)));

                routes.add(route);
            } while (c.moveToNext());
        }

        c.close();

        return routes;
    }

    long getRouteId(long runId, long defaultId) {
        SQLiteDatabase db = this.getReadableDatabase();

        String selectQuery = "SELECT " + RunRouteEntry.COLUMN_NAME_ROUTE_ID +
                " FROM " + RunRouteEntry.TABLE_NAME +
                " WHERE " + RunRouteEntry.COLUMN_NAME_RUN_ID + " = " + runId;

        Cursor c = db.rawQuery(selectQuery, null);

        long routeId = defaultId;
        if (c != null) {
            if (c.moveToFirst()) {
                routeId = c.getLong(0);
            }
            c.close();
        }

        return routeId;
    }

    public void tempClear() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL(SQL_DELETE_TABLE_RUN_ROUTE);
        db.execSQL(SQL_DELETE_TABLE_ROUTE);

        db.execSQL(SQL_CREATE_TABLE_ROUTE);
        db.execSQL(SQL_CREATE_TABLE_RUN_ROUTE);
    }
}
