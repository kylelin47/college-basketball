package io.coachapps.collegebasketballcoach;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
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
import io.coachapps.collegebasketballcoach.basketballsim.Game;
import io.coachapps.collegebasketballcoach.basketballsim.GameSimThread;
import io.coachapps.collegebasketballcoach.basketballsim.Player;
import io.coachapps.collegebasketballcoach.basketballsim.PlayerGen;
import io.coachapps.collegebasketballcoach.basketballsim.Simulator;
import io.coachapps.collegebasketballcoach.basketballsim.Strategy;
import io.coachapps.collegebasketballcoach.basketballsim.Team;
import io.coachapps.collegebasketballcoach.db.DbHelper;
import io.coachapps.collegebasketballcoach.db.PlayerDao;
import io.coachapps.collegebasketballcoach.db.TeamDao;
import io.coachapps.collegebasketballcoach.db.YearlyTeamStatsDao;
import io.coachapps.collegebasketballcoach.models.YearlyTeamStats;
import io.coachapps.collegebasketballcoach.util.LeagueEvents;

public class MainActivity extends AppCompatActivity {
    boolean hasScheduledConferenceTournament;
    String playerTeamName = "Default Team Name";
    Team playerTeam;
    Simulator bballSim;
    PlayerGen playerGen;
    List<Team> teamList;
    List<String> teamStrList;
    List<Game> tournamentGames;

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
    Button playGameButton;
    int lastSelectedTeamPosition = 0;
    volatile boolean canSimWeek = true;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DbHelper.getInstance(this).close();
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        System.out.println("Calling onResume() in MainActivity");
        if (playerTeamName != null && playerTeam != null) {
            PlayerDao playerDao = new PlayerDao(this);
            try {
                playerTeam.players = playerDao.getPlayers(playerTeamName);
                for (Player p : playerTeam.players) {
                    System.out.println(p.name + ", lineupPos = " + p.getLineupPosition());
                }
                Collections.sort(playerTeam.players, new Comparator<Player>() {
                    @Override
                    public int compare(Player left, Player right) {
                        return right.getLineupPosition() < left.getLineupPosition() ?
                                1 : left.getLineupPosition() == right.getLineupPosition() ? 0 : -1;
                    }
                });
                examineTeam(playerTeamName);
                rosterListAdapter.clear();
                rosterListAdapter.addAll(playerTeam.players);
                rosterListAdapter.notifyDataSetChanged();
            } catch (Exception e) {
                // k
                System.out.println("Resume failed!");
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final TeamDao teamDao = new TeamDao(this);
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

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Input Your Team Name");
            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.i("MainActivity", "Set team name");
                    ((AlertDialog) dialog).getButton(DialogInterface.BUTTON_POSITIVE).setEnabled
                            (false);
                    playerTeamName = input.getText().toString().trim();
                    String replacementTeamName = "Chef Boyardees";
                    teamList = new ArrayList<>();
                    String[] teamNames = getResources().getStringArray(R.array.team_names);
                    teamList.add(new Team(playerTeamName, 99, playerGen, true));
                    for (int i = 0; i < 9; ++i) {
                        if (playerTeamName.equals(teamNames[i])) teamNames[i] = replacementTeamName;
                        teamList.add(new Team(teamNames[i], (int)(Math.random()*100), playerGen, false));
                    }
                    setEverythingUp();
                    teamDao.saveTeams(teamList, playerTeamName);
                    playerTeam.sortPlayersOvrPosition();
                    PlayerDao pd = new PlayerDao(MainActivity.this);
                    for (Player p : playerTeam.players) {
                        pd.updatePlayerRatings(p.getId(), p.ratings);
                    }
                }
            });
            builder.setCancelable(false);
            builder.show();
        } else {
            playerTeamName = teamDao.getPlayerTeamName();
            setEverythingUp();
        }
    }

    private void setEverythingUp() {
        Collections.sort(teamList, new Comparator<Team>() {
            @Override
            public int compare(Team team, Team t1) {
                if (team.name.equals(playerTeamName)) return -1;
                if (t1.name.equals(playerTeamName)) return 1;
                return team.name.compareTo(t1.name);
            }
        });
        playerTeam = teamList.get(0);
        getSupportActionBar().setTitle(playerTeamName);

        // Sim games
        bballSim = new Simulator(MainActivity.this);
        LeagueEvents.scheduleSeason(teamList, this);
        tryToScheduleConferenceTournament();
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

        simGameButton = (Button) findViewById(R.id.simGameButton);
        simGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                advanceGame(true);
            }
        });
        playGameButton = (Button) findViewById(R.id.playGameButton);
        playGameButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playNextUserGame();
            }
        });

        // Set up ListView
        rosterList = (ListView) findViewById(R.id.rosterList);
        statsList = (ListView) findViewById(R.id.teamStatsList);
        gameList = (ListView) findViewById(R.id.gameList);

        teamSpinner = (Spinner) findViewById(R.id.examineTeamSpinner);
        teamStrList = new ArrayList<>();
        populateTeamStrList();
        dataAdapterTeam = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, teamStrList);
        dataAdapterTeam.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        teamSpinner.setAdapter(dataAdapterTeam);
        teamSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        examineTeam(teamList.get(position));
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
                Player p  = rosterListAdapter.getItem(position);
                showPlayerDialog(p);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_set_lineup) {
            /**
             * Clicked Set Team Lineup
             */
            showSetLineupDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    public void examineTeam(Team team) {
        currTeamTextView.setText(team.getName());
        // Unless we change the ui, this can be consolidated to a single ListView
        rosterListAdapter = new PlayerStatsListArrayAdapter(MainActivity.this, team.players);
        rosterList.setAdapter(rosterListAdapter);

        statsListAdapter = new TeamStatsListArrayAdapter(MainActivity.this,
                getTeamStatsCSVs(team.getName()), false);
        statsList.setAdapter(statsListAdapter);

        gameListAdapter = new GameScheduleListArrayAdapter(MainActivity.this, MainActivity.this,
                team, team.gameSchedule);
        gameList.setAdapter(gameListAdapter);
    }

    public void examineTeam(String teamName) {
        int teamIndex = 0;
        for (int i = 0; i < teamList.size(); ++i) {
            if (teamList.get(i).getName().equals(teamName)) {
                teamIndex = i;
                break;
            }
        }
        teamSpinner.setSelection(teamIndex);
    }

    public void updateUI() {
        rosterListAdapter.notifyDataSetChanged();
        statsListAdapter = new TeamStatsListArrayAdapter(MainActivity.this,
                getTeamStatsCSVs(teamList.get(lastSelectedTeamPosition).getName()), false);
        statsList.setAdapter(statsListAdapter);
        gameListAdapter.notifyDataSetChanged();
        populateTeamStrList();
        dataAdapterTeam.notifyDataSetChanged();
    }

    public void advanceGame(boolean simPlayerGame) {
        if (canSimWeek) {
            canSimWeek = false;
            simGameButton.setEnabled(false);
            playGameButton.setEnabled(false);
            new SimulateGameTask().execute(simPlayerGame);
        }
    }

    public void playNextUserGame() {
        int currGame = LeagueEvents.determineLastUnplayedRegularSeasonWeek(teamList);
        if (currGame != Integer.MAX_VALUE) {
            System.out.println("currGame = " + currGame);
            Game userGame = playerTeam.gameSchedule.get(currGame);
            showGameSimDialog(userGame);
            advanceGame(false);
        } else {
            Game userGame = playerTeam.gameSchedule.get(playerTeam.gameSchedule.size() - 1);
            if (!userGame.hasPlayed()) {
                showGameSimDialog(userGame);
                advanceGame(false);
            }
        }
    }

    private void populateTeamStrList() {
        teamStrList.clear();
        for (int i = 0; i < teamList.size(); i++) {
            teamStrList.add( teamList.get(i).prestige +
                    " (" + teamList.get(i).wins + "-" + teamList.get(i).losses + ")" +
                            " " + teamList.get(i).getName());
        }
    }

    public void updateAllPlayerRatings() {
        PlayerDao pd = new PlayerDao(this);
        for (Team t : teamList) {
            for (Player p : t.players) {
                pd.updatePlayerRatings(p.getId(), p.ratings);
            }
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
            teamStatsCSVs.add("0.0,Steals Per Game,N/A");
            teamStatsCSVs.add("0.0,Blocks Per Game,N/A");
            teamStatsCSVs.add("0.0,Turnovers Per Game,N/A");
            teamStatsCSVs.add("0.0,FGM Per Game,N/A");
            teamStatsCSVs.add("0.0,FGA Per Game,N/A");
            teamStatsCSVs.add("0.0,3FGM Per Game,N/A");
            teamStatsCSVs.add("0.0,3FGA Per Game,N/A");
            return teamStatsCSVs;
        }
        int highestIndex = currentTeamStats.indexOf(statsOfSelectedTeam);

        while (highestIndex >= 0 && currentTeamStats.get(highestIndex).wins == statsOfSelectedTeam.wins) {
            highestIndex--;
        }
        teamStatsCSVs.add(statsOfSelectedTeam.wins + " - " + statsOfSelectedTeam.losses + ",Wins " +
                "- Losses," + String.valueOf(highestIndex + 2));

        // This is disgusting

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

        Collections.sort(currentTeamStats, new Comparator<YearlyTeamStats>() {
            @Override
            public int compare(YearlyTeamStats left, YearlyTeamStats right) {
                return right.steals < left.steals ? -1 : left.steals == right.steals ? 0 : 1;
            }
        });
        teamStatsCSVs.add(statsOfSelectedTeam.getPGDisplay("SPG") + ",Steals Per Game," +
                String.valueOf(currentTeamStats.indexOf(statsOfSelectedTeam) + 1));

        Collections.sort(currentTeamStats, new Comparator<YearlyTeamStats>() {
            @Override
            public int compare(YearlyTeamStats left, YearlyTeamStats right) {
                return right.blocks < left.blocks ? -1 : left.blocks == right.blocks ? 0 : 1;
            }
        });
        teamStatsCSVs.add(statsOfSelectedTeam.getPGDisplay("BPG") + ",Blocks Per Game," +
                String.valueOf(currentTeamStats.indexOf(statsOfSelectedTeam) + 1));

        Collections.sort(currentTeamStats, new Comparator<YearlyTeamStats>() {
            @Override
            public int compare(YearlyTeamStats left, YearlyTeamStats right) {
                return right.turnovers < left.turnovers ? 1 : left.turnovers == right.turnovers ? 0 : -1;
            }
        });
        teamStatsCSVs.add(statsOfSelectedTeam.getPGDisplay("TPG") + ",Turnovers Per Game," +
                String.valueOf(currentTeamStats.indexOf(statsOfSelectedTeam) + 1));

        Collections.sort(currentTeamStats, new Comparator<YearlyTeamStats>() {
            @Override
            public int compare(YearlyTeamStats left, YearlyTeamStats right) {
                return right.fgm < left.fgm ? -1 : left.fgm == right.fgm ? 0 : 1;
            }
        });
        teamStatsCSVs.add(statsOfSelectedTeam.getPGDisplay("FGMPG") + ",FGM Per Game," +
                String.valueOf(currentTeamStats.indexOf(statsOfSelectedTeam) + 1));

        Collections.sort(currentTeamStats, new Comparator<YearlyTeamStats>() {
            @Override
            public int compare(YearlyTeamStats left, YearlyTeamStats right) {
                return right.fga < left.fga ? -1 : left.fga == right.fga ? 0 : 1;
            }
        });
        teamStatsCSVs.add(statsOfSelectedTeam.getPGDisplay("FGAPG") + ",FGA Per Game," +
                String.valueOf(currentTeamStats.indexOf(statsOfSelectedTeam) + 1));

        Collections.sort(currentTeamStats, new Comparator<YearlyTeamStats>() {
            @Override
            public int compare(YearlyTeamStats left, YearlyTeamStats right) {
                return right.getFGP() < left.getFGP() ? -1 : left.getFGP() == right.getFGP() ? 0 : 1;
            }
        });
        teamStatsCSVs.add(statsOfSelectedTeam.getFGPStr() + "%,Field Goal Percentage," +
                String.valueOf(currentTeamStats.indexOf(statsOfSelectedTeam) + 1));

        Collections.sort(currentTeamStats, new Comparator<YearlyTeamStats>() {
            @Override
            public int compare(YearlyTeamStats left, YearlyTeamStats right) {
                return right.threePM < left.threePM ? -1 : left.threePM == right.threePM ? 0 : 1;
            }
        });
        teamStatsCSVs.add(statsOfSelectedTeam.getPGDisplay("3FGMPG") + ",3FGM Per Game," +
                String.valueOf(currentTeamStats.indexOf(statsOfSelectedTeam) + 1));

        Collections.sort(currentTeamStats, new Comparator<YearlyTeamStats>() {
            @Override
            public int compare(YearlyTeamStats left, YearlyTeamStats right) {
                return right.threePA < left.threePA ? -1 : left.threePA == right.threePA ? 0 : 1;
            }
        });
        teamStatsCSVs.add(statsOfSelectedTeam.getPGDisplay("3FGAPG") + ",3FGA Per Game," +
                String.valueOf(currentTeamStats.indexOf(statsOfSelectedTeam) + 1));

        Collections.sort(currentTeamStats, new Comparator<YearlyTeamStats>() {
            @Override
            public int compare(YearlyTeamStats left, YearlyTeamStats right) {
                return right.get3FGP() < left.get3FGP() ? -1 : left.get3FGP() == right.get3FGP() ? 0 : 1;
            }
        });
        teamStatsCSVs.add(statsOfSelectedTeam.get3FGPStr() + "%,3 Point Percentage," +
                String.valueOf(currentTeamStats.indexOf(statsOfSelectedTeam) + 1));

        return teamStatsCSVs;
    }

    public void showPlayerDialog(final Player p) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        DialogFragment newFragment = PlayerDialogFragment.newInstance(p);
        newFragment.show(ft, "player dialog");
    }

    public void showGameSummaryDialog(final Game gm, final int week) {
        System.out.println("showing game summary at week " + week);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        DialogFragment newFragment = GameSummaryFragment.newInstance(
                2016, week, gm.getHome().getName(), gm.getAway().getName());
        newFragment.show(ft, "game dialog");
    }

    public void showGameSimDialog(final Game gm) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(getLayoutInflater().inflate(R.layout.play_game_layout, null));
        builder.setCancelable(false);
        final AlertDialog dialog = builder.create();
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

        final GameSimThread t = new GameSimThread(this, this, uiElements, gm, tournamentGames);

        Spinner dialogSpinner = (Spinner) dialog.findViewById(R.id.spinnerGameDialog);
        ArrayList<String> spinnerStrList = new ArrayList<>();
        spinnerStrList.add("Game Log");
        spinnerStrList.add(gm.getAway().getName() + " Player Stats");
        spinnerStrList.add(gm.getHome().getName() + " Player Stats");
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
                        } else if (position == 1){
                            // Away stats
                            uiElements.listViewGameStats.setVisibility(View.VISIBLE);
                            uiElements.scrollViewGameLog.setVisibility(View.GONE);
                            uiElements.textViewGameLog.setVisibility(View.GONE);
                            t.updateStatsAdapter(false);
                        } else {
                            // Home stats
                            uiElements.listViewGameStats.setVisibility(View.VISIBLE);
                            uiElements.scrollViewGameLog.setVisibility(View.GONE);
                            uiElements.textViewGameLog.setVisibility(View.GONE);
                            t.updateStatsAdapter(true);
                        }
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                        //heh
                    }
                });

        uiElements.buttonCallTimeout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                if (t.isPlaying()) {
                    t.togglePause();
                    showChangeStrategyDialog(playerTeam, t);
                } else {
                    dialog.dismiss();
                    updateUI();
                }
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

    public void showSetLineupDialog() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        DialogFragment newFragment = SetLineupFragment.newInstance(playerTeamName);
        newFragment.show(ft, "lineup dialog");

        /*
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(getLayoutInflater().inflate(R.layout.simple_list, null));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setTitle("Set Lineup");
        final AlertDialog dialog = builder.create();
        dialog.show();

        ListView listView = (ListView) dialog.findViewById(R.id.listView);
        listView.setAdapter(new SetLineupListArrayAdapter(this, playerTeam.players));
        */
    }

    /**
     * Class responsible for simulating a week.
     * Done via a AsyncTask so the UI thread isn't overwhelmed.
     */
    private class SimulateGameTask extends AsyncTask<Boolean, Void, Void> {
        @Override
        protected Void doInBackground(Boolean... simPlayerGame) {
            boolean spg = simPlayerGame[0];
            LeagueEvents.playRegularSeasonGame(teamList, bballSim, spg, playerTeamName);
            LeagueEvents.playTournamentRound(tournamentGames, bballSim, spg, playerTeamName);
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            updateUI();
            canSimWeek = true;
            simGameButton.setEnabled(true);
            playGameButton.setEnabled(true);
            tryToScheduleConferenceTournament();
        }
    }
    private void tryToScheduleConferenceTournament() {
        if (!hasScheduledConferenceTournament && LeagueEvents
                .determineLastUnplayedRegularSeasonWeek(teamList) == Integer.MAX_VALUE) {
            tournamentGames = LeagueEvents.scheduleConferenceTournament(teamList, MainActivity
                    .this);
            hasScheduledConferenceTournament = true;
        }
    }
}
