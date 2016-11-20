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
 * Adapter for displaying lists of strengths and weaknesses (used in recruiting)
 * Created by jojones on 11/13/16.
 */

public class StrengthWeaknessListArrayAdapter extends ArrayAdapter<String> {
    private Context context;
    private List<String> values;
    private boolean isStrength;

    public StrengthWeaknessListArrayAdapter(Context context, List<String> values, boolean isStrength) {
        super(context, R.layout.strength_weakness_list_item, values);
        this.context = context;
        this.values = values;
        this.isStrength = isStrength;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(
                    getContext()).inflate(R.layout.strength_weakness_list_item, parent, false);
        }

        TextView textViewPlusMinus = (TextView) convertView.findViewById(R.id.textViewPlusMinus);
        TextView textViewDescription = (TextView) convertView.findViewById(R.id.textViewDescription);

        if (isStrength) {
            textViewPlusMinus.setText("+");
        } else {
            textViewPlusMinus.setText("-");
        }

        textViewDescription.setText(values.get(position));

        return convertView;
    }
}
