package io.coachapps.collegebasketballcoach;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
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
import java.util.HashMap;
import java.util.List;

import io.coachapps.collegebasketballcoach.adapters.GameScheduleListArrayAdapter;
import io.coachapps.collegebasketballcoach.adapters.LeagueLeadersListArrayAdapter;
import io.coachapps.collegebasketballcoach.adapters.PlayerStatsListArrayAdapter;
import io.coachapps.collegebasketballcoach.adapters.TeamStatsListArrayAdapter;
import io.coachapps.collegebasketballcoach.basketballsim.Game;
import io.coachapps.collegebasketballcoach.basketballsim.GameSimThread;
import io.coachapps.collegebasketballcoach.basketballsim.League;
import io.coachapps.collegebasketballcoach.basketballsim.Player;
import io.coachapps.collegebasketballcoach.basketballsim.PlayerGen;
import io.coachapps.collegebasketballcoach.basketballsim.Simulator;
import io.coachapps.collegebasketballcoach.basketballsim.Strategy;
import io.coachapps.collegebasketballcoach.basketballsim.Team;
import io.coachapps.collegebasketballcoach.db.DbHelper;
import io.coachapps.collegebasketballcoach.db.LeagueResultsEntryDao;
import io.coachapps.collegebasketballcoach.db.PlayerDao;
import io.coachapps.collegebasketballcoach.db.Schemas;
import io.coachapps.collegebasketballcoach.db.TeamDao;
import io.coachapps.collegebasketballcoach.db.YearlyPlayerStatsDao;
import io.coachapps.collegebasketballcoach.models.YearlyPlayerStats;
import io.coachapps.collegebasketballcoach.util.DataDisplayer;
import io.coachapps.collegebasketballcoach.util.LeagueEvents;

public class MainActivity extends AppCompatActivity {
    boolean hasScheduledConferenceTournament;
    Team playerTeam;
    Simulator bballSim;
    League league;
    List<Team> teamList;
    HashMap<Integer, Player> playerMap;
    HashMap<Integer, Team> playerTeamMap;
    List<String> teamStrList;
    List<Game> tournamentGames;

    Spinner teamSpinner;
    Spinner conferenceSpinner;
    TextView currTeamTextView;
    ArrayAdapter<String> dataAdapterTeam;

    PlayerStatsListArrayAdapter rosterListAdapter;
    ListView rosterList;

    TeamStatsListArrayAdapter statsListAdapter;
    ListView statsList;

    GameScheduleListArrayAdapter gameListAdapter;
    ListView gameList;

    Button simGameButton;
    Button playGameButton;
    int lastSelectedTeamPosition = 0;
    volatile boolean canSimWeek = true;

    boolean doneWithSeason = false;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DbHelper.getInstance(this).close();
    }

    @Override
    public void onResume() {
        super.onResume();  // Always call the superclass method first
        System.out.println("Calling onResume() in MainActivity");
        if (playerTeam != null) {
            PlayerDao playerDao = new PlayerDao(this);
            try {
                playerTeam.players = playerDao.getPlayers(playerTeam.getName());
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
                examineTeam(playerTeam.getName());
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
            league = new League(teamDao.getAllTeams(getYear()));
            teamList = league.getPlayerConference();
        } catch (IOException | ClassNotFoundException e) {
            Log.e("MainActivity", "Could not retrieve teams", e);
            // PROBABLY JUST CRASH
        }
        if (teamList == null) {
            final PlayerGen playerGen = new PlayerGen
                    (getString(R.string.league_player_names),
                    getString(R.string.league_last_names), 2016);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Input Your Team Name");
            final EditText input = new EditText(this);
            input.setHint("3 or more characters");
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // do nothing
                }
            });
            builder.setCancelable(false);
            final AlertDialog dialog = builder.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View
                    .OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("MainActivity", "Set team name");
                    String playerTeamName = input.getText().toString().trim();
                    if (playerTeamName.length() >= 3) {
                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                        league = new League(playerTeamName, MainActivity.this, playerGen);
                        teamList = league.getPlayerConference();
                        setEverythingUp();
                        teamDao.saveTeams(league.getAllTeams(), playerTeamName);
                        playerTeam.sortPlayersOvrPosition();
                        PlayerDao pd = new PlayerDao(MainActivity.this);
                        for (Player p : playerTeam.players) {
                            pd.updatePlayerRatings(p.getId(), p.ratings);
                        }
                        dialog.dismiss();
                    }
                }
            });
        } else {
            setEverythingUp();
        }
    }

    private void setEverythingUp() {
        playerTeam = league.getPlayerTeam();
        getSupportActionBar().setTitle(playerTeam.name);

        // Sim games
        bballSim = new Simulator(MainActivity.this);
        LeagueEvents.scheduleSeason(teamList, this, getYear());
        tryToScheduleConferenceTournament();
        // Set up UI components
        currTeamTextView = (TextView) findViewById(R.id.currentTeamText);
        final ViewFlipper vf = (ViewFlipper) findViewById(R.id.viewFlipper);
        vf.setDisplayedChild(1);

        Button statsButton = (Button) findViewById(R.id.teamStatsButton);
        statsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vf.setDisplayedChild(0);
            }
        });
        Button rosterButton = (Button) findViewById(R.id.rosterButton);
        rosterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vf.setDisplayedChild(1);
            }
        });
        Button teamScheduleButton = (Button) findViewById(R.id.teamScheduleButton);
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
                if (!doneWithSeason) {
                    advanceGame(true);
                } else startRecruiting();
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
                        onTeamSpinnerSelection(teamList.get(position));
                        lastSelectedTeamPosition = position;
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                        //heh
                    }
                });

        conferenceSpinner = (Spinner) findViewById(R.id.examineConfSpinner);
        ArrayAdapter<String> conferenceAdapter = new ArrayAdapter<>(this, android.R.layout
                .simple_spinner_item, league.getConferenceNames());
        conferenceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        conferenceSpinner.setAdapter(conferenceAdapter);
        conferenceSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                teamList = league.getConference(league.getConferences().get(i));
                populateTeamStrList();
                dataAdapterTeam.notifyDataSetChanged();
                if (lastSelectedTeamPosition == 0) {
                    onTeamSpinnerSelection(teamList.get(0));
                } else {
                    examineTeam(teamList.get(0).name);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // do nothing
            }
        });

        // Make players clickable
        rosterList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Player p = rosterListAdapter.getItem(position);
                showPlayerDialog(p);
            }
        });

        // Populate hashmaps to store id->player and id->team
        populateMaps();
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
            showSetLineupDialog();
        } else if (id == R.id.action_league_leaders) {
            showLeagueLeadersDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    public void onTeamSpinnerSelection(Team team) {
        int year = getYear();
        currTeamTextView.setText(team.getName());
        // Unless we change the ui, this can be consolidated to a single ListView
        rosterListAdapter = new PlayerStatsListArrayAdapter(MainActivity.this, team.players, year);
        rosterList.setAdapter(rosterListAdapter);

        statsListAdapter = new TeamStatsListArrayAdapter(MainActivity.this,
                DataDisplayer.getTeamStatsCSVs(team.getName(), this, year), false);
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

    public void populateMaps() {
        playerMap = new HashMap<>();
        playerTeamMap = new HashMap<>();
        for (Team t : teamList) {
            for (Player p : t.players) {
                playerMap.put(p.getId(), p);
                playerTeamMap.put(p.getId(), t);
            }
        }
    }

    private int getYear() {
        LeagueResultsEntryDao leagueResultsEntryDao = new LeagueResultsEntryDao(this);
        int currentYear = leagueResultsEntryDao.getCurrentYear();
        Log.i("MainActivity", "Current Year: " + currentYear);
        return currentYear;
    }

    public void updateUI() {
        rosterListAdapter.notifyDataSetChanged();
        statsListAdapter = new TeamStatsListArrayAdapter(MainActivity.this,
                DataDisplayer.getTeamStatsCSVs(teamList.get(lastSelectedTeamPosition).getName(),
                        this, getYear()), false);
        statsList.setAdapter(statsListAdapter);
        gameListAdapter.notifyDataSetChanged();
        populateTeamStrList();
        dataAdapterTeam.notifyDataSetChanged();
        playGameButton.setEnabled(!playerTeam.gameSchedule.get(playerTeam.gameSchedule.size() -
                1).hasPlayed());
        if (LeagueEvents.tryToFinishTournament(tournamentGames, this)) {
            Log.i("MainActivity", "Finished tournament");
            // enter recruiting
            doneWithSeason = true;
            simGameButton.setText("Recruit");
            //onNewYear(); // for testing
        }
    }

    private void startRecruiting() {
        Intent intent = new Intent(this, RecruitingActivity.class);
        startActivity(intent);
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

    public void showPlayerDialog(final Player p) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        DialogFragment newFragment =
                PlayerDialogFragment.newInstance(p, playerTeamMap.get(p.getId()).getName(),
                        getYear());
        newFragment.show(ft, "player dialog");
    }

    public void showBracketDialog() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        DialogFragment newFragment = BracketDialogFragment.newInstance(tournamentGames);
        newFragment.show(ft, "bracket dialog");
    }
    public void showGameSummaryDialog(final Game gm) {
        System.out.println("showing game summary at week " + gm.getWeek());
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        DialogFragment newFragment = GameSummaryFragment.newInstance(
                gm.getYear(), gm.getWeek(), gm.getHome().getName(), gm.getAway().getName());
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
        ArrayAdapter<String> stratOffSpinnerAdapter = new ArrayAdapter<>(this,
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
        ArrayAdapter<String> stratDefSpinnerAdapter = new ArrayAdapter<>(this,
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
        DialogFragment newFragment = SetLineupFragment.newInstance(playerTeam.name);
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

    public void showLeagueLeadersDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(getLayoutInflater().inflate(R.layout.spinner_list, null));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setTitle("League Leaders");
        final AlertDialog dialog = builder.create();
        dialog.show();

        final YearlyPlayerStatsDao yearlyPlayerStatsDao = new YearlyPlayerStatsDao(this);
        populateMaps();

        final ListView listView = (ListView) dialog.findViewById(R.id.listView);
        final Spinner spinner = (Spinner) dialog.findViewById(R.id.spinner);
        final List<String> categoryList = new ArrayList<>();
        final List<String> schemaCategoryList = new ArrayList<>();
        categoryList.add("Points");
        categoryList.add("Rebounds");
        categoryList.add("Assists");
        categoryList.add("Field Goals Made");
        categoryList.add("Three Pointers Made");
        schemaCategoryList.add(Schemas.YearlyPlayerStatsEntry.POINTS);
        schemaCategoryList.add(Schemas.YearlyPlayerStatsEntry.DEFENSIVE_REBOUNDS);
        schemaCategoryList.add(Schemas.YearlyPlayerStatsEntry.ASSISTS);
        schemaCategoryList.add(Schemas.YearlyPlayerStatsEntry.FGM);
        schemaCategoryList.add(Schemas.YearlyPlayerStatsEntry.THREE_POINTS_MADE);
        ArrayAdapter<String> stratDefSpinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categoryList);
        stratDefSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(stratDefSpinnerAdapter);
        spinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        List<YearlyPlayerStats> leaders = yearlyPlayerStatsDao.getLeagueLeaders(
                                getYear(), 25, schemaCategoryList.get(position));
                        List<Player> players = new ArrayList<>();
                        for (YearlyPlayerStats stats : leaders) {
                            if (playerMap.containsKey(stats.playerId)) {
                                players.add(playerMap.get(stats.playerId));
                            }
                        }
                        listView.setAdapter(new LeagueLeadersListArrayAdapter(
                                MainActivity.this, players, leaders));
                        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                Player p = ((LeagueLeadersListArrayAdapter) listView
                                        .getAdapter()).getItem(position);
                                showPlayerDialog(p);
                            }
                        });
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                        // do nothing
                    }
                });
    }

    /**
     * Class responsible for simulating a week.
     * Done via a AsyncTask so the UI thread isn't overwhelmed.
     */
    private class SimulateGameTask extends AsyncTask<Boolean, Void, Void> {
        @Override
        protected Void doInBackground(Boolean... simPlayerGame) {
            boolean spg = simPlayerGame[0];
            LeagueEvents.playRegularSeasonGame(teamList, bballSim, spg, playerTeam.name);
            LeagueEvents.playTournamentRound(tournamentGames, bballSim, spg, playerTeam.name);
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
    public void tryToScheduleConferenceTournament() {
        if (!hasScheduledConferenceTournament && LeagueEvents
                .determineLastUnplayedRegularSeasonWeek(teamList) == Integer.MAX_VALUE) {
            tournamentGames = LeagueEvents.scheduleConferenceTournament(teamList, MainActivity
                    .this);
            hasScheduledConferenceTournament = true;
        }
    }
}
