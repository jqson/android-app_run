package com.example.run;

import android.provider.BaseColumns;

/**
 * Created by Qson on 6/23/2017.
 */

public class HistoryReaderContract {
    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private HistoryReaderContract() {}

    /* Inner class that defines the table contents */
    public static class HistoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "history";
        public static final String COLUMN_NAME_DATE = "dateMs";
        public static final String COLUMN_NAME_TIME = "time";
        public static final String COLUMN_NAME_DISTANCE = "distance";
        public static final String COLUMN_NAME_FILENAME = "filename";
    }
}
