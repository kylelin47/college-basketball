package io.coachapps.collegebasketballcoach.adapters.game;

import android.content.Context;
import android.graphics.Color;
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
        String[] gameSummary = team.getGameSummaryStr(games.get(position));
        if (convertView == null || games.get(position).gameType.isTournament()) {
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
        } else {
            viewHolder.viewGame.setBackground(ContextCompat.getDrawable(context, R.drawable
                    .button_shape_neutral));
        }

        if (games.get(position).gameType.isTournament()) {
            if (games.get(position).gameType == Game.GameType.TOURNAMENT_GAME) {
                viewHolder.gameType.setTextColor(Color.parseColor("#347378"));
            } else if (games.get(position).gameType == Game.GameType.MARCH_MADNESS) {
                viewHolder.gameType.setTextColor(Color.parseColor("#DD5600"));
            }
            viewHolder.gameType.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mainAct.showBracketDialog(games.get(position).gameType);
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

    @Override
    public Game getItem(int position) {
        return games.get(position);
    }

    @Override
    public int getCount() {
        return games.size();
    }
}
