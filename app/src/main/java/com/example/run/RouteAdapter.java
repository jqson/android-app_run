package com.example.run;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Qson on 7/20/2017.
 */

class RouteAdapter extends ArrayAdapter<Route> {

    RouteAdapter(Context context, List<Route> routeList) {
        super(context, 0, routeList);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        Route route = getItem(position);

        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.route_list_item, parent, false);
        }

        // Lookup view for data population
        TextView routeName = (TextView) convertView.findViewById(R.id.route_name);
        TextView runCount = (TextView) convertView.findViewById(R.id.run_count);
        TextView lastDate = (TextView) convertView.findViewById(R.id.last_date);
        TextView bestTime = (TextView) convertView.findViewById(R.id.best_time);
        TextView avgDistance = (TextView) convertView.findViewById(R.id.avg_distance);
        TextView bestSpeed = (TextView) convertView.findViewById(R.id.best_avg_speed);

        // Populate the data into the template view using the data object
        routeName.setText(route.getRouteName());
        runCount.setText(Util.runCountToString(route.getRunCount()));
        lastDate.setText(Util.dateToString(route.getLastRunDate()));
        bestTime.setText(Util.secToTimeString(route.getBestTime()));
        avgDistance.setText(Util.meterToString(route.getDistance()));
        bestSpeed.setText(Util.speedToString(route.getDistance(), route.getBestTime()));
        // Return the completed view to render on screen
        return convertView;
    }
}
