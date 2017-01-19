package io.coachapps.collegebasketballcoach.adapters.player;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.List;

import io.coachapps.collegebasketballcoach.R;
import io.coachapps.collegebasketballcoach.basketballsim.Player;
import io.coachapps.collegebasketballcoach.basketballsim.PlayerGen;
import io.coachapps.collegebasketballcoach.db.PlayerDao;
import io.coachapps.collegebasketballcoach.models.PlayerModel;
import io.coachapps.collegebasketballcoach.util.DataDisplayer;

/**
 * List adpater for changing players usages.
 * Created by jojones on 1/18/17.
 */

public class SetUsageListArrayAdapter extends ArrayAdapter<Player> {
    private final Context context;
    public final List<Player> players;
    private final String playerTeamName;
    private final PlayerDao playerDao;

    private final double USAGE_SCALE = 0.2;

    public SetUsageListArrayAdapter(Context context, List<Player> values, String playerTeamName) {
        super(context, R.layout.set_usage_list_item, values);
        this.context = context;
        this.players = values;
        this.playerTeamName = playerTeamName;
        this.playerDao = new PlayerDao(context);
    }

    private static class ViewHolder {
        TextView playerName;
        TextView playerPosition;
        TextView playerOvrPot;
        TextView playerInside;
        TextView playerMidrange;
        TextView playerOutside;
        Spinner usageSpinner;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.set_usage_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.playerName = (TextView) convertView.findViewById(R.id.textViewName);
            viewHolder.playerPosition = (TextView) convertView.findViewById(R.id.textViewPosition);
            viewHolder.playerOvrPot = (TextView) convertView.findViewById(R.id.textViewOvrPot);
            viewHolder.playerInside = (TextView) convertView.findViewById(R.id.textViewInside);
            viewHolder.playerMidrange = (TextView) convertView.findViewById(R.id.textViewMidrange);
            viewHolder.playerOutside = (TextView) convertView.findViewById(R.id.textViewOutside);
            viewHolder.usageSpinner = (Spinner) convertView.findViewById(R.id.spinnerUsage);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        final Player p = players.get(position);
        viewHolder.playerName.setText(p.name + " [" + DataDisplayer.getYearAbbreviation(p.year) + "]");
        String benchOrStart = (position / 5) == 0 ? "Start " : "Bench ";
        if (position < 10) {
            String posStr = benchOrStart + DataDisplayer.getPositionAbbreviation(position % 5 + 1);
            viewHolder.playerPosition.setText(posStr);
        } else {
            viewHolder.playerPosition.setText("Won't Play");
        }
        viewHolder.playerOvrPot.setText(String.valueOf(p.getOverall()) + " / " +
                DataDisplayer.getLetterGrade(p.getPotential()));

        viewHolder.playerInside.setText(String.valueOf(p.getIntS()));
        viewHolder.playerMidrange.setText(String.valueOf(p.getMidS()));
        viewHolder.playerOutside.setText(String.valueOf(p.getOutS()));

        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(context,
                R.array.usage_choices, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        viewHolder.usageSpinner.setAdapter(adapter);

        int calcUsage = PlayerGen.usageCalc(p.ratings);
        int actualUsage = p.getUsage();
        double scaled = (double)(actualUsage) / calcUsage;
        int selection = 4 - ((int)((scaled + 0.1)/USAGE_SCALE) - 3);
        if (selection < 0) selection = 0;
        if (selection > 4) selection = 4;
        viewHolder.usageSpinner.setSelection(selection);
        viewHolder.usageSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        // Set usage
                        int newUsage = (int) (((1 - 2*USAGE_SCALE) + USAGE_SCALE*(4-position))
                                * PlayerGen.usageCalc(p.ratings));
                        Log.i("SetUsageLAA", p.name + ": old = " + p.ratings.usage + ", new = " + newUsage);
                        if (p.ratings.usage != newUsage) {
                            PlayerWithUsage pu = new PlayerWithUsage(p, newUsage);
                            new UpdateUsageTask().execute(pu);
                        }
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                        // do nothing
                    }
                });

        return convertView;
    }

    public Player getItem(int position) {
        return players.get(position);
    }

    private class PlayerWithUsage {
        Player player;
        int newUsage;

        public PlayerWithUsage(Player player, int newUsage) {
            this.player = player;
            this.newUsage = newUsage;
        }
    }

    /**
     * Class responsible for simulating a week.
     * Done via a AsyncTask so the UI thread isn't overwhelmed.
     */
    private class UpdateUsageTask extends AsyncTask<PlayerWithUsage, Void, Void> {
        @Override
        protected Void doInBackground(PlayerWithUsage... playerWithUsages) {
            Player p = playerWithUsages[0].player;
            p.ratings.usage = playerWithUsages[0].newUsage;
            PlayerModel pm = new PlayerModel(p, playerTeamName);
            playerDao.updatePlayer(pm);
            return null;
        }
    }
}
