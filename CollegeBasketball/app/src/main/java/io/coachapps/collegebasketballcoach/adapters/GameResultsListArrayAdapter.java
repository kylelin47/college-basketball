package io.coachapps.collegebasketballcoach.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import java.util.List;

import io.coachapps.collegebasketballcoach.MainActivity;
import io.coachapps.collegebasketballcoach.R;
import io.coachapps.collegebasketballcoach.basketballsim.Game;
import io.coachapps.collegebasketballcoach.basketballsim.Team;

/**
 * Adapter to show the results of games.
 * Created by jojones on 10/24/16.
 */

public class GameResultsListArrayAdapter extends ArrayAdapter<Game> {
    private final Context context;
    private final List<Game> games;
    private final Team team;
    private final MainActivity mainAct;

    public GameResultsListArrayAdapter(Context context, MainActivity mainAct, Team team, List<Game> games) {
        super(context, R.layout.game_result_list_item, games);
        this.context = context;
        this.mainAct = mainAct;
        this.games = games;
        this.team = team;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.game_result_list_item, parent, false);

        TextView textHomeName = (TextView) rowView.findViewById(R.id.textViewHomeTeamName);
        TextView textHomeWL = (TextView) rowView.findViewById(R.id.textViewHomeTeamWL);
        TextView textHomeScore = (TextView) rowView.findViewById(R.id.textViewHomeTeamScore);

        TextView textAwayName = (TextView) rowView.findViewById(R.id.textViewAwayTeamName);
        TextView textAwayWL = (TextView) rowView.findViewById(R.id.textViewAwayTeamWL);
        TextView textAwayScore = (TextView) rowView.findViewById(R.id.textViewAwayTeamScore);

        Game gm = games.get(position);

        textHomeName.setText(gm.getHome().name);
        textHomeWL.setText(gm.getHome().wins + "-" + gm.getHome().losses);
        textHomeScore.setText(gm.getHomeScore());

        textAwayName.setText(gm.getAway().name);
        textAwayWL.setText(gm.getAway().wins + "-" + gm.getAway().losses);
        textAwayScore.setText(gm.getAwayScore());

        return rowView;
    }
}

