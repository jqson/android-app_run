package com.example.run;

import java.util.Date;

/**
 * Created by Qson on 6/23/2017.
 */

class RunHistory {

    int id;
    Date date;
    int time;
    float distance;
    String filename;

    public RunHistory() {

    }

    public RunHistory(int id, Date date, int time, float distance, String filename) {
        this.id = id;
        this.date = date;
        this.time = time;
        this.distance = distance;
        this.filename = filename;
    }

    public RunHistory(int id, long dateMs, int time, float distance, String filename) {
        this.id = id;
        this.date = new Date(dateMs);
        this.time = time;
        this.distance = distance;
        this.filename = filename;
    }

    public RunHistory(Date date, int time, float distance, String filename) {
        this.date = date;
        this.time = time;
        this.distance = distance;
        this.filename = filename;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public void setDate(long dateMs) {
        this.date = new Date(dateMs);
    }

    public void setDate(int dateMs) {
        this.date = new Date(dateMs);
    }

    public void setTime(int time) {
        this.time = time;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public int getId() {
        return id;
    }

    public Date getDate() {
        return date;
    }

    public long getLongDate() {
        return date.getTime();
    }

    public int getTime() {
        return time;
    }

    public float getDistance() {
        return distance;
    }

    public String getFilename() {
        return filename;
    }
}
