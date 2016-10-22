package io.coachapps.collegebasketballcoach;

import android.app.Fragment;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
        YearlyPlayerStats latestStats = stats.size() == 0 ? new YearlyPlayerStats(playerId) : stats
                .get(stats.size() - 1);
        setStatValues(view, latestStats);
        view.findViewById(R.id.ppgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChart("PPG", "PPG", inflater, stats);
            }
        });
        view.findViewById(R.id.apgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChart("APG", "APG", inflater, stats);
            }
        });
        view.findViewById(R.id.rpgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChart("RPG", "RPG", inflater, stats);
            }
        });
        view.findViewById(R.id.mpgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChart("MPG", "MPG", inflater, stats);
            }
        });
        view.findViewById(R.id.spgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChart("SPG", "SPG", inflater, stats);
            }
        });
        view.findViewById(R.id.tpgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChart("TPG", "TPG", inflater, stats);
            }
        });
        view.findViewById(R.id.bpgButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChart("BPG", "BPG", inflater, stats);
            }
        });
        view.findViewById(R.id.fgaButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChart("FGA", "FGA", inflater, stats);
            }
        });
        view.findViewById(R.id.fgmButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChart("FGM", "FGM", inflater, stats);
            }
        });
        view.findViewById(R.id.fgPercentButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChart("FG%", "FG%", inflater, stats);
            }
        });
        view.findViewById(R.id.threePAButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChart("3PA", "3PA", inflater, stats);
            }
        });
        view.findViewById(R.id.threePMButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChart("3PM", "3PM", inflater, stats);
            }
        });
        view.findViewById(R.id.threePercentButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChart("3P%", "3P%", inflater, stats);
            }
        });
        view.findViewById(R.id.ftaButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChart("FTA", "FTA", inflater, stats);
            }
        });
        view.findViewById(R.id.ftmButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChart("FTM", "FTM", inflater, stats);
            }
        });
        view.findViewById(R.id.ftPercentButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showChart("FT%", "FT%", inflater, stats);
            }
        });
    }
    private void showChart(String label, String abbreviation, LayoutInflater inflater,
                           List<YearlyPlayerStats> stats) {
        AlertDialog dialog = createDialog(inflater);
        dialog.show();
        List<Entry> entries = new ArrayList<>();
        for (YearlyPlayerStats data : stats) {
            entries.add(new Entry(data.year, data.getPG(abbreviation)));
        }
        LineChart chart = (LineChart) dialog.findViewById(R.id.chart);
        createChart(chart, entries, label);
    }
    private void setStatValues(View view, YearlyPlayerStats latestStats) {
        ((TextView) view.findViewById(R.id.ppg)).setText(latestStats.getPGDisplay("PPG"));
        ((TextView) view.findViewById(R.id.apg)).setText(latestStats.getPGDisplay("APG"));
        ((TextView) view.findViewById(R.id.rpg)).setText(latestStats.getPGDisplay("RPG"));
        ((TextView) view.findViewById(R.id.mpg)).setText(latestStats.getPGDisplay("MPG"));
        ((TextView) view.findViewById(R.id.spg)).setText(latestStats.getPGDisplay("SPG"));
        ((TextView) view.findViewById(R.id.tpg)).setText(latestStats.getPGDisplay("TPG"));
        ((TextView) view.findViewById(R.id.bpg)).setText(latestStats.getPGDisplay("BPG"));
        ((TextView) view.findViewById(R.id.fga)).setText(latestStats.getPGDisplay("FGA"));
        ((TextView) view.findViewById(R.id.fgm)).setText(latestStats.getPGDisplay("FGM"));
        ((TextView) view.findViewById(R.id.fgPercent)).setText(latestStats.getPGDisplay("FG%"));
        ((TextView) view.findViewById(R.id.threePA)).setText(latestStats.getPGDisplay("3PA"));
        ((TextView) view.findViewById(R.id.threePM)).setText(latestStats.getPGDisplay("3PM"));
        ((TextView) view.findViewById(R.id.threePercent)).setText(latestStats.getPGDisplay("3P%"));
        ((TextView) view.findViewById(R.id.fta)).setText(latestStats.getPGDisplay("FTA"));
        ((TextView) view.findViewById(R.id.ftm)).setText(latestStats.getPGDisplay("FTM"));
        ((TextView) view.findViewById(R.id.ftPercent)).setText(latestStats.getPGDisplay("FT%"));
    }
    private AlertDialog createDialog(LayoutInflater inflater) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setPositiveButton("OK",null);
        builder.setView(inflater.inflate(R.layout.chart, null));
        return builder.create();
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
        dataSet.setValueTextSize(14);
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
