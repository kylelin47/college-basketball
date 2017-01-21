package io.coachapps.collegebasketballcoach.adapters.game;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import io.coachapps.collegebasketballcoach.R;
import io.coachapps.collegebasketballcoach.models.GameModel;
import io.coachapps.collegebasketballcoach.util.DataDisplayer;

/**
 * Adapter for displaying the past matchups of two teams.
 * Created by jojones on 1/20/17.
 */

public class PastMatchupsListArrayAdapter extends ArrayAdapter<GameModel> {
    private final Context context;
    private final List<GameModel> games;

    public PastMatchupsListArrayAdapter(Context context, List<GameModel> games) {
        super(context, R.layout.matchup_list_item, games);
        this.context = context;
        this.games = games;
    }

    private static class ViewHolder {
        TextView gameTitle;
        TextView leftAbbr;
        TextView leftScore;
        TextView rightAbbr;
        TextView rightScore;
        TextView textVSorAt;
    }

    @Override
    @NonNull
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        ViewHolder viewHolder;
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.matchup_list_item, parent, false);
            viewHolder = new ViewHolder();
            viewHolder.gameTitle = (TextView) convertView.findViewById(R.id.gameTitle);
            viewHolder.leftAbbr = (TextView) convertView.findViewById(R.id.gameAbbrLeft);
            viewHolder.leftScore = (TextView) convertView.findViewById(R.id.gameScoreLeft);
            viewHolder.rightAbbr = (TextView) convertView.findViewById(R.id.gameAbbrRight);
            viewHolder.rightScore = (TextView) convertView.findViewById(R.id.gameScoreRight);
            viewHolder.textVSorAt = (TextView) convertView.findViewById(R.id.gameScoreDash);
            convertView.setTag(viewHolder);
        } else {
            viewHolder = (ViewHolder) convertView.getTag();
        }

        if (games.get(position) != null) {
            GameModel game = games.get(position);

            viewHolder.gameTitle.setText(DataDisplayer.getGameTitle(game));

            String leftAbbr;
            int leftScore;
            String vsOrAt;
            String rightAbbr;
            int rightScore;

            if (game.homeTeam.compareTo(game.awayTeam) < 0) {
                leftAbbr = game.homeTeam.substring(0, Math.min(3, game.homeTeam.length())).toUpperCase();
                rightAbbr = game.awayTeam.substring(0, Math.min(3, game.homeTeam.length())).toUpperCase();
                vsOrAt = "vs";
                leftScore = game.homeStats.stats.points;
                rightScore = game.awayStats.stats.points;
            } else {
                leftAbbr = game.awayTeam.substring(0, Math.min(3, game.homeTeam.length())).toUpperCase();
                rightAbbr = game.homeTeam.substring(0, Math.min(3, game.homeTeam.length())).toUpperCase();
                vsOrAt = "@";
                leftScore = game.awayStats.stats.points;
                rightScore = game.homeStats.stats.points;
            }

            if (leftScore > rightScore) {
                viewHolder.leftAbbr.setTextColor(Color.parseColor("#DD5600"));
                viewHolder.rightAbbr.setTextColor(Color.parseColor("#555555"));
            } else {
                viewHolder.rightAbbr.setTextColor(Color.parseColor("#DD5600"));
                viewHolder.leftAbbr.setTextColor(Color.parseColor("#555555"));
            }

            viewHolder.leftAbbr.setText(leftAbbr);
            viewHolder.leftScore.setText(String.valueOf(leftScore));
            viewHolder.rightAbbr.setText(rightAbbr);
            viewHolder.rightScore.setText(String.valueOf(rightScore));
            viewHolder.textVSorAt.setText(vsOrAt);

        } else {
            // Do a comparison of wins of the last X games
            String title;
            String leftAbbr;
            String vsOrAt = " ";
            String rightAbbr;
            int leftScore;
            int rightScore;

            GameModel game = games.get(games.size()-1);

            if (game.homeTeam.compareTo(game.awayTeam) < 0) {
                leftAbbr = game.homeTeam.substring(0, Math.min(3, game.homeTeam.length())).toUpperCase();
                rightAbbr = game.awayTeam.substring(0, Math.min(3, game.homeTeam.length())).toUpperCase();
                leftScore = countWins(game.homeTeam);
                rightScore = games.size() - leftScore - 1;
            } else {
                leftAbbr = game.awayTeam.substring(0, Math.min(3, game.homeTeam.length())).toUpperCase();
                rightAbbr = game.homeTeam.substring(0, Math.min(3, game.homeTeam.length())).toUpperCase();
                leftScore = countWins(game.awayTeam);
                rightScore = games.size() - leftScore - 1;
            }

            title = "Record over last " + (games.size()-1) + " matchups";

            viewHolder.leftAbbr.setTextColor(Color.parseColor("#347378"));
            viewHolder.rightAbbr.setTextColor(Color.parseColor("#347378"));

            viewHolder.gameTitle.setText(title);
            viewHolder.leftAbbr.setText(leftAbbr);
            viewHolder.leftScore.setText(String.valueOf(leftScore));
            viewHolder.rightAbbr.setText(rightAbbr);
            viewHolder.rightScore.setText(String.valueOf(rightScore));
            viewHolder.textVSorAt.setText(vsOrAt);
        }

        return convertView;
    }

    private int countWins(String teamName) {
        int wins = 0;
        for (int i = 1; i < games.size(); ++i) {
            GameModel game = games.get(i);
            if (game.homeTeam.equals(teamName) &&
                    game.homeStats.stats.points > game.awayStats.stats.points) {
                wins++;
            } else if (game.awayTeam.equals(teamName) &&
                    game.homeStats.stats.points < game.awayStats.stats.points) {
                wins++;
            }
        }
        return wins;
    }
}
