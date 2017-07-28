package com.example.run;

import android.content.Context;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by Qson on 6/14/2017.
 */

class GhostDataManager extends DataManager {

    private int totalTime;
    private float totalDistance;

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
        totalDistance = dataPointList.get(dataPointList.size() - 1).distance;

        setRunTime(0);

        return true;
    }

    void reset() {
        curDataPointIdx = 0;
        setRunTime(0);
    }

    void setRunTime(int timeSec) {
        if (timeSec >= totalTime) {
            // TODO ghost done
            return;
        }

        // only work with incremental time
        while (timeSec > dataPointList.get(curDataPointIdx).timeSec) {
            curDataPointIdx++;
        }

        if (timeSec == dataPointList.get(curDataPointIdx).timeSec) {
            curLatLng = new LatLng(
                    dataPointList.get(curDataPointIdx).latitude,
                    dataPointList.get(curDataPointIdx).longitude);
            curDist = dataPointList.get(curDataPointIdx).distance;
        } else {
            int timeToPre = timeSec - curTime;
            int timeToNext = dataPointList.get(curDataPointIdx).timeSec - timeSec;

            double newLat = (curLatLng.latitude * timeToNext
                    + dataPointList.get(curDataPointIdx).latitude * timeToPre)
                    /(double)(timeToPre + timeToNext);
            double newLng = (curLatLng.longitude * timeToNext
                    + dataPointList.get(curDataPointIdx).longitude * timeToPre)
                    /(double)(timeToPre + timeToNext);

            curLatLng = new LatLng(newLat, newLng);
            curDist = (curDist * timeToNext
                    + dataPointList.get(curDataPointIdx).distance * timeToPre)
                    /(float) (timeToPre + timeToNext);
        }

        curTime = timeSec;
    }

    LatLng getLatLng() {
        return curLatLng;
    }

    float getDistance() {
        return curDist;
    }

    int getTotalTime() {
        return totalTime;
    }

    float getTotalDistance() {
        return totalDistance;
    }

    float getAvgSpeed() {
        return totalDistance / totalTime;
    }
}
