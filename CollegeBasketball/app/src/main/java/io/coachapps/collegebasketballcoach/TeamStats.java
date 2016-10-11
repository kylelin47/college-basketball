package io.coachapps.collegebasketballcoach;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;

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

public class TeamStats extends AppCompatActivity {
    private static final String TAG = "MyActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_stats);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        LineChart chart = (LineChart) findViewById(R.id.chart);
        chart.setDrawGridBackground(false);
        chart.setTouchEnabled(true);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        List<Entry> entries = new ArrayList<>();
        YearlyPlayerStatsDao yearlyPlayerStatsDao = new YearlyPlayerStatsDao(this);
        List<YearlyPlayerStats> stats = yearlyPlayerStatsDao.getPlayerStatsFromYears(0, 2000, 2005);
        Log.i(TAG, "Size : " + stats.size());
        Log.i(TAG, "Points : " + stats.get(0).playerStats.points);
        Log.i(TAG, "Games Played : " + stats.get(0).gamesPlayed);

        for (YearlyPlayerStats data : stats) {
            entries.add(new Entry(data.year, data.getPPG()));
        }
        LineDataSet dataSet = new LineDataSet(entries, "PPG");
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
