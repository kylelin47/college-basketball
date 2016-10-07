package io.coachapps.collegebasketballcoach;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import io.coachapps.collegebasketballcoach.adapters.PlayerRatingsListArrayAdapter;
import io.coachapps.collegebasketballcoach.adapters.PlayerStatsListArrayAdapter;
import io.coachapps.collegebasketballcoach.basketballsim.GameSimThread;
import io.coachapps.collegebasketballcoach.basketballsim.Player;
import io.coachapps.collegebasketballcoach.basketballsim.PlayerGen;
import io.coachapps.collegebasketballcoach.basketballsim.Simulator;
import io.coachapps.collegebasketballcoach.basketballsim.Team;
import io.coachapps.collegebasketballcoach.db.TeamDao;

public class MainActivity extends AppCompatActivity {

    Simulator bballSim;
    PlayerGen playerGen;
    List<Team> teamList;

    Spinner teamSpinner;
    TextView currTeamTextView;

    ListView mainList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        /*

        TeamDao teamDao = new TeamDao(this);
        try {
            teamList = teamDao.getAllTeams();
        } catch (IOException | ClassNotFoundException e) {
            Log.e("MainActivity", "Could not retrieve teams", e);
            // PROBABLY JUST CRASH
        }
        if (teamList.size() == 0) {
            // Make generator, passing in possible player names
            playerGen = new PlayerGen(getString(R.string.league_player_names),
                    getString(R.string.league_last_names));

            // Make 10 teams;
            teamList = new ArrayList<>();
            for (int i = 0; i < 10; ++i) {
                teamList.add(new Team("Team " + i, playerGen));
            }
            teamDao.saveTeams(teamList, "player team name");
        }

        // Sim games
        bballSim = new Simulator(MainActivity.this);
        bballSim.playSeason(teamList);

        // Set up UI components
        currTeamTextView = (TextView) findViewById(R.id.currentTeamText);

        // Set up ListView
        mainList = (ListView) findViewById(R.id.mainList);

        teamSpinner = (Spinner) findViewById(R.id.examineTeamSpinner);
        ArrayList<String> teamStrList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            teamStrList.add(teamList.get(i).getName() + " Wins: " + teamList.get(i).getWins82());
        }
        ArrayAdapter<String> dataAdapterTeam = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, teamStrList);
        dataAdapterTeam.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        teamSpinner.setAdapter(dataAdapterTeam);
        teamSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        currTeamTextView.setText(teamList.get(position).getName() +
                                " Wins: " + teamList.get(position).getWins82());
                        mainList.setAdapter(new PlayerStatsListArrayAdapter(MainActivity.this,
                                teamList.get(position).players));
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                        //heh
                    }
                });

        // Make players clickable
        mainList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Player p  = ((PlayerStatsListArrayAdapter) mainList.getAdapter()).getItem(position);
                showPlayerDialog(p);
            }
        });

        */

        showGameSimDialog();

    }

    public void showPlayerDialog(Player p) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing?
                    }
                })
                .setView(getLayoutInflater().inflate(R.layout.player_stats, null));
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        TextView textViewName = (TextView) dialog.findViewById(R.id.textViewName);
        TextView textViewPosition = (TextView) dialog.findViewById(R.id.textViewPosition);
        TextView textViewYear = (TextView) dialog.findViewById(R.id.textViewYear);
        TextView textViewOvrPot = (TextView) dialog.findViewById(R.id.textViewOvrPot);
        TextView textViewVitals = (TextView) dialog.findViewById(R.id.textViewVitals);
        TextView textViewAttributes = (TextView) dialog.findViewById(R.id.textViewAttributes);

        try {
            textViewName.setText(p.name);
            textViewPosition.setText(Player.getPositionStr(p.getPosition()));
            textViewYear.setText("Sr");
            textViewOvrPot.setText(String.valueOf(p.getOverall()));
            textViewVitals.setText("6'7\", 250 lbs");
            textViewAttributes.setText("Attributes: " + p.attributes);

            ListView list = (ListView) dialog.findViewById(R.id.listView);
            ArrayList<String> ratings = new ArrayList<>();
            List<String> ratCSVs = p.getRatingsCSVs();
            int i = 0;
            StringBuilder sb = new StringBuilder();
            for (String rat : ratCSVs) {
                if (i == 3) {
                    ratings.add(sb.toString());
                    sb.setLength(0);
                    i = 0;
                }

                sb.append(rat + ",");

                i++;
            }
            ratings.add(sb.toString());

            list.setAdapter(new PlayerRatingsListArrayAdapter(MainActivity.this, ratings));


        } catch (java.lang.NullPointerException e) {
            // lol
        }
    }

    public void showGameSimDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(getLayoutInflater().inflate(R.layout.play_game_layout, null));
        AlertDialog dialog = builder.create();
        dialog.show();

        final GameSimThread.GameDialogElements uiElements = new GameSimThread.GameDialogElements();
        uiElements.textViewGameLog = (TextView) dialog.findViewById(R.id.textViewGameLog);
        uiElements.scrollViewGameLog = (ScrollView) dialog.findViewById(R.id.scrollViewGameLog);
        uiElements.listViewGameStats = (ListView) dialog.findViewById(R.id.listViewGameDialogStats);
        uiElements.seekBarGameSpeed = (SeekBar) dialog.findViewById(R.id.seekBarSimSpeed);
        uiElements.buttonCallTimeout = (Button) dialog.findViewById(R.id.buttonCallTimeout);
        uiElements.textViewHomeAbbr = (TextView) dialog.findViewById(R.id.gameDialogAbbrHome);
        uiElements.textViewAwayAbbr = (TextView) dialog.findViewById(R.id.gameDialogAbbrAway);
        uiElements.textViewHomeScore = (TextView) dialog.findViewById(R.id.gameDialogScoreHome);
        uiElements.textViewAwayScore = (TextView) dialog.findViewById(R.id.gameDialogScoreAway);

        Spinner dialogSpinner = (Spinner) dialog.findViewById(R.id.spinnerGameDialog);
        ArrayList<String> spinnerStrList = new ArrayList<>();
        spinnerStrList.add("Game Log");
        spinnerStrList.add("Player Stats");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerStrList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        dialogSpinner.setAdapter(dataAdapter);
        dialogSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        if (position == 0) {
                            // Game log
                            uiElements.listViewGameStats.setVisibility(View.GONE);
                            uiElements.scrollViewGameLog.setVisibility(View.VISIBLE);
                            uiElements.textViewGameLog.setVisibility(View.VISIBLE);
                        } else {
                            // Game stats
                            uiElements.listViewGameStats.setVisibility(View.VISIBLE);
                            uiElements.scrollViewGameLog.setVisibility(View.GONE);
                            uiElements.textViewGameLog.setVisibility(View.GONE);
                        }
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                        //heh
                    }
                });

        playerGen = new PlayerGen(getString(R.string.league_player_names),
                getString(R.string.league_last_names));

        Team homeTeam = new Team("Warriors", playerGen);
        Team awayTeam = new Team("Cavaliers", playerGen);

        final GameSimThread t = new GameSimThread(this, this, uiElements, homeTeam, awayTeam);

        uiElements.buttonCallTimeout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                t.togglePause();
            }
        });

        t.start();

    }
}
