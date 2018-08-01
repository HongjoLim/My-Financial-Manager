package com.hongjolim.mfmanager;


import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hongjolim.mfmanager.tools.DateFormatConverter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GridLabelRenderer;
import com.jjoe64.graphview.helper.StaticLabelsFormatter;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.util.Calendar;


/**
 * Name: HONGJO Lim
 * Date: Apr 8th, 2018
 * Purpose: This class shows the chart of monthly net earnings for the last 6 months.
 * It is called from MainActivity.
 */

public class MonthlyNetEarningsFragment extends DialogFragment {


    public MonthlyNetEarningsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_monthly_net_earnings, container, false);

        getActivity().getContentResolver();

        Calendar calendar = Calendar.getInstance();

        //double array for y values
        double[] netEarnings = getArguments().getDoubleArray("Y_VALUES");

        //string array for x labels
        String[] months = new String[6];

        //get 6 string values for months from now to 6 months ago
        for(int i = 0; i<6; i++){
            /**
             * get month strings from DateFormatConverter class and put them in the string array inversely
             * because this chart takes labels x axises from left to right
             */

            months[5-i] = DateFormatConverter.MONTH_STRINGS[calendar.get(Calendar.MONTH)];
            calendar.add(Calendar.MONTH, -1);
        }

        GraphView graphView = view.findViewById(R.id.netEarnings_graph_monthly);


        //initialize StaticLabelsFormatter class to set horizontal labels with string(months)
        StaticLabelsFormatter formatter = new StaticLabelsFormatter(graphView);
        formatter.setHorizontalLabels(months);

        graphView.getGridLabelRenderer().setLabelFormatter(formatter);

        LineGraphSeries<DataPoint> series = new LineGraphSeries<>(new DataPoint[] {
                new DataPoint(0, netEarnings[5]),
                new DataPoint(1, netEarnings[4]),
                new DataPoint(2, netEarnings[3]),
                new DataPoint(3, netEarnings[2]),
                new DataPoint(4, netEarnings[1]),
                new DataPoint(5, netEarnings[0]),
        });

        graphView.setTitleTextSize(20.0f);
        GridLabelRenderer renderer = graphView.getGridLabelRenderer();
        renderer.setTextSize(32.0f);

        //set thickness for the line
        series.setThickness(8);
        //draw points on data
        series.setDrawDataPoints(true);
        series.setDataPointsRadius(10.0f);
        graphView.addSeries(series);

        return view;
    }

}
