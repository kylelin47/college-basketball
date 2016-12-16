package io.coachapps.collegebasketballcoach;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.InputType;
import android.util.Log;
import android.util.SparseArray;
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
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.coachapps.collegebasketballcoach.adapters.ChampionsListArrayAdapter;
import io.coachapps.collegebasketballcoach.adapters.TeamRankingsListArrayAdapter;
import io.coachapps.collegebasketballcoach.adapters.TeamStatsListArrayAdapter;
import io.coachapps.collegebasketballcoach.adapters.game.GameScheduleListArrayAdapter;
import io.coachapps.collegebasketballcoach.adapters.player.LeagueLeadersListArrayAdapter;
import io.coachapps.collegebasketballcoach.adapters.player.PlayerAwardTeamListArrayAdapter;
import io.coachapps.collegebasketballcoach.adapters.player.PlayerStatsListArrayAdapter;
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
import io.coachapps.collegebasketballcoach.fragments.BracketDialogFragment;
import io.coachapps.collegebasketballcoach.fragments.GameSummaryFragment;
import io.coachapps.collegebasketballcoach.fragments.PlayerDialogFragment;
import io.coachapps.collegebasketballcoach.fragments.SetLineupFragment;
import io.coachapps.collegebasketballcoach.models.LeagueResults;
import io.coachapps.collegebasketballcoach.models.ThreeAwardTeams;
import io.coachapps.collegebasketballcoach.models.YearlyPlayerStats;
import io.coachapps.collegebasketballcoach.util.DataDisplayer;
import io.coachapps.collegebasketballcoach.util.LeagueEvents;

public class MainActivity extends AppCompatActivity {
    Team playerTeam;
    Simulator bballSim;
    League league;
    List<Team> currentConferenceTeamList;
    SparseArray<Player> playerMap;
    SparseArray<Team> playerTeamMap;
    List<String> teamStrList;

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
    AtomicBoolean selectOutOfConferenceTeam = new AtomicBoolean(false);

    boolean doneWithSeason = false;
    int numGamesPlayerTeam = 0;

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
            currentConferenceTeamList = league.getPlayerConference();
        } catch (IOException | ClassNotFoundException e) {
            Log.e("MainActivity", "Could not retrieve teams", e);
            // PROBABLY JUST CRASH
        }
        if (currentConferenceTeamList == null) {
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
                        currentConferenceTeamList = league.getPlayerConference();
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
            playerTeam = league.getPlayerTeam();
            playerTeam.sortPlayersOvrPosition();
            PlayerDao playerDao = new PlayerDao(this);
            for (Player p : playerTeam.players) {
                playerDao.updatePlayerRatings(p.getId(), p.ratings);
            }
            setEverythingUp();
        }
    }

    private void setEverythingUp() {
        playerTeam = league.getPlayerTeam();
        league.assignPollRanks(this, getYear());
        getSupportActionBar().setTitle(getYear() + " " + playerTeam.name);

        // Sim games
        bballSim = new Simulator(MainActivity.this);
        LeagueEvents.scheduleSeason(league, this, getYear());
        tryToScheduleTournaments();
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
                } else showSeasonSummaryDialog();
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
                        onTeamSpinnerSelection(currentConferenceTeamList.get(position));
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
                currentConferenceTeamList = league.getConference(league.getConferences().get(i));
                populateTeamStrList();
                dataAdapterTeam.notifyDataSetChanged();
                if (selectOutOfConferenceTeam.getAndSet(false)) {
                    if (teamSpinner.getSelectedItemPosition() == lastSelectedTeamPosition) {
                        onTeamSpinnerSelection(currentConferenceTeamList.get(lastSelectedTeamPosition));
                    } else {
                        teamSpinner.setSelection(lastSelectedTeamPosition);
                    }
                } else {
                    if (lastSelectedTeamPosition == 0) {
                        onTeamSpinnerSelection(currentConferenceTeamList.get(0));
                    } else {
                        teamSpinner.setSelection(0);
                    }
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
        } else if (id == R.id.action_set_strategy) {
            showChangeStrategyDialog(playerTeam, null);
        } else if (id == R.id.action_team_rankings) {
            showTeamRankingsDialog();
        } else if (id == R.id.action_my_team) {
            examineTeam(playerTeam.getName());
        } else if (id == R.id.action_season_awards) {
            if (doneWithSeason) {
                showEndOfSeasonDialog();
            }
        }

        return super.onOptionsItemSelected(item);
    }

    public void onTeamSpinnerSelection(Team team) {
        int year = getYear();
        currTeamTextView.setText(team.getRankNameWLStr());
        // Unless we change the ui, this can be consolidated to a single ListView
        rosterListAdapter = new PlayerStatsListArrayAdapter(MainActivity.this, team.players, year);
        rosterList.setAdapter(rosterListAdapter);

        statsListAdapter = new TeamStatsListArrayAdapter(MainActivity.this,
                DataDisplayer.getTeamStatsCSVs(team.getName(), league, this, year), false);
        statsList.setAdapter(statsListAdapter);
        List<Game> games = new ArrayList<>(team.gameSchedule);
        games.removeAll(Collections.singleton(null));
        gameListAdapter = new GameScheduleListArrayAdapter(MainActivity.this, MainActivity.this,
                team, games);
        gameList.setAdapter(gameListAdapter);
    }

    public void examineTeam(String teamName) {
        int teamIndex = findIndex(currentConferenceTeamList, teamName);
        if (teamIndex == -1) {
            selectOutOfConferenceTeam.set(true);
            League.Conference conference = league.getTeamConference(teamName);
            lastSelectedTeamPosition = findIndex(league.getConference(conference), teamName);
            conferenceSpinner.setSelection(league.getConferences().indexOf(conference));
        } else {
            teamSpinner.setSelection(teamIndex);
        }
    }

    private int findIndex(List<Team> teams, String teamName) {
        int teamIndex = -1;
        for (int i = 0; i < teams.size(); ++i) {
            if (teams.get(i).getName().equals(teamName)) {
                teamIndex = i;
                break;
            }
        }
        return teamIndex;
    }

    public void populateMaps() {
        playerMap = new SparseArray<>();
        playerTeamMap = new SparseArray<>();
        for (Team t : league.getAllTeams()) {
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

        // Allow recent games and such to be viewed after the last tourney game is played
        if (!doneWithSeason)
            return currentYear;
        else return currentYear - 1;
    }

    public void updateUI() {
        league.assignPollRanks(this, getYear());
        rosterListAdapter.notifyDataSetChanged();
        Team currentTeam = currentConferenceTeamList.get(lastSelectedTeamPosition);
        statsListAdapter = new TeamStatsListArrayAdapter(MainActivity.this,
                DataDisplayer.getTeamStatsCSVs(currentTeam.getName(), league, this, getYear()), false);
        statsList.setAdapter(statsListAdapter);
        Game lastGame = currentTeam.gameSchedule.get(currentTeam.gameSchedule.size() - 1);
        if (lastGame != null && gameListAdapter.getPosition(lastGame) == -1) {
            gameListAdapter.add(lastGame);
        }
        gameListAdapter.notifyDataSetChanged();
        currTeamTextView.setText(currentTeam.getRankNameWLStr());
        populateTeamStrList();
        dataAdapterTeam.notifyDataSetChanged();
        playGameButton.setEnabled(!playerTeam.gameSchedule.get(playerTeam.gameSchedule.size() -
                1).hasPlayed());
        if (LeagueEvents.tryToFinishSeason(this, league)) {
            Log.i("MainActivity", "Finished season");
            // enter recruiting
            doneWithSeason = true;
            simGameButton.setText("Recruit");
            showEndOfSeasonDialog();
        }
    }

    public void showSummaryToast() {
        if (playerTeam.getNumGamesPlayed() > numGamesPlayerTeam) {
            Toast.makeText(MainActivity.this, playerTeam.getLastGameSummary(),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showSeasonSummaryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Season Summary")
                .setMessage(getSeasonSummaryStr())
                .setPositiveButton("Go to Offseason", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        advanceToOffSeason();
                    }
                });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public String getSeasonSummaryStr() {
        boolean wonConfChamp = false;
        boolean wonNatChamp = false;

        League.Conference playerConf = league.getTeamConference(playerTeam.getName());
        if (league.getConfChampionshipGame(playerConf).getWinner().getName().equals(playerTeam.getName())) {
            wonConfChamp = true;
        }
        if (LeagueEvents.getChampions(league) != null && LeagueEvents.getChampions(league)[0].equals(playerTeam.getName())) {
            wonNatChamp = true;
        }
        StringBuilder sb = new StringBuilder();
        String str = "Your team, the " + playerTeam.getName() + ", finished the season ranked #" + playerTeam.pollRank +
                " with " + playerTeam.wins + " wins and " + playerTeam.losses + " losses.\n\n";
        sb.append(str);

        int diff = playerTeam.getPrestigeDiff();
        if (diff > 0) {
            sb.append("You exceeded expectations this year and have been awarded with +" + diff + " prestige!\n\n");
        } else if (diff == 0) {
            sb.append("You met expectations, and didn't gain or lose prestige.\n\n");
        } else {
            sb.append("You fell short of expectations, and recruits took notice. You lost " + Math.abs(diff) + " prestige.\n\n");
        }

        if (wonConfChamp) {
            sb.append("You won your conference championship! Your fans and boosters are pleased, and you are awarded with +4 prestige.\n\n");
        }
        if (wonNatChamp) {
            sb.append("You won the National Championship! Great job! Fans, boosters, and recruits are impressed with your coaching. You are awarded with +10 prestige!\n\n");
        }

        return sb.toString();
    }

    public void advanceToOffSeason() {
        // Set new prestiges for all the teams and start the recruiting activity
        // 30 wins for 100 prestige, 10 wins for 0 prestige
        SQLiteDatabase db = DbHelper.getInstance(this).getReadableDatabase();
        TeamDao teamDao = new TeamDao(this);
        db.beginTransaction();
        try {
            String[] champs = LeagueEvents.getChampions(league);
            for (Team t : league.getAllTeams()) {
                int diff = t.getPrestigeDiff();
                if (champs[0].equals(t.getName())) diff += 10;
                for (int i = 1; i < 7; ++i) {
                    if (champs[i].equals(t.getName())) diff += 4;
                }
                t.prestige += diff;
                if (t.prestige < 5) t.prestige = 5;
                if (t.prestige > 95) t.prestige = 95;
                teamDao.updateTeam(t);
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }

        File recruitingFile = new File(getFilesDir(), "current_state");
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(recruitingFile), "utf-8"))) {
            writer.write("RECRUITING");
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        Intent myIntent = new Intent(MainActivity.this, RecruitingActivity.class);
        MainActivity.this.startActivity(myIntent);
    }

    public void advanceGame(boolean simPlayerGame) {
        if (canSimWeek) {
            canSimWeek = false;
            simGameButton.setEnabled(false);
            playGameButton.setEnabled(false);
            numGamesPlayerTeam = playerTeam.getNumGamesPlayed();
            new SimulateGameTask().execute(simPlayerGame);
        }
    }

    public void playNextUserGame() {
        int currGame = LeagueEvents.determineLastUnplayedRegularSeasonWeek(league.getAllTeams());
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
        for (int i = 0; i < currentConferenceTeamList.size(); i++) {
            Team t = currentConferenceTeamList.get(i);
            teamStrList.add(t.getRankNameWLStr());
        }
    }

    public void showPlayerDialog(final Player p) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        DialogFragment newFragment =
                PlayerDialogFragment.newInstance(p, playerTeamMap.get(p.getId()).getName(),
                        getYear());
        newFragment.show(ft, "player dialog");
    }

    public void showBracketDialog(Game.GameType gameType) {
        if (!gameType.isTournament()) return;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        DialogFragment newFragment;
        if (gameType == Game.GameType.TOURNAMENT_GAME) {
            newFragment = BracketDialogFragment.newInstance(league.getTournamentGames
                            (League.Conference.valueOf(currentConferenceTeamList.get(0).conference)));
        } else {
            newFragment = BracketDialogFragment.newInstance(league.getMarchMadnessGames());
        }
        newFragment.show(ft, "bracket dialog");
    }
    public void showGameSummaryDialog(final Game gm) {
        System.out.println("showing game summary at week " + gm.getWeek());
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        DialogFragment newFragment = GameSummaryFragment.newInstance(
                gm.getYear(), gm.getWeek(), gm.getHome().getName(), gm.getAway().getName(),
                gm.getHome().getRankNameWLStr(), gm.getAway().getRankNameWLStr());
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
        uiElements.buttonPause = (Button) dialog.findViewById(R.id.buttonPause);
        uiElements.textViewHomeAbbr = (TextView) dialog.findViewById(R.id.gameDialogAbbrHome);
        uiElements.textViewAwayAbbr = (TextView) dialog.findViewById(R.id.gameDialogAbbrAway);
        uiElements.textViewHomeScore = (TextView) dialog.findViewById(R.id.gameDialogScoreHome);
        uiElements.textViewAwayScore = (TextView) dialog.findViewById(R.id.gameDialogScoreAway);

        List<Game> tournamentGames = league.getMarchMadnessGames();
        if (tournamentGames == null) {
            tournamentGames = league.getTournamentGames(League.Conference.valueOf(playerTeam
                    .conference));
        }
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

        uiElements.buttonPause.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Perform action on click
                if (t.isPlaying()) {
                    t.togglePause();
                    if (t.isGamePaused()) {
                        uiElements.buttonPause.setText("Resume");
                    } else {
                        uiElements.buttonPause.setText("Pause");
                    }
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
                        if (t != null) t.togglePause();
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
                            if (playerMap.get(stats.playerId) != null) {
                                players.add(playerMap.get(stats.playerId));
                            }
                        }
                        listView.setAdapter(new LeagueLeadersListArrayAdapter(
                                MainActivity.this, players, leaders, playerTeamMap, playerTeam.getName()));
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

    public void showTeamRankingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(getLayoutInflater().inflate(R.layout.spinner_list, null));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setTitle("Team Rankings");
        final AlertDialog dialog = builder.create();
        dialog.show();

        final ListView listView = (ListView) dialog.findViewById(R.id.listView);
        final Spinner spinner = (Spinner) dialog.findViewById(R.id.spinner);
        final List<String> categoryList = new ArrayList<>();
        categoryList.add("Poll Votes");
        for (String cat : DataDisplayer.getAllCategories()) {
            categoryList.add(DataDisplayer.getDescriptionCategory(cat));
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categoryList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        if (position == 0) {
                            ArrayList<String> teamRankingsCSV =
                                    DataDisplayer.getTeamRankingsCSVs(league, MainActivity.this, getYear(),
                                            "Poll Votes", true);
                            listView.setAdapter(new TeamRankingsListArrayAdapter(MainActivity.this,
                                    teamRankingsCSV, playerTeam.getRankNameWLStr()));
                        } else {
                            boolean higherIsBetter = false;
                            if (position <= 15) higherIsBetter = true;
                            ArrayList<String> teamRankingsCSV =
                                    DataDisplayer.getTeamRankingsCSVs(league, MainActivity.this, getYear(),
                                            DataDisplayer.getAllCategories()[position - 1], higherIsBetter);
                            listView.setAdapter(new TeamRankingsListArrayAdapter(MainActivity.this,
                                    teamRankingsCSV, playerTeam.getRankNameWLStr()));
                        }
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                        // do nothing
                    }
                });
    }

    public void showEndOfSeasonDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(getLayoutInflater().inflate(R.layout.spinner_list, null));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setTitle("End Of Season Awards");
        final AlertDialog dialog = builder.create();
        dialog.show();

        final LeagueResultsEntryDao leagueResultsDao = new LeagueResultsEntryDao(this);
        populateMaps();

        Log.i("MainActivity", "leagueResults year: " + (leagueResultsDao.getCurrentYear()-1));
        final LeagueResults leagueResults = leagueResultsDao.getLeagueResults(
                leagueResultsDao.getCurrentYear()-1, leagueResultsDao.getCurrentYear()-1).get(0);

        final ListView listView = (ListView) dialog.findViewById(R.id.listView);

        final Spinner spinner = (Spinner) dialog.findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.season_awards_choices, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        // Look at the right category
                        List<Player> awardWinners = new ArrayList<>();
                        if (position == 0) {
                            listView.setAdapter(new ChampionsListArrayAdapter(MainActivity.this,
                                    DataDisplayer.getCSVChampions(leagueResults, league)));
                        }
                        else if (position < 3) {
                            // MVP or DPOY
                            if (position == 1) awardWinners.add(playerMap.get(leagueResults.mvpId));
                            else awardWinners.add(playerMap.get(leagueResults.dpoyId));

                            listView.setAdapter(new PlayerAwardTeamListArrayAdapter(
                                    MainActivity.this, awardWinners, playerTeamMap, getYear(),
                                    playerTeam.getRankNameWLStr()));
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Player p = ((PlayerAwardTeamListArrayAdapter) listView
                                            .getAdapter()).getItem(position);
                                    showPlayerDialog(p);
                                }
                            });
                        } else {
                            // Award Teams
                            ThreeAwardTeams awardTeams = leagueResults.getTeam(position-3);
                            for (int t = 0; t < 3; ++t) {
                                for (int pos = 1; pos < 6; pos++) {
                                    int pid = awardTeams.get(t).getIdPosition(pos);
                                    awardWinners.add(playerMap.get(pid));
                                }
                            }
                            listView.setAdapter(new PlayerAwardTeamListArrayAdapter(
                                    MainActivity.this, awardWinners, playerTeamMap, getYear(),
                                    playerTeam.getRankNameWLStr()));
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Player p = ((PlayerAwardTeamListArrayAdapter) listView
                                            .getAdapter()).getItem(position);
                                    showPlayerDialog(p);
                                }
                            });
                        }

                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                        // do nothing
                    }
                });

    }
    public void tryToScheduleTournaments() {
        if (LeagueEvents.determineLastUnplayedRegularSeasonWeek(league.getAllTeams()) == Integer.MAX_VALUE) {
            league.scheduleConferenceTournament(MainActivity.this);
        }
        if (league.conferenceTournamentFinished()) {
            league.scheduleMarchMadness(MainActivity.this);
        }
    }
    /**
     * Class responsible for simulating a week.
     * Done via a AsyncTask so the UI thread isn't overwhelmed.
     */
    private class SimulateGameTask extends AsyncTask<Boolean, Void, Void> {
        boolean spg;
        @Override
        protected Void doInBackground(Boolean... simPlayerGame) {
            spg = simPlayerGame[0];
            LeagueEvents.playRegularSeasonGame(league.getAllTeams(), bballSim, spg, playerTeam.name);
            LeagueEvents.playTournamentRound(league, bballSim, spg, playerTeam.name);
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            canSimWeek = true;
            simGameButton.setEnabled(true);
            tryToScheduleTournaments();
            if (spg) showSummaryToast();
            updateUI();
        }
    }
}
