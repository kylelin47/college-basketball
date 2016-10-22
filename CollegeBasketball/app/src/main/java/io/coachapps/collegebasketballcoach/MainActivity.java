package io.coachapps.collegebasketballcoach;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.ViewFlipper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.coachapps.collegebasketballcoach.adapters.GameScheduleListArrayAdapter;
import io.coachapps.collegebasketballcoach.adapters.PlayerStatsListArrayAdapter;
import io.coachapps.collegebasketballcoach.adapters.TeamStatsListArrayAdapter;
import io.coachapps.collegebasketballcoach.basketballsim.GameSimThread;
import io.coachapps.collegebasketballcoach.basketballsim.Player;
import io.coachapps.collegebasketballcoach.basketballsim.PlayerGen;
import io.coachapps.collegebasketballcoach.basketballsim.Simulator;
import io.coachapps.collegebasketballcoach.basketballsim.Strategy;
import io.coachapps.collegebasketballcoach.basketballsim.Team;
import io.coachapps.collegebasketballcoach.db.DbHelper;
import io.coachapps.collegebasketballcoach.db.TeamDao;
import io.coachapps.collegebasketballcoach.db.YearlyTeamStatsDao;
import io.coachapps.collegebasketballcoach.models.YearlyTeamStats;
import io.coachapps.collegebasketballcoach.util.LeagueEvents;

public class MainActivity extends AppCompatActivity {

    Simulator bballSim;
    PlayerGen playerGen;
    List<Team> teamList;

    Spinner teamSpinner;
    TextView currTeamTextView;
    ArrayAdapter<String> dataAdapterTeam;

    PlayerStatsListArrayAdapter rosterListAdapter;
    ListView rosterList;

    TeamStatsListArrayAdapter statsListAdapter;
    ListView statsList;

    GameScheduleListArrayAdapter gameListAdapter;
    ListView gameList;

    ViewFlipper vf;
    Button statsButton;
    Button rosterButton;
    Button teamScheduleButton;
    Button simGameButton;

    int currGame;
    final int totalGames = 9;
    // nice hack 8^)
    int lastSelectedTeamPosition = 0;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DbHelper.getInstance(this).close();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle("Test Toolbar");

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
            String[] teamNames = getResources().getStringArray(R.array.team_names);
            for (int i = 0; i < 10; ++i) {
                teamList.add(new Team(teamNames[i], playerGen));
            }
            teamDao.saveTeams(teamList, "player team name");
        }

        // Sim games
        bballSim = new Simulator(MainActivity.this);
        LeagueEvents.scheduleSeason(teamList);
        //bballSim.playSeason(teamList);

        // Set up UI components
        currTeamTextView = (TextView) findViewById(R.id.currentTeamText);
        vf = (ViewFlipper) findViewById(R.id.viewFlipper);
        vf.setDisplayedChild(1);

        statsButton = (Button) findViewById(R.id.teamStatsButton);
        statsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vf.setDisplayedChild(0);
            }
        });
        rosterButton = (Button) findViewById(R.id.rosterButton);
        rosterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vf.setDisplayedChild(1);
            }
        });
        teamScheduleButton = (Button) findViewById(R.id.teamScheduleButton);
        teamScheduleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vf.setDisplayedChild(2);
            }
        });

        currGame = 0;
        simGameButton = (Button) findViewById(R.id.simGameButton);
        simGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                advanceGame();
            }
        });

        // Set up ListView
        rosterList = (ListView) findViewById(R.id.rosterList);
        statsList = (ListView) findViewById(R.id.teamStatsList);
        gameList = (ListView) findViewById(R.id.gameList);

        teamSpinner = (Spinner) findViewById(R.id.examineTeamSpinner);
        ArrayList<String> teamStrList = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            teamStrList.add(teamList.get(i).getName() + " Wins: " + teamList.get(i).wins);
        }
        dataAdapterTeam = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, teamStrList);
        dataAdapterTeam.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        teamSpinner.setAdapter(dataAdapterTeam);
        teamSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        currTeamTextView.setText(teamList.get(position).getName() +
                                " Wins: " + teamList.get(position).wins);

                        // Unless we change the ui, this can be consolidated to a single ListView
                        rosterListAdapter = new PlayerStatsListArrayAdapter(MainActivity.this,
                                teamList.get(position).players);
                        rosterList.setAdapter(rosterListAdapter);

                        statsListAdapter = new TeamStatsListArrayAdapter(MainActivity.this,
                                getTeamStatsCSVs(teamList.get(position).getName()));
                        statsList.setAdapter(statsListAdapter);

                        gameListAdapter = new GameScheduleListArrayAdapter(MainActivity.this, MainActivity.this,
                                teamList.get(position), teamList.get(position).gameSchedule);
                        gameList.setAdapter(gameListAdapter);
                        lastSelectedTeamPosition = position;
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                        //heh
                    }
                });

        // Make players clickable
        rosterList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Player p  = ((PlayerStatsListArrayAdapter) rosterList.getAdapter()).getItem(position);
                showPlayerDialog(p);
            }
        });
        //showGameSimDialog();
    }

    public void advanceGame() {
        if (currGame < totalGames) {
            LeagueEvents.playGame(2016, currGame, teamList, bballSim);
            rosterListAdapter.notifyDataSetChanged();
            statsListAdapter = new TeamStatsListArrayAdapter(MainActivity.this,
                    getTeamStatsCSVs(teamList.get(lastSelectedTeamPosition).getName()));
            statsList.setAdapter(statsListAdapter);
            gameListAdapter.notifyDataSetChanged();
            dataAdapterTeam.notifyDataSetChanged();
            currGame++;
        }
    }

    private ArrayList<String> getTeamStatsCSVs(String teamName) {
        YearlyTeamStatsDao yearlyTeamStatsDao = new YearlyTeamStatsDao(this);
        List<YearlyTeamStats> currentTeamStats = yearlyTeamStatsDao.getTeamStatsOfYear(2016);
        YearlyTeamStats statsOfSelectedTeam = null;
        for (YearlyTeamStats stats : currentTeamStats) {
            if (stats.team.equals(teamName)) {
                statsOfSelectedTeam = stats;
                break;
            }
        }
        ArrayList<String> teamStatsCSVs = new ArrayList<>();
        teamStatsCSVs.add(",,Rank");
        if (statsOfSelectedTeam == null)  {
            teamStatsCSVs.add("0 - 0,Wins - Losses,N/A");
            teamStatsCSVs.add("0.0,Points Per Game,N/A");
            teamStatsCSVs.add("0.0,Assists Per Game,N/A");
            teamStatsCSVs.add("0.0,Rebounds Per Game,N/A");
            return teamStatsCSVs;
        }
        int highestIndex = currentTeamStats.indexOf(statsOfSelectedTeam);
        while (highestIndex >= 0 && currentTeamStats.get(highestIndex).wins == statsOfSelectedTeam.wins) {
            highestIndex--;
        }
        teamStatsCSVs.add(statsOfSelectedTeam.wins + " - " + statsOfSelectedTeam.losses + ",Wins " +
                "- Losses," + String.valueOf(highestIndex + 2));
        Collections.sort(currentTeamStats, new Comparator<YearlyTeamStats>() {
            @Override
            public int compare(YearlyTeamStats left, YearlyTeamStats right) {
                return right.points < left.points ? -1 : left.points == right.points ? 0 : 1;
            }
        });
        teamStatsCSVs.add(statsOfSelectedTeam.getPGDisplay("PPG") + ",Points Per Game," +
                String.valueOf(currentTeamStats.indexOf(statsOfSelectedTeam) + 1));
        Collections.sort(currentTeamStats, new Comparator<YearlyTeamStats>() {
            @Override
            public int compare(YearlyTeamStats left, YearlyTeamStats right) {
                return right.assists < left.assists ? -1 : left.assists == right.assists ? 0 : 1;
            }
        });
        teamStatsCSVs.add(statsOfSelectedTeam.getPGDisplay("APG") + ",Assists Per Game," +
                String.valueOf(currentTeamStats.indexOf(statsOfSelectedTeam) + 1));
        Collections.sort(currentTeamStats, new Comparator<YearlyTeamStats>() {
            @Override
            public int compare(YearlyTeamStats left, YearlyTeamStats right) {
                return right.rebounds < left.rebounds ? -1 : left.rebounds == right.rebounds ? 0 : 1;
            }
        });
        teamStatsCSVs.add(statsOfSelectedTeam.getPGDisplay("RPG") + ",Rebounds Per Game," +
                String.valueOf(currentTeamStats.indexOf(statsOfSelectedTeam) + 1));
        return teamStatsCSVs;
    }

    public void showPlayerDialog(final Player p) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        DialogFragment newFragment = PlayerDialogFragment.newInstance(p);
        newFragment.show(ft, "player dialog");
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
        spinnerStrList.add("GameModel Log");
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

        final Team homeTeam = new Team("Warriors", playerGen);
        final Team awayTeam = new Team("Cavaliers", playerGen);

        final GameSimThread t = new GameSimThread(this, this, uiElements, homeTeam, awayTeam);

        uiElements.buttonCallTimeout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                t.togglePause();
                showChangeStrategyDialog(homeTeam, t);
            }
        });

        t.start();

    }

    public void showChangeStrategyDialog(final Team userTeam, final GameSimThread t) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Team Strategy")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //do nothing?
                        t.togglePause();
                    }
                })
                .setView(getLayoutInflater().inflate(R.layout.team_strategy_dialog, null));
        AlertDialog dialog = builder.create();
        dialog.show();

        // Get the options for team strategies in both offense and defense
        final Strategy.Strats[] tsOff = Strategy.Strats.getOffStrats();
        final Strategy.Strats[] tsDef = Strategy.Strats.getDefStrats();
        int offStratNum = 0;
        int defStratNum = 0;

        String[] stratOffSelection = new String[ tsOff.length ];
        for (int i = 0; i < tsOff.length; ++i) {
            stratOffSelection[i] = tsOff[i].getName();
            if (stratOffSelection[i].equals(userTeam.getOffStrat().getName())) offStratNum = i;
        }

        String[] stratDefSelection = new String[ tsDef.length ];
        for (int i = 0; i < tsDef.length; ++i) {
            stratDefSelection[i] = tsDef[i].getName();
            if (stratDefSelection[i].equals(userTeam.getDefStrat().getName())) defStratNum = i;
        }

        final TextView offStratDescription = (TextView) dialog.findViewById(R.id.textOffenseStrategy);
        final TextView defStratDescription = (TextView) dialog.findViewById(R.id.textDefenseStrategy);

        // Offense Strategy Spinner
        Spinner stratOffSelectionSpinner = (Spinner) dialog.findViewById(R.id.spinnerOffenseStrategy);
        ArrayAdapter<String> stratOffSpinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, stratOffSelection);
        stratOffSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stratOffSelectionSpinner.setAdapter(stratOffSpinnerAdapter);
        stratOffSelectionSpinner.setSelection(offStratNum);

        stratOffSelectionSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        //offStratDescription.setText(tsOff[position].getStratDescription());
                        userTeam.setOffStrat(tsOff[position]);
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                        // do nothing
                    }
                });

        // Defense Spinner Adapter
        Spinner stratDefSelectionSpinner = (Spinner) dialog.findViewById(R.id.spinnerDefenseStrategy);
        ArrayAdapter<String> stratDefSpinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, stratDefSelection);
        stratDefSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        stratDefSelectionSpinner.setAdapter(stratDefSpinnerAdapter);
        stratDefSelectionSpinner.setSelection(defStratNum);

        stratDefSelectionSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        //defStratDescription.setText(tsDef[position].getStratDescription());
                        userTeam.setDefStrat(tsDef[position]);
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                        // do nothing
                    }
                });

    }
}
