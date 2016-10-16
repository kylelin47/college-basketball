package io.coachapps.collegebasketballcoach;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.AxisValueFormatter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import io.coachapps.collegebasketballcoach.db.YearlyPlayerStatsDao;
import io.coachapps.collegebasketballcoach.models.YearlyPlayerStats;

public class PlayerStatsFragment extends Fragment {
    private static final String ID_KEY = "playerId";

    public static PlayerStatsFragment newInstance(int playerId) {
        PlayerStatsFragment fragment = new PlayerStatsFragment();
        Bundle args = new Bundle();
        args.putInt(ID_KEY, playerId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {
        View view = inflater.inflate(R.layout.stats_fragment, container, false);
        fillStats(getPlayerId(), view, inflater);
        return view;
    }

    private int getPlayerId() {
        return getArguments().getInt(ID_KEY);
    }

    private void fillStats(int playerId, View view, final LayoutInflater inflater) {
        YearlyPlayerStatsDao yearlyPlayerStatsDao = new YearlyPlayerStatsDao(getActivity());
        final List<YearlyPlayerStats> stats = yearlyPlayerStatsDao.getPlayerStatsFromYears(playerId, 0, 9999);
        view.findViewById(R.id.ppg).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setView(inflater.inflate(R.layout.chart, null));
                AlertDialog dialog = builder.create();
                List<Entry> entries = new ArrayList<>();
                for (YearlyPlayerStats data : stats) {
                    entries.add(new Entry(data.year, data.getPPG()));
                }
                dialog.show();
                LineChart chart = (LineChart) dialog.findViewById(R.id.chart);
                createChart(chart, entries, "PPG");
            }
        });

    }
    private void createChart(LineChart chart, List<Entry> entries, String name) {
        chart.setDrawGridBackground(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        LineDataSet dataSet = new LineDataSet(entries, name);
        dataSet.setLineWidth(1.75f);
        dataSet.setCircleRadius(4f);
        dataSet.setCircleHoleRadius(2.0f);
        dataSet.setColor(Color.BLACK);
        dataSet.setCircleColor(Color.GRAY);
        dataSet.setHighLightColor(Color.CYAN);
        dataSet.setValueTextSize(20);
        LineData lineData = new LineData(dataSet);
        chart.setData(lineData);
        chart.getXAxis().setValueFormatter(new MyXAxisValueFormatter());
        chart.getXAxis().setGranularity(1f);
        chart.getLegend().setYOffset(20);
        chart.setDescription("Player stats");
        chart.setDrawBorders(false);
        chart.invalidate();
    }
}
class MyXAxisValueFormatter implements AxisValueFormatter {

    private DecimalFormat mFormat;

    public MyXAxisValueFormatter() {
        mFormat = new DecimalFormat("#");
    }

    @Override
    public String getFormattedValue(float value, AxisBase axis) {
        return mFormat.format(value);
    }

    /** this is only needed if numbers are returned, else return 0 */
    @Override
    public int getDecimalDigits() { return 0; }
}
