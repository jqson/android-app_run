package com.example.run;

import android.content.Context;
import android.location.Location;

/**
 * Created by Qson on 6/14/2017.
 */

class RunnerDataManager extends DataManager {
    // speed m/s less than this will be treat as GPS error
    private static final float SPEED_THRESHOLD = 0.2f;

    private Location preLocation;

    RunnerDataManager(Context context) {
        super(context);
    }

    // return speed in m/s
    float addPoint(int timeSec, Location location) {
        float speed = 0.0f;
        if (dataPointList.size() == 0) {
            DataPoint newData = new DataPoint(
                    timeSec,
                    location.getLatitude(),
                    location.getLongitude(),
                    0.0f,
                    0.0f);
            dataPointList.add(newData);
            preLocation = location;
        } else if (location == preLocation) {
            speed = dataPointList.get(dataPointList.size() - 1).speed;
        } else {
            float distToPre = location.distanceTo(preLocation);
            int timeToPre = timeSec - dataPointList.get(dataPointList.size() - 1).timeSec;
            speed = distToPre / timeToPre;
            if (speed > SPEED_THRESHOLD) {
                DataPoint newData = new DataPoint(
                        timeSec,
                        location.getLatitude(),
                        location.getLongitude(),
                        speed,
                        dataPointList.get(dataPointList.size() - 1).distance + distToPre);
                dataPointList.add(newData);
                preLocation = location;
            }
        }

        return speed;
    }

    int getTime() {
        if (dataPointList.size() == 0) {
            return -1;
        } else {
            return dataPointList.get(dataPointList.size() - 1).timeSec;
        }
    }

    float getDistance() {
        if (dataPointList.size() == 0) {
            return 0.0f;
        } else {
            return dataPointList.get(dataPointList.size() - 1).distance;
        }
    }
}
