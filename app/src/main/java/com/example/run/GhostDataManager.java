package com.example.run;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Qson on 6/14/2017.
 */

class GhostDataManager extends DataManager {
    private int totalTime;

    private int curTime;
    private int curDataPointIdx;
    private LatLng curLatLng;
    private float curDist;

    GhostDataManager(Context context) {
        super(context);
    }

    boolean loadFromFile(String filename) {
        if (!super.loadFromFile(filename)) {
            return false;
        }

        totalTime = dataPointList.get(dataPointList.size() - 1).timeSec;

        setRunTime(0);

        return true;
    }

    void reset() {
        curDataPointIdx = 0;
        setRunTime(0);
    }

    void setRunTime(int timeSec) {
        curTime = timeSec;
        if (curTime >= totalTime) {
            // TODO ghost done
            return;
        }

        while (curTime > dataPointList.get(curDataPointIdx).timeSec) {
            curDataPointIdx++;
        }

        if (curTime == dataPointList.get(curDataPointIdx).timeSec) {
            curLatLng = new LatLng(
                    dataPointList.get(curDataPointIdx).latitude,
                    dataPointList.get(curDataPointIdx).longitude);
            curDist = dataPointList.get(curDataPointIdx).distance;
        } else {
            // TODO interpolate ghost info
        }
    }

    LatLng getLatLng() {
        return curLatLng;
    }

    float getDistance() {
        return curDist;
    }
}
