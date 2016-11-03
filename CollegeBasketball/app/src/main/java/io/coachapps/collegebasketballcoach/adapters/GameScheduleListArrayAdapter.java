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
 * Adapter for displaying the list of games
 * Created by Achi Jones on 10/22/2016.
 */

public class GameScheduleListArrayAdapter extends ArrayAdapter<Game> {
    private final Context context;
    private final List<Game> games;
    private final Team team;
    private final MainActivity mainAct;

    public GameScheduleListArrayAdapter(Context context, MainActivity mainAct, Team team, List<Game> games) {
        super(context, R.layout.game_schedule_list_item, games);
        this.context = context;
        this.mainAct = mainAct;
        this.games = games;
        this.team = team;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.game_schedule_list_item, parent, false);
        TextView textLeft = (TextView) rowView.findViewById(R.id.gameScheduleLeft);
        Button gameButton = (Button) rowView.findViewById(R.id.gameScheduleButtonList);
        Button textRight = (Button) rowView.findViewById(R.id.gameScheduleRight);

        String[] gameSummary = team.getGameSummaryStr(position);
        textLeft.setText( gameSummary[0] );
        gameButton.setText( gameSummary[1] );
        textRight.setText( gameSummary[2] );

        if (games.get(position).hasPlayed()) {
            if (team == games.get(position).getWinner()) {
                gameButton.setBackground(context.getResources().getDrawable(R.drawable.button_shape_win));
            } else {
                gameButton.setBackground(context.getResources().getDrawable(R.drawable.button_shape_loss));
            }
        }

        gameButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Examine game summary
                Game gm = getItem(position);
                if (gm.hasPlayed()) {
                    mainAct.showGameSummaryDialog(gm, position);
                }
            }
        });

        textRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Examine team
                Game gm = getItem(position);
                if (gm.getHome() == team) {
                    mainAct.examineTeam(gm.getAway().name);
                } else {
                    mainAct.examineTeam(gm.getHome().name);
                }
            }
        });

        return rowView;
    }

    public Game getItem(int position) {
        return games.get(position);
    }
}
