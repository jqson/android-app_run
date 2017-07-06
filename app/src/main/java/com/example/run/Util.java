package com.example.run;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by Qson on 6/26/2017.
 */

class Util {
    private static final String DATE_TIME_PATTERN = "EEE, d MMM yy h:mm a";
    private static final String METER_PATTERN = "#.#";

    static String secToTimeString(int second) {
        int sec = second % 60;
        int min = (second / 60) % 60;
        int hour = second / 3600;

        StringBuilder timeSB = new StringBuilder();
        if (hour != 0) {
            timeSB.append(String.valueOf(hour)).append(":");
        }
        timeSB.append(String.format("%02d", min)).append(":");
        timeSB.append(String.format("%02d", sec));

        return timeSB.toString();
    }

    static String dateToString(String dateInMsString) {
        return dateToString(Long.valueOf(dateInMsString));
    }

    static String dateToString(long dateInMs) {
        return dateToString(new Date(dateInMs));
    }

    static String dateToString(Date date) {
        // generate history filename list and text display list
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_TIME_PATTERN);

        return sdf.format(date).toString();
    }

    static String meterToString(float meter) {
        DecimalFormat df = new DecimalFormat(METER_PATTERN);
        return df.format(meter) + " m";
    }
}
