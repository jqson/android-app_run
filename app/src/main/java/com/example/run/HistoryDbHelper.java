package com.example.run;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.run.HistoryReaderContract.HistoryEntry;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Qson on 6/23/2017.
 */

public class HistoryDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "RunHistory.db";


    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + HistoryEntry.TABLE_NAME + " (" +
                    HistoryEntry._ID + " INTEGER PRIMARY KEY," +
                    HistoryEntry.COLUMN_NAME_DATE + " INTEGER," +
                    HistoryEntry.COLUMN_NAME_TIME + " INTEGER," +
                    HistoryEntry.COLUMN_NAME_DISTANCE + " REAL," +
                    HistoryEntry.COLUMN_NAME_FILENAME + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + HistoryEntry.TABLE_NAME;

    public HistoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public long createHistory(RunHistory history) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(HistoryEntry.COLUMN_NAME_DATE, history.getLongDate());
        values.put(HistoryEntry.COLUMN_NAME_TIME, history.getTime());
        values.put(HistoryEntry.COLUMN_NAME_DISTANCE, history.getDistance());
        values.put(HistoryEntry.COLUMN_NAME_FILENAME, history.getFilename());

        return db.insert(HistoryEntry.TABLE_NAME, null, values);
    }

    public List<RunHistory> getAllHistory() {
        List<RunHistory> histories = new ArrayList<RunHistory>();
        String selectQuery = "SELECT * FROM " + HistoryEntry.TABLE_NAME;

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

        return histories;
    }
}
