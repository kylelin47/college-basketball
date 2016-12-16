package io.coachapps.collegebasketballcoach.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import io.coachapps.collegebasketballcoach.R;

/**
 * List adapter for displaying national and conference champions.
 * Created by jojones on 12/15/16.
 */

public class ChampionsListArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final List<String> values;

    public ChampionsListArrayAdapter(Context context, List<String> values) {
        super(context, R.layout.champions_list_item, values);
        this.context = context;
        this.values = values;
        }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(
            getContext()).inflate(R.layout.champions_list_item, parent, false);
        }

        TextView textViewTop = (TextView) convertView.findViewById(R.id.textViewTop);
        TextView textViewBottom = (TextView) convertView.findViewById(R.id.textViewBottom);

        textViewTop.setText(values.get(position).split(",")[0]);
        textViewBottom.setText(values.get(position).split(",")[1]);

        return convertView;
        }
}