package com.example.run;

import android.content.Context;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Qson on 6/26/2017.
 */

class HistoryAdapter extends ArrayAdapter<RunHistory> {
    public HistoryAdapter(Context context, List<RunHistory> historyList) {
        super(context, 0, historyList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        RunHistory history = getItem(position);
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.history_list_item, parent, false);
        }
        // Lookup view for data population
        TextView runDate = (TextView) convertView.findViewById(R.id.history_date);
        TextView runTime = (TextView) convertView.findViewById(R.id.history_time);
        TextView runDistance = (TextView) convertView.findViewById(R.id.history_distance);
        TextView runSpeed = (TextView) convertView.findViewById(R.id.history_speed);
        // Populate the data into the template view using the data object
        runDate.setText(Util.dateToString(history.getDate()));
        runTime.setText(Util.secToTimeString(history.getTime()));
        runDistance.setText(Util.meterToString(history.getDistance()));
        runSpeed.setText(Util.speedToString(history.getDistance(), history.getTime()));
        // Return the completed view to render on screen
        return convertView;
    }
}
