package com.example.run;

import java.util.Date;

/**
 * Created by Qson on 7/13/2017.
 */

class Route {

    long id;
    String routeName;
    int runCount;
    Date lastRunDate;
    int bestTime;
    float distance;
    String filename;

    public Route() {

    }

    public Route(long id, String name, int runCount,
                 long dateMs, int bestTime, float distance, String filename) {
        this.id = id;
        this.routeName = name;
        this.runCount = runCount;
        this.lastRunDate = new Date(dateMs);
        this.bestTime = bestTime;
        this.distance = distance;
        this.filename = filename;
    }

    public Route(String name, int runCount,
                 long dateMs, int bestTime, float distance, String filename) {
        this.routeName = name;
        this.runCount = runCount;
        this.lastRunDate = new Date(dateMs);
        this.bestTime = bestTime;
        this.distance = distance;
        this.filename = filename;
    }

    public Route(String name, int runCount,
                 Date lastRunDate, int bestTime, float distance, String filename) {
        this.routeName = name;
        this.runCount = runCount;
        this.lastRunDate = lastRunDate;
        this.bestTime = bestTime;
        this.distance = distance;
        this.filename = filename;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getRouteName() {
        return routeName;
    }

    public void setRouteName(String routeName) {
        this.routeName = routeName;
    }

    public int getRunCount() {
        return runCount;
    }

    public void setRunCount(int runCount) {
        this.runCount = runCount;
    }

    public Date getLastRunDate() {
        return lastRunDate;
    }

    public long getLastRunDateLong() {
        return lastRunDate.getTime();
    }

    public void setLastRunDate(Date lastRunDate) {
        this.lastRunDate = lastRunDate;
    }

    public int getBestTime() {
        return bestTime;
    }

    public void setBestTime(int bestTime) {
        this.bestTime = bestTime;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }
}
