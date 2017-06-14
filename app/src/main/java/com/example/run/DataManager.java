package com.example.run;

import android.content.Context;
import android.location.Location;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Qson on 6/13/2017.
 */

class DataManager {
    // speed m/s less than this will be treat as GPS error
    private static final float SPEED_THRESHOLD = 0.2f;

    private class DataPoint {
        int timeSec;
        double latitude;
        double longitude;
        float speed;
        float distance;

        private DataPoint(int time, double lat, double lng, float speed, float distance) {
            this.timeSec = time;
            this.latitude = lat;
            this.longitude = lng;
            this.speed = speed;
            this.distance = distance;
        }


    }

    private List<DataPoint> dataPointList;
    private Location preLocation;

    private Context context;

    public DataManager(Context context) {
        this.context = context;
        dataPointList = new ArrayList<>();
    }

    // return speed in m/s
    public float addPoint(int timeSec, Location location) {
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

    public float getDistance() {
        if (dataPointList.size() == 0) {
            return 0.0f;
        } else {
            return dataPointList.get(dataPointList.size() - 1).distance;
        }
    }

    public List<LatLng> getLatLngList() {
        List<LatLng> latLngList = new ArrayList<>();
        for (DataPoint dp : dataPointList) {
            latLngList.add(new LatLng(dp.latitude, dp.longitude));
        }
        return latLngList;
    }

    public boolean loadFromFile(String filename) {
        String dataString = readFile(filename, context);
        if (dataString == null || dataString.length() == 0) {
            return false;
        }
        String[] dataArray = dataString.split("\n");
        for (int i = 1; i < dataArray.length; i++) {
            String[] lineData = dataArray[i].split(",");
            DataPoint newData = new DataPoint(
                    Integer.parseInt(lineData[0]),
                    Double.parseDouble(lineData[1]),
                    Double.parseDouble(lineData[2]),
                    Float.parseFloat(lineData[3]),
                    Float.parseFloat(lineData[4]));
            dataPointList.add(newData);
        }
        return true;
    }

    public void saveToFile(String filename) {
        StringBuilder fileDataBuilder = new StringBuilder();
        fileDataBuilder.append("time,lat,lng,incDist,totalDist\n");

        for (DataPoint dp : dataPointList) {
            List<String> runData = new ArrayList<>();
            runData.add(String.valueOf(dp.timeSec));
            runData.add(String.valueOf(dp.latitude));
            runData.add(String.valueOf(dp.longitude));
            runData.add(String.valueOf(dp.speed));
            runData.add(String.valueOf(dp.distance));
            fileDataBuilder.append(TextUtils.join(",", runData));
            fileDataBuilder.append("\n");
        }

        writeFile(filename, fileDataBuilder.toString(), context);
    }

    public static void writeFile(String filename, String data, Context context) {
        try {
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(context.openFileOutput(filename, Context.MODE_PRIVATE));
            outputStreamWriter.write(data);
            outputStreamWriter.close();
        }
        catch (IOException e) {
            Log.e("Exception", "File write failed: " + e.toString());
        }
    }

    public static String readFile(String filename, Context context) {
        String fileData = "";

        try {
            InputStream inputStream = context.openFileInput(filename);

            if ( inputStream != null ) {
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String receiveString = "";
                StringBuilder stringBuilder = new StringBuilder();

                while ( (receiveString = bufferedReader.readLine()) != null ) {
                    stringBuilder.append(receiveString);
                    stringBuilder.append("\n");
                }

                inputStream.close();
                fileData = stringBuilder.toString();
            }
        }
        catch (FileNotFoundException e) {
            Log.e("login activity", "File not found: " + e.toString());
        } catch (IOException e) {
            Log.e("login activity", "Can not read file: " + e.toString());
        }

        return fileData;
    }
}
