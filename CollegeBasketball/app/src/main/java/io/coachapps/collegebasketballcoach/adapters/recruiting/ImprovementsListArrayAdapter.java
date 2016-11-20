package io.coachapps.collegebasketballcoach.adapters.recruiting;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import io.coachapps.collegebasketballcoach.R;

/**
 * Adapter for displaying a list of players with how much they improved in the offseason.
 * Created by jojones on 11/15/16.
 */

public class ImprovementsListArrayAdapter extends ArrayAdapter<String> {
    private final Context context;
    private final List<String> values;

    public ImprovementsListArrayAdapter(Context context, List<String> values) {
        super(context, R.layout.improvements_list_item, values);
        this.context = context;
        this.values = values;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(
                    getContext()).inflate(R.layout.improvements_list_item, parent, false);
        }

        TextView textViewPlusMinus = (TextView) convertView.findViewById(R.id.textViewPlusMinus);
        TextView textViewDescription = (TextView) convertView.findViewById(R.id.textViewDescription);

        textViewPlusMinus.setText(values.get(position).split(",")[0]);
        textViewDescription.setText(values.get(position).split(",")[1]);

        return convertView;
    }
}
