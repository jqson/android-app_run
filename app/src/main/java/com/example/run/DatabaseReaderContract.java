package com.example.run;

import android.provider.BaseColumns;

/**
 * Created by Qson on 6/23/2017.
 */

public class DatabaseReaderContract {

    // To prevent someone from accidentally instantiating the contract class,
    // make the constructor private.
    private DatabaseReaderContract() {}

    /* Inner class that defines the table contents */
    public static class HistoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "history";
        public static final String COLUMN_NAME_DATE = "dateMs";
        public static final String COLUMN_NAME_TIME = "time";
        public static final String COLUMN_NAME_DISTANCE = "distance";
        public static final String COLUMN_NAME_FILENAME = "filename";
    }

    public static class RouteEntry implements BaseColumns {
        public static final String TABLE_NAME = "route";
        public static final String COLUMN_NAME_ROUTE_NAME = "name";
        public static final String COLUMN_NAME_COUNT = "count";
        public static final String COLUMN_NAME_LAST_DATE = "lastDateMs";
        public static final String COLUMN_NAME_TIME = "time";
        public static final String COLUMN_NAME_DISTANCE = "distance";
        public static final String COLUMN_NAME_FILENAME = "filename";
    }

    public static class RunRouteEntry implements BaseColumns {
        public static final String TABLE_NAME = "run_route";
        public static final String COLUMN_NAME_RUN_ID = "run_id";
        public static final String COLUMN_NAME_ROUTE_ID = "route_id";
    }
}
