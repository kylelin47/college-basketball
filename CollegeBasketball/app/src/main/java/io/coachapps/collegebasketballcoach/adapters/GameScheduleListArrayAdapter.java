package io.coachapps.collegebasketballcoach.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
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

    private static class ViewHolder {
        TextView gameType;
        Button viewGame;
        Button viewOpponent;
    }

    @Override
    @NonNull
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        String[] gameSummary = team.getGameSummaryStr(position);
        if (convertView == null || gameSummary[0].equals("Tournament")) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.game_schedule_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.gameType = (TextView) convertView.findViewById(R.id.gameScheduleLeft);
            viewHolder.viewGame = (Button) convertView.findViewById(R.id.gameScheduleButtonList);
            viewHolder.viewOpponent = (Button) convertView.findViewById(R.id.gameScheduleRight);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }


        viewHolder.gameType.setText( gameSummary[0] );
        viewHolder.viewGame.setText( gameSummary[1] );
        viewHolder.viewOpponent.setText( gameSummary[2] );

        if (games.get(position).hasPlayed()) {
            if (team == games.get(position).getWinner()) {
                viewHolder.viewGame.setBackground(ContextCompat.getDrawable(context, R.drawable
                        .button_shape_win));
            } else {
                viewHolder.viewGame.setBackground(ContextCompat.getDrawable(context, R.drawable
                        .button_shape_loss));
            }
        }
        if (gameSummary[0].equals("Tournament")) {
            viewHolder.gameType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mainAct.showBracketDialog();
                }
            });
        }
        viewHolder.viewGame.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Examine game summary
                Game gm = getItem(position);
                if (gm.hasPlayed()) {
                    mainAct.showGameSummaryDialog(gm);
                }
            }
        });

        viewHolder.viewOpponent.setOnClickListener(new View.OnClickListener() {
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

        return convertView;
    }

    public Game getItem(int position) {
        return games.get(position);
    }
}
