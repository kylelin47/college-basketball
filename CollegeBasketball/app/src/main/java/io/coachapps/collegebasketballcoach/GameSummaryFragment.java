package io.coachapps.collegebasketballcoach;

import android.app.DialogFragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.coachapps.collegebasketballcoach.adapters.PlayerBoxScoreListArrayAdapter;
import io.coachapps.collegebasketballcoach.adapters.TeamStatsListArrayAdapter;
import io.coachapps.collegebasketballcoach.basketballsim.Player;
import io.coachapps.collegebasketballcoach.db.BoxScoreDao;
import io.coachapps.collegebasketballcoach.db.GameDao;
import io.coachapps.collegebasketballcoach.db.PlayerDao;
import io.coachapps.collegebasketballcoach.models.BoxScore;
import io.coachapps.collegebasketballcoach.models.GameModel;
import io.coachapps.collegebasketballcoach.util.DataDisplayer;

/**
 * Dialog Fragment for showing the summary of a certain game.
 * Created by Achi Jones on 10/25/2016.
 */

public class GameSummaryFragment extends DialogFragment {

    class BoxScoreIDComp implements Comparator<BoxScore> {
        @Override
        public int compare( BoxScore a, BoxScore b ) {
            return b.playerId - a.playerId;
        }
    }

    class BoxScoreComp implements Comparator<BoxScore> {
        @Override
        public int compare( BoxScore a, BoxScore b ) {
            return b.playerStats.points - a.playerStats.points;
        }
    }

    public class PlayerBoxScore {
        public BoxScore boxScore;
        public Player player;
    }

    private static final String YEAR_KEY = "year";
    private static final String WEEK_KEY = "week";
    private static final String HOME_KEY = "home";
    private static final String AWAY_KEY = "away";

    PlayerDao playerDao;
    private GameModel gameModel;
    private List<BoxScore> awayBoxScores;
    private List<BoxScore> homeBoxScores;

    public static GameSummaryFragment newInstance(int year, int week, String homeTeam, String awayTeam) {
        GameSummaryFragment fragment = new GameSummaryFragment();
        Bundle args = new Bundle();
        args.putInt(YEAR_KEY, year);
        args.putInt(WEEK_KEY, week);
        args.putString(HOME_KEY, homeTeam);
        args.putString(AWAY_KEY, awayTeam);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_FRAME, android.R.style.Theme_DeviceDefault_Light);
        int year = getArguments().getInt(YEAR_KEY);
        int week = getArguments().getInt(WEEK_KEY);
        String homeTeamName = getArguments().getString(HOME_KEY);
        String awayTeamName = getArguments().getString(AWAY_KEY);
        playerDao = new PlayerDao(getActivity());
        // Get GameModel
        GameDao gameDao = new GameDao(getActivity());
        gameModel = gameDao.getGame(year, week, homeTeamName, awayTeamName);

        try {
            BoxScoreDao boxScoreDao = new BoxScoreDao(getActivity());
            awayBoxScores = boxScoreDao.getBoxScoresFromGame(year, week,
                    awayTeamName);
            homeBoxScores = boxScoreDao.getBoxScoresFromGame(year, week,
                    homeTeamName);
            Collections.sort(awayBoxScores, new BoxScoreComp());
            Collections.sort(homeBoxScores, new BoxScoreComp());

        } catch (Exception e) {
            // TODO: how to handle this?
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.game_summary, container, false);

        TextView textHomeName = (TextView) view.findViewById(R.id.textViewHomeTeamName);
        TextView textHomeWL = (TextView) view.findViewById(R.id.textViewHomeTeamWL);
        TextView textHomeScore = (TextView) view.findViewById(R.id.textViewHomeTeamScore);

        TextView textAwayName = (TextView) view.findViewById(R.id.textViewAwayTeamName);
        TextView textAwayWL = (TextView) view.findViewById(R.id.textViewAwayTeamWL);
        TextView textAwayScore = (TextView) view.findViewById(R.id.textViewAwayTeamScore);

        textHomeName.setText(gameModel.homeTeam);
        textHomeWL.setText("");
        textHomeScore.setText(String.valueOf(gameModel.homeStats.points));

        textAwayName.setText(gameModel.awayTeam);
        textAwayWL.setText("");
        textAwayScore.setText(String.valueOf(gameModel.awayStats.points));

        final ListView listView = (ListView) view.findViewById(R.id.listViewGameSummary);
        Spinner spinner = (Spinner) view.findViewById(R.id.spinnerGameSummary);
        final List<String> list = new ArrayList<String>();
        list.add("Team Stats Comparison");
        list.add(gameModel.awayTeam + " Stats");
        list.add(gameModel.homeTeam + " Stats");
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_spinner_item, list);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (i == 0) {
                    // Team Stats
                    listView.setAdapter(getTeamStatsAdapter());
                } else if (i == 1) {
                    // Away Stats
                    listView.setAdapter(new PlayerBoxScoreListArrayAdapter(getActivity(), awayBoxScores));
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {
                            BoxScore bs = (BoxScore)listView.getItemAtPosition(position);
                            Player p = playerDao.getPlayer(bs.playerId);
                            MainActivity mainActivity = (MainActivity) getActivity();
                            mainActivity.showPlayerDialog(p);
                        }
                    });
                } else if (i == 2) {
                    // Home Stats
                    listView.setAdapter(new PlayerBoxScoreListArrayAdapter(getActivity(), homeBoxScores));
                    listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        public void onItemClick(AdapterView<?> parent, View view,
                                                int position, long id) {
                            BoxScore bs = (BoxScore)listView.getItemAtPosition(position);
                            Player p = playerDao.getPlayer(bs.playerId);
                            MainActivity mainActivity = (MainActivity) getActivity();
                            mainActivity.showPlayerDialog(p);
                        }
                    });
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        return view;
    }

    private TeamStatsListArrayAdapter getTeamStatsAdapter() {
        ArrayList<String> teamStatsList = new ArrayList<>();
        teamStatsList.add(gameModel.awayTeam + ",@," + gameModel.homeTeam);
        teamStatsList.add(gameModel.awayStats.points + ",Points," + gameModel.homeStats.points);
        teamStatsList.add((gameModel.awayStats.defensiveRebounds+gameModel.awayStats.offensiveRebounds) +
                ",Rebounds," + (gameModel.homeStats.defensiveRebounds+gameModel.homeStats.offensiveRebounds));
        teamStatsList.add(gameModel.awayStats.assists + ",Assists," + gameModel.homeStats.assists);
        teamStatsList.add(gameModel.awayStats.steals + ",Steals," + gameModel.homeStats.steals);
        teamStatsList.add(gameModel.awayStats.blocks + ",Blocks," + gameModel.homeStats.blocks);
        teamStatsList.add(gameModel.awayStats.turnovers + ",Turnovers," + gameModel.homeStats.turnovers);

        teamStatsList.add(gameModel.awayStats.fieldGoalsMade + "/" + gameModel.awayStats.fieldGoalsAttempted +
                ",FGM/FGA," + gameModel.homeStats.fieldGoalsMade + "/" + gameModel.homeStats.fieldGoalsAttempted);
        String awayFGP = DataDisplayer.getFieldGoalPercentage(
                gameModel.awayStats.fieldGoalsMade, gameModel.awayStats.fieldGoalsAttempted);
        String homeFGP = DataDisplayer.getFieldGoalPercentage(
                gameModel.homeStats.fieldGoalsMade, gameModel.homeStats.fieldGoalsAttempted);
        teamStatsList.add(awayFGP + "%,FG%," + homeFGP + "%");

        teamStatsList.add(gameModel.awayStats.threePointsMade + "/" + gameModel.awayStats.threePointsAttempted +
                ",3FGM/3FGA," + gameModel.homeStats.threePointsMade + "/" + gameModel.homeStats.threePointsAttempted);
        String away3GP = DataDisplayer.getFieldGoalPercentage(
                gameModel.awayStats.threePointsMade, gameModel.awayStats.threePointsAttempted);
        String home3GP = DataDisplayer.getFieldGoalPercentage(
                gameModel.homeStats.threePointsMade, gameModel.homeStats.threePointsAttempted);
        teamStatsList.add(away3GP + "%,3FG%," + home3GP + "%");

        return new TeamStatsListArrayAdapter(getActivity(), teamStatsList, true);
    }

}

