package io.coachapps.collegebasketballcoach.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

import io.coachapps.collegebasketballcoach.R;
import io.coachapps.collegebasketballcoach.basketballsim.Game;
import io.coachapps.collegebasketballcoach.basketballsim.Player;
import io.coachapps.collegebasketballcoach.db.GameDao;
import io.coachapps.collegebasketballcoach.db.PlayerDao;
import io.coachapps.collegebasketballcoach.models.BoxScore;
import io.coachapps.collegebasketballcoach.models.GameModel;
import io.coachapps.collegebasketballcoach.models.Stats;
import io.coachapps.collegebasketballcoach.util.DataDisplayer;

/**
 * Adapter for displaying a player's recent games.
 * Created by Achi Jones on 11/3/2016.
 */

public class RecentGamesListArrayAdapter extends ArrayAdapter<BoxScore> {

    private final Context context;
    private final List<BoxScore> playerBoxScores;
    private final GameDao gameDao;

    public RecentGamesListArrayAdapter(Context context, List<BoxScore> values) {
        super(context, R.layout.recent_games_list_item, values);
        this.context = context;
        this.playerBoxScores = values;
        this.gameDao = new GameDao(context);
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.recent_games_list_item, parent, false);

        String team = playerBoxScores.get(position).teamName;
        int year = playerBoxScores.get(position).year;
        int week = playerBoxScores.get(position).week;
        GameModel gameModel = gameDao.getGame(year, week, team);
        String gameSummary = getGameSummaryStr(team, gameModel);

        TextView textGameSummary = (TextView) rowView.findViewById(R.id.textViewGameSummary);
        TextView textWL = (TextView) rowView.findViewById(R.id.textViewWL);
        TextView textMinutes = (TextView) rowView.findViewById(R.id.textViewMinutes);

        TextView playerPTS  = (TextView) rowView.findViewById(R.id.textViewPTS);
        TextView playerREB  = (TextView) rowView.findViewById(R.id.textViewREB);
        TextView playerAST  = (TextView) rowView.findViewById(R.id.textViewAST);
        TextView playerFGMA = (TextView) rowView.findViewById(R.id.textViewFGMA);
        TextView player3GMA = (TextView) rowView.findViewById(R.id.textView3GMA);
        TextView playerBLK  = (TextView) rowView.findViewById(R.id.textViewBLK);
        TextView playerSTL  = (TextView) rowView.findViewById(R.id.textViewSTL);

        BoxScore boxScore = playerBoxScores.get(position);
        Stats gmStats = boxScore.playerStats;
        textGameSummary.setText(gameSummary.substring(1,gameSummary.length()));
        textWL.setText(gameSummary.substring(0,1));
        if (gameSummary.substring(0,1).equals("W")) {
            textWL.setTextColor(context.getResources().getColor(R.color.winColorPressed));
        } else {
            textWL.setTextColor(context.getResources().getColor(R.color.lossColorPressed));
        }
        textMinutes.setText(String.valueOf(gmStats.secondsPlayed/60) + "min");

        playerPTS.setText(String.valueOf(gmStats.points));
        playerREB.setText(String.valueOf(gmStats.defensiveRebounds+gmStats.offensiveRebounds));
        playerAST.setText(String.valueOf(gmStats.assists));
        playerFGMA.setText(String.valueOf(gmStats.fieldGoalsMade)+"/"+String.valueOf(gmStats.fieldGoalsAttempted));
        player3GMA.setText(String.valueOf(gmStats.threePointsMade)+"/"+String.valueOf(gmStats.threePointsAttempted));
        playerBLK.setText(String.valueOf(gmStats.blocks));
        playerSTL.setText(String.valueOf(gmStats.steals));

        return rowView;
    }

    public BoxScore getItem(int position) {
        return playerBoxScores.get(position);
    }

    private String getGameSummaryStr(String teamName, GameModel gameModel) {
        if (gameModel.awayTeam.equals(teamName)) {
            // Away
            if (gameModel.awayStats.stats.points > gameModel.homeStats.stats.points) {
                return "W " + gameModel.awayStats.stats.points + "-" + gameModel.homeStats.stats.points +
                        " @ " + gameModel.homeTeam;
            } else {
                return "L " + gameModel.awayStats.stats.points + "-" + gameModel.homeStats.stats.points +
                        " @ " + gameModel.homeTeam;
            }
        } else {
            // Home
            if (gameModel.homeStats.stats.points > gameModel.awayStats.stats.points) {
                return "W " + gameModel.homeStats.stats.points + "-" + gameModel.awayStats.stats.points +
                        " vs " + gameModel.awayTeam;
            } else {
                return "L " + gameModel.homeStats.stats.points + "-" + gameModel.awayStats.stats.points +
                        " vs " + gameModel.awayTeam;
            }
        }
    }

}