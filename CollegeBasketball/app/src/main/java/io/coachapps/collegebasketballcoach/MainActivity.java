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
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import io.coachapps.collegebasketballcoach.adapters.ChampionsListArrayAdapter;
import io.coachapps.collegebasketballcoach.adapters.LeagueHistoryListArrayAdapter;
import io.coachapps.collegebasketballcoach.adapters.LeagueRecordsListArrayAdapter;
import io.coachapps.collegebasketballcoach.adapters.TeamHistoryListArrayAdapter;
import io.coachapps.collegebasketballcoach.adapters.TeamRankingsListArrayAdapter;
import io.coachapps.collegebasketballcoach.adapters.TeamStatsListArrayAdapter;
import io.coachapps.collegebasketballcoach.adapters.game.GameScheduleListArrayAdapter;
import io.coachapps.collegebasketballcoach.adapters.player.HallOfFameListArrayAdapter;
import io.coachapps.collegebasketballcoach.adapters.player.LeagueLeadersListArrayAdapter;
import io.coachapps.collegebasketballcoach.adapters.player.PlayerAwardTeamListArrayAdapter;
import io.coachapps.collegebasketballcoach.adapters.player.PlayerAwardsListArrayAdapter;
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
import io.coachapps.collegebasketballcoach.db.YearlyTeamStatsDao;
import io.coachapps.collegebasketballcoach.fragments.BracketDialogFragment;
import io.coachapps.collegebasketballcoach.fragments.GameSummaryFragment;
import io.coachapps.collegebasketballcoach.fragments.PlayerDialogFragment;
import io.coachapps.collegebasketballcoach.fragments.SetLineupFragment;
import io.coachapps.collegebasketballcoach.models.LeagueResults;
import io.coachapps.collegebasketballcoach.models.ThreeAwardTeams;
import io.coachapps.collegebasketballcoach.models.YearlyPlayerStats;
import io.coachapps.collegebasketballcoach.models.YearlyTeamStats;
import io.coachapps.collegebasketballcoach.util.DataDisplayer;
import io.coachapps.collegebasketballcoach.util.LeagueEvents;
import io.coachapps.collegebasketballcoach.util.LeagueRecords;
import io.coachapps.collegebasketballcoach.util.Settings;

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

    boolean shownMadeMissedConfDialog = false;
    boolean shownMadeMissedMarchDialog = false;

    Settings settings;
    LeagueRecords leagueRecords;
    LeagueRecords teamRecords;

    public void onBackPressed() {
        Intent myIntent = new Intent(MainActivity.this, StartActivity.class);
        MainActivity.this.startActivity(myIntent);
    }

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

            // Check to see if in the middle of game, so don't reset players
            boolean inGame = false;
            if (playerTeam.players != null) {
                for (Player p : playerTeam.players) {
                    if (p != null && p.gmStats != null && p.gmStats.secondsPlayed > 0) {
                        inGame = true;
                        break;
                    }
                }
            }

            // Only reset players if not in middle of game
            if (!inGame) {
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
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final TeamDao teamDao = new TeamDao(this);
        final PlayerGen playerGen = new PlayerGen
                (getString(R.string.league_player_names),
                        getString(R.string.league_last_names), 2016);
        try {
            league = new League(teamDao.getAllTeams(getYear(), playerGen));
            currentConferenceTeamList = league.getPlayerConference();
        } catch (IOException | ClassNotFoundException e) {
            Log.e("MainActivity", "Could not retrieve teams", e);
            // PROBABLY JUST CRASH
        }
        if (currentConferenceTeamList == null) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setView(getLayoutInflater().inflate(R.layout.new_game_dialog, null));
            builder.setTitle("New Game");
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // do nothing
                }
            });
            builder.setCancelable(false);
            final AlertDialog dialog = builder.create();
            dialog.show();

            final Spinner spinnerDifficulty = (Spinner) dialog.findViewById(R.id.spinnerDifficulty);
            final Spinner spinnerConference = (Spinner) dialog.findViewById(R.id.spinnerConference);
            final EditText editTextTeamName = (EditText) dialog.findViewById(R.id.editTextTeamName);

            final List<String> difficultyList = new ArrayList<>();
            difficultyList.add("Easy");
            difficultyList.add("Normal");
            difficultyList.add("Hard");
            ArrayAdapter<String> spinnerDifficultyAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, difficultyList);
            spinnerDifficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerDifficulty.setAdapter(spinnerDifficultyAdapter);

            List<String> conferenceList = new ArrayList<>();
            conferenceList.add("Cowboy");
            conferenceList.add("Lakes");
            conferenceList.add("Mountains");
            conferenceList.add("North");
            conferenceList.add("Pacific");
            conferenceList.add("South");
            ArrayAdapter<String> spinnerConferenceAdapter = new ArrayAdapter<>(this,
                    android.R.layout.simple_spinner_item, conferenceList);
            spinnerConferenceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinnerConference.setAdapter(spinnerConferenceAdapter);

            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View
                    .OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.i("MainActivity", "Set team name");
                    String playerTeamName = editTextTeamName.getText().toString().trim();
                    if (playerTeamName.length() >= 3) {
                        // Settings file, default settings
                        File settingsFile = new File(getFilesDir(), Settings.SETTINGS_FILE_NAME);
                        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                                new FileOutputStream(settingsFile), "utf-8"))) {
                            writer.write("Difficulty " + spinnerDifficulty.getSelectedItemPosition() + "\n");
                            writer.write("Toasts 1");
                        } catch (Exception e) {
                            System.out.println(e.toString());
                        }
                        settings = new Settings(settingsFile);

                        // Records file, hacky hack
                        leagueRecords = new LeagueRecords(null);
                        leagueRecords.saveRecords(new File(getFilesDir(), Settings.RECORDS_FILE_NAME));

                        // Records file, hacky hack
                        teamRecords = new LeagueRecords(null);
                        teamRecords.saveRecords(new File(getFilesDir(), Settings.TEAM_RECORDS_FILE_NAME));

                        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setEnabled(false);
                        playerGen.resetCurrID(2016);
                        league = new League(playerTeamName, MainActivity.this, playerGen,
                                spinnerDifficulty.getSelectedItemPosition(),
                                spinnerConference.getSelectedItemPosition());
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
            settings = new Settings(new File(getFilesDir(), Settings.SETTINGS_FILE_NAME));
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
        leagueRecords = new LeagueRecords(new File(getFilesDir(), Settings.RECORDS_FILE_NAME));
        teamRecords = new LeagueRecords(new File(getFilesDir(), Settings.TEAM_RECORDS_FILE_NAME));
        playerTeam = league.getPlayerTeam();
        league.assignPollRanks(this, getYear());
        getSupportActionBar().setTitle(getYear() + " " + playerTeam.name);

        // Sim games
        bballSim = new Simulator(MainActivity.this);
        LeagueEvents.scheduleSeason(league, this, getYear());
        tryToScheduleTournaments(false);
        // Set up UI components
        currTeamTextView = (TextView) findViewById(R.id.currentTeamText);
        final ViewFlipper vf = (ViewFlipper) findViewById(R.id.viewFlipper);
        vf.setDisplayedChild(1);

        ImageButton statsButton = (ImageButton) findViewById(R.id.teamStatsButton);
        statsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vf.setDisplayedChild(0);
            }
        });
        ImageButton rosterButton = (ImageButton) findViewById(R.id.rosterButton);
        rosterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                vf.setDisplayedChild(1);
            }
        });
        ImageButton teamScheduleButton = (ImageButton) findViewById(R.id.teamScheduleButton);
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

        if (playerTeam.gameSchedule.get(0) != null &&
                !playerTeam.gameSchedule.get(0).hasPlayed() && getYear() != 2016) {
            // Not played first game of the season, do recruit class rankings
            showRecruitClassRankings();
        }
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
            } else {
                showPOTYWatchDialog();
            }
        } else if (id == R.id.action_league_history) {
            showLeagueHistoryDialog();
        } else if (id == R.id.action_hall_of_fame) {
            showHallOfFameDialog();
        } if (id == R.id.action_settings) {
            showSettingsDialog();
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

    public int getYear() {
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
        if (settings.areToastsEnabled() && playerTeam.getNumGamesPlayed() > numGamesPlayerTeam) {
            Toast.makeText(MainActivity.this, playerTeam.getLastGameSummary(),
                    Toast.LENGTH_SHORT).show();
        }

        // Made/missed conference tournament
        if (!shownMadeMissedConfDialog) {
            int numConfGames = league.getNumConfTourneyGames();
            Log.i("MainActivity", "numConfGames = " + numConfGames);
            if (numConfGames == 1) {
                boolean made = false;
                for (Game g : playerTeam.gameSchedule) {
                    if (g != null && g.gameType == Game.GameType.TOURNAMENT_GAME) {
                        made = true;
                        break;
                    }
                }
                showMadeMissedTourneyDialog(Game.GameType.TOURNAMENT_GAME, made);
                shownMadeMissedConfDialog = true;
            }
        }

        // Made/missed march madness
        if (!shownMadeMissedMarchDialog) {
            int numMarchGames = league.getNumMarchMadnessGames();
            Log.i("MainActivity", "numMarchGames = " + numMarchGames);
            if (numMarchGames == 1) {
                boolean made = false;
                for (Game g : playerTeam.gameSchedule) {
                    if (g != null && g.gameType == Game.GameType.MARCH_MADNESS) {
                        made = true;
                        break;
                    }
                }
                showMadeMissedTourneyDialog(Game.GameType.MARCH_MADNESS, made);
                shownMadeMissedMarchDialog = true;
            }
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
        League.Conference playerConf = league.getTeamConference(playerTeam.getName());
        if (league.getConfChampionshipGame(playerConf).getWinner().getName().equals(playerTeam.getName())) {
            wonConfChamp = true;
        }

        int marchMadnessGamesWon = playerTeam.getNumMarchMadnessGamesWon();

        StringBuilder sb = new StringBuilder();
        String str = "Your team, the " + playerTeam.getName() + ", finished the season ranked #" + playerTeam.pollRank +
                " with " + playerTeam.wins + " wins and " + playerTeam.losses + " losses.\n\n";
        sb.append(str);

        int diff = (3*(playerTeam.wins+5) - playerTeam.oldPrestige)/3;
        if (diff > 0) {
            sb.append("You exceeded win expectations this year and have gained prestige!\n\n");
        } else if (diff == 0 || marchMadnessGamesWon >= 3) {
            sb.append("You met win expectations, and didn't gain or lose prestige.\n\n");
        } else {
            sb.append("You fell short of win expectations, and recruits took notice. You lost some prestige.\n\n");
        }

        if (wonConfChamp) {
            sb.append("You won your conference championship! Your fans and boosters are pleased, and you are awarded with +4 prestige.\n\n");
        }
        if (marchMadnessGamesWon == 5) {
            sb.append("You won the National Championship! Great job! Fans, boosters, and recruits are impressed with your coaching. You are awarded with +10 prestige!\n\n");
        } else if (marchMadnessGamesWon == 4) {
            sb.append("Even though you didn't win the National Championship, you still made it to the finals. You are awarded with +8 prestige!\n\n");
        } else if (marchMadnessGamesWon == 3) {
            sb.append("Making the Final Four is a big accomplishment, and your fans are pleased. You are awarded with +6 prestige!\n\n");
        } else if (marchMadnessGamesWon == 2) {
            sb.append("Though your boosters wanted more, making the Elite Eight is still a good season. You are awarded with +3 prestige.\n\n");
        }

        sb.append("Old Prestige: " + playerTeam.oldPrestige + ", New Prestige: " + playerTeam.prestige);
        if (playerTeam.prestige == 95) sb.append(" (MAX)");
        if (playerTeam.prestige == 5) sb.append(" (MIN)");

        return sb.toString();
    }

    public void advanceToOffSeason() {
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

    public void showPlayerDialog(final Player p, final String teamName, int year) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        DialogFragment newFragment =
                PlayerDialogFragment.newInstance(p, teamName, year);
        newFragment.show(ft, "player dialog");
    }

    public void showPlayerDialogFromRecord(LeagueRecords.Record r) {
        if (r.isPlayerRecord()) {
            PlayerDao pd = new PlayerDao(MainActivity.this);
            Player player = pd.getPlayer(Integer.parseInt(r.getHolder()));
            YearlyPlayerStatsDao playerStatsDao = new YearlyPlayerStatsDao(MainActivity.this);
            YearlyPlayerStats careerStats = playerStatsDao.getCareerStats(player.getId());
            showPlayerDialog(player, player.teamName, careerStats.year);
        }
    }

    public void showBracketDialog(Game.GameType gameType) {
        if (!gameType.isTournament()) return;
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        DialogFragment newFragment;
        if (gameType == Game.GameType.TOURNAMENT_GAME) {
            newFragment = BracketDialogFragment.newInstance(league.getTournamentGames
                            (League.Conference.valueOf(currentConferenceTeamList.get(0).conference)), playerTeam.getName());
        } else {
            newFragment = BracketDialogFragment.newInstance(league.getMarchMadnessGames(), playerTeam.getName());
        }
        newFragment.show(ft, "bracket dialog");
    }

    public void showGameSummaryDialog(final Game gm) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        DialogFragment newFragment = GameSummaryFragment.newInstance(
                gm.getYear(), gm.getWeek(), gm.getHome().getName(), gm.getAway().getName(),
                gm.getHome().getRankNameWLStr(), gm.getAway().getRankNameWLStr());
        newFragment.show(ft, "game dialog");
    }

    public void showGamePreviewDialog(final Game gm) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(getLayoutInflater().inflate(R.layout.game_preview_dialog, null));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do nothing?
                dialog.dismiss();;
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();

        ListView listView = (ListView) dialog.findViewById(R.id.listViewGamePreview);

        TextView textHomeName = (TextView) dialog.findViewById(R.id.textViewHomeTeamName);
        TextView textHomeWL = (TextView) dialog.findViewById(R.id.textViewHomeTeamWL);
        TextView textHomeRank = (TextView) dialog.findViewById(R.id.textViewHomeTeamScore);

        TextView textAwayName = (TextView) dialog.findViewById(R.id.textViewAwayTeamName);
        TextView textAwayWL = (TextView) dialog.findViewById(R.id.textViewAwayTeamWL);
        TextView textAwayRank = (TextView) dialog.findViewById(R.id.textViewAwayTeamScore);

        textHomeName.setText(gm.getHome().getName());
        textAwayName.setText(gm.getAway().getName());
        textHomeWL.setText("(" + gm.getHome().wins + "-" + gm.getHome().losses + ")");
        textAwayWL.setText("(" + gm.getAway().wins + "-" + gm.getAway().losses + ")");
        textHomeRank.setText("#" + gm.getHome().pollRank);
        textAwayRank.setText("#" + gm.getAway().pollRank);

        ArrayList<String> teamComparison = DataDisplayer.getGamePreviewComparison(this, getYear(), gm);

        listView.setAdapter(new TeamStatsListArrayAdapter(this, teamComparison, true));
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
                    if (!t.isGamePaused()) t.togglePause();
                    showChangeStrategyDialog(playerTeam, t);
                } else {
                    t.finishGame();
                    dialog.dismiss();
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
                    t.finishGame();
                    dialog.dismiss();
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
                        if (t != null) {
                            t.togglePause();
                            if (t.isGamePaused()) {
                                t.uiElements.buttonPause.setText("Resume");
                            } else {
                                t.uiElements.buttonPause.setText("Pause");
                            }
                        }
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
                        offStratDescription.setText(tsOff[position].getDescription());
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
                        defStratDescription.setText(tsDef[position].getDescription());
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

    public void showRecruitClassRankings() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(getLayoutInflater().inflate(R.layout.simple_list, null));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setTitle("Recruit Class Rankings");
        final AlertDialog dialog = builder.create();
        dialog.show();

        ArrayList<String> rankingsCSV = DataDisplayer.getRecruitClassRankingsCSV(league);
        final ListView listView = (ListView) dialog.findViewById(R.id.listView);
        listView.setAdapter(new TeamRankingsListArrayAdapter(MainActivity.this,
                rankingsCSV, playerTeam.getName()));

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
        categoryList.add("Total Points");
        categoryList.add("Total Rebounds");
        categoryList.add("Total Assists");
        categoryList.add("Total Blocks");
        categoryList.add("Total Steals");
        categoryList.add("Field Goals Made");
        categoryList.add("Three Pointers Made");
        categoryList.add("Free Throws Made");
        schemaCategoryList.add(Schemas.YearlyPlayerStatsEntry.POINTS);
        schemaCategoryList.add(Schemas.YearlyPlayerStatsEntry.DEFENSIVE_REBOUNDS);
        schemaCategoryList.add(Schemas.YearlyPlayerStatsEntry.ASSISTS);
        schemaCategoryList.add(Schemas.YearlyPlayerStatsEntry.BLOCKS);
        schemaCategoryList.add(Schemas.YearlyPlayerStatsEntry.STEALS);
        schemaCategoryList.add(Schemas.YearlyPlayerStatsEntry.FGM);
        schemaCategoryList.add(Schemas.YearlyPlayerStatsEntry.THREE_POINTS_MADE);
        schemaCategoryList.add(Schemas.YearlyPlayerStatsEntry.FTM);
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
                                MainActivity.this, players, leaders, schemaCategoryList.get(position),
                                playerTeamMap, playerTeam.getName()));
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

    public void showHallOfFameDialog() {
        final PlayerDao playerDao = new PlayerDao(this);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(getLayoutInflater().inflate(R.layout.spinner_list, null));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setTitle("Hall Of Fame");
        final AlertDialog dialog = builder.create();
        dialog.show();

        final ListView listView = (ListView) dialog.findViewById(R.id.listView);
        final Spinner spinner = (Spinner) dialog.findViewById(R.id.spinner);

        final List<String> hofChoices = new ArrayList<>();
        hofChoices.add("Hall of Fame");
        hofChoices.add(playerTeam.getName());
        List<Team> listAllTeams = league.getAllTeams();
        Collections.sort(listAllTeams, new Comparator<Team>() {
            @Override
            public int compare(Team a, Team b) {
                return a.getName().compareTo(b.getName());
            }
        });
        for (Team t : listAllTeams) {
            if (t != playerTeam) {
                hofChoices.add(t.getName());
            }
        }

        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, hofChoices);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        // Look at the right category
                        try {
                            List<Player> hofPlayers;
                            if (position == 0) {
                                // All HoF Players
                                hofPlayers = playerDao.getAllHoFPlayers();
                            } else {
                                // Just for one team
                                hofPlayers = playerDao.getHoFPlayers(hofChoices.get(position));
                            }

                            if (hofPlayers.isEmpty()) {
                                List<String> noPlayersList = new ArrayList<String>();
                                noPlayersList.add("No players here yet!, ");
                                listView.setAdapter(new PlayerAwardsListArrayAdapter(MainActivity.this, noPlayersList));
                            } else {
                                // Set adapter
                                listView.setAdapter(new HallOfFameListArrayAdapter(MainActivity.this, hofPlayers,
                                        getYear(), playerTeam.getName()));

                                // Allow players to be clickable
                                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                        Player p = ((HallOfFameListArrayAdapter) listView
                                                .getAdapter()).getItem(position);
                                        YearlyPlayerStatsDao statsDao = new YearlyPlayerStatsDao(MainActivity.this);
                                        List<YearlyPlayerStats> stats = statsDao.getPlayerStatsFromYears(p.getId(), 2016, getYear());
                                        int year = getYear();
                                        if (stats.size() > 0)
                                            year = stats.get(stats.size() - 1).year;
                                        showPlayerDialog(p, p.teamName, year);
                                    }
                                });
                            }
                        } catch (Exception e) {
                            Log.e("MainActivity", "Something went wrong when getting HoF");
                        }
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
        categoryList.add("Conference Standings");
        categoryList.add("Prestige");
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
                        if (position < 3) {
                            ArrayList<String> teamRankingsCSV =
                                    DataDisplayer.getTeamRankingsCSVs(league, MainActivity.this, getYear(),
                                            categoryList.get(position), true);
                            listView.setAdapter(new TeamRankingsListArrayAdapter(MainActivity.this,
                                    teamRankingsCSV, playerTeam.getRankNameWLStr()));
                        } else {
                            boolean higherIsBetter = false;
                            if (position <= 18) higherIsBetter = true;
                            ArrayList<String> teamRankingsCSV =
                                    DataDisplayer.getTeamRankingsCSVs(league, MainActivity.this, getYear(),
                                            DataDisplayer.getAllCategories()[position - 3], higherIsBetter);
                            listView.setAdapter(new TeamRankingsListArrayAdapter(MainActivity.this,
                                    teamRankingsCSV, playerTeam.getRankNameWLStr()));
                        }
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                        // do nothing
                    }
                });
    }

    public void showLeagueHistoryDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(getLayoutInflater().inflate(R.layout.spinner_list, null));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setTitle("League History");
        final AlertDialog dialog = builder.create();
        dialog.show();

        final LeagueResultsEntryDao historyDao = new LeagueResultsEntryDao(this);
        final YearlyTeamStatsDao teamStatsDao = new YearlyTeamStatsDao(this);

        final ListView listView = (ListView) dialog.findViewById(R.id.listView);

        final Spinner spinner = (Spinner) dialog.findViewById(R.id.spinner);
        final List<String> leagueHistoryChoices = new ArrayList<>();
        leagueHistoryChoices.add("League History");
        leagueHistoryChoices.add("Number Of Championships");
        leagueHistoryChoices.add("All Time Win Percentage");
        leagueHistoryChoices.add("League Records");
        leagueHistoryChoices.add("My Team Records");
        leagueHistoryChoices.add(playerTeam.getName());
        List<Team> listAllTeams = league.getAllTeams();
        Collections.sort(listAllTeams, new Comparator<Team>() {
            @Override
            public int compare(Team a, Team b) {
                return a.getName().compareTo(b.getName());
            }
        });
        for (Team t : listAllTeams) {
            if (t != playerTeam) {
                leagueHistoryChoices.add(t.getName());
            }
        }
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, leagueHistoryChoices);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(dataAdapter);
        spinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        // Look at the right category
                        if (position == 0) {
                            // League history
                            listView.setAdapter(new LeagueHistoryListArrayAdapter(MainActivity.this,
                                    historyDao.getLeagueResults(2016, getYear()), playerTeam.getName()));
                        } else if (position == 1) {
                            // Champ rankings
                            listView.setAdapter(new TeamRankingsListArrayAdapter(MainActivity.this,
                                    DataDisplayer.getChampTeamRankingsCSV(historyDao, getYear()), playerTeam.getName()));
                        } else if (position == 2) {
                            // Win Percentage
                            listView.setAdapter(new TeamRankingsListArrayAdapter(MainActivity.this,
                                    DataDisplayer.getWinPercentageRankingsCSV(teamStatsDao), playerTeam.getName()));
                        } else if (position == 3) {
                            // League Records
                            listView.setAdapter(new LeagueRecordsListArrayAdapter(MainActivity.this,
                                    leagueRecords.getAllRecords(), playerTeam.getName()));
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    LeagueRecords.Record r = ((LeagueRecordsListArrayAdapter) listView
                                            .getAdapter()).getItem(position);
                                    showPlayerDialogFromRecord(r);
                                }
                            });
                        } else if (position == 4) {
                            // Team Records
                            listView.setAdapter(new LeagueRecordsListArrayAdapter(MainActivity.this,
                                    teamRecords.getAllRecords(), "XXX"));
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    LeagueRecords.Record r = ((LeagueRecordsListArrayAdapter) listView
                                            .getAdapter()).getItem(position);
                                    showPlayerDialogFromRecord(r);
                                }
                            });
                        } else {
                            // Team histories
                            List<YearlyTeamStats> teamStatsList =
                                    teamStatsDao.getTeamStatsFromYears(
                                            leagueHistoryChoices.get(position), 2016, getYear());
                            teamStatsList.add(0, null);
                            listView.setAdapter(new TeamHistoryListArrayAdapter(MainActivity.this, teamStatsList));
                        }
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                        // do nothing
                    }
                });

    }

    public void showPOTYWatchDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(getLayoutInflater().inflate(R.layout.simple_list, null));
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.setTitle("Player of the Year Watch");
        final AlertDialog dialog = builder.create();
        dialog.show();

        YearlyPlayerStatsDao yps = new YearlyPlayerStatsDao(this);
        List<YearlyPlayerStats> playerStatsList = yps.getAllYearlyPlayerStats(getYear());
        Collections.sort(playerStatsList, new Comparator<YearlyPlayerStats>() {
            @Override
            public int compare(YearlyPlayerStats a, YearlyPlayerStats b) {
                return b.getMVPScore() - a.getMVPScore();
            }
        });
        playerStatsList = playerStatsList.subList(0, Math.min(playerStatsList.size(), 5));

        List<Player> awardWinners = new ArrayList<>();
        for (YearlyPlayerStats stats : playerStatsList) {
            awardWinners.add(playerMap.get(stats.playerId));
        }

        final ListView listView = (ListView) dialog.findViewById(R.id.listView);
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

    public List<LeagueRecords.Record> checkSeasonRecords() {
        YearlyTeamStatsDao yearlyTeamStatsDao = new YearlyTeamStatsDao(this);
        List<YearlyTeamStats> teamStats = yearlyTeamStatsDao.getTeamStatsOfYear(getYear());
        Log.i("MainActivity", "teamStats size = " + teamStats.size());
        for (YearlyTeamStats yts : teamStats) {
            leagueRecords.checkRecord(LeagueRecords.TEAM_SEASON_PPG, yts.team, yts.year, yts.getPG("PPG"));
            leagueRecords.checkRecord(LeagueRecords.TEAM_SEASON_OPPG, yts.team, yts.year, yts.getPG("OPPG"));
            leagueRecords.checkRecord(LeagueRecords.TEAM_SEASON_FGP, yts.team, yts.year, yts.getPG("FG%"));
            leagueRecords.checkRecord(LeagueRecords.TEAM_SEASON_OFGP, yts.team, yts.year, yts.getPG("OFG%"));
            leagueRecords.checkRecord(LeagueRecords.TEAM_SEASON_APG, yts.team, yts.year, yts.getPG("APG"));
            leagueRecords.checkRecord(LeagueRecords.TEAM_SEASON_RPG, yts.team, yts.year, yts.getPG("RPG"));
            leagueRecords.checkRecord(LeagueRecords.TEAM_SEASON_BPG, yts.team, yts.year, yts.getPG("BPG"));
            leagueRecords.checkRecord(LeagueRecords.TEAM_SEASON_SPG, yts.team, yts.year, yts.getPG("SPG"));

            if (yts.team.equals(playerTeam.getName())) {
                teamRecords.checkRecord(LeagueRecords.TEAM_SEASON_PPG, yts.team, yts.year, yts.getPG("PPG"));
                teamRecords.checkRecord(LeagueRecords.TEAM_SEASON_OPPG, yts.team, yts.year, yts.getPG("OPPG"));
                teamRecords.checkRecord(LeagueRecords.TEAM_SEASON_FGP, yts.team, yts.year, yts.getPG("FG%"));
                teamRecords.checkRecord(LeagueRecords.TEAM_SEASON_OFGP, yts.team, yts.year, yts.getPG("OFG%"));
                teamRecords.checkRecord(LeagueRecords.TEAM_SEASON_APG, yts.team, yts.year, yts.getPG("APG"));
                teamRecords.checkRecord(LeagueRecords.TEAM_SEASON_RPG, yts.team, yts.year, yts.getPG("RPG"));
                teamRecords.checkRecord(LeagueRecords.TEAM_SEASON_BPG, yts.team, yts.year, yts.getPG("BPG"));
                teamRecords.checkRecord(LeagueRecords.TEAM_SEASON_SPG, yts.team, yts.year, yts.getPG("SPG"));
            }
        }

        YearlyPlayerStatsDao yearlyPlayerStatsDao = new YearlyPlayerStatsDao(this);
        List<YearlyPlayerStats> playerStats = yearlyPlayerStatsDao.getAllYearlyPlayerStats(getYear());
        Log.i("MainActivity", "playerStats size = " + playerStats.size());
        for (YearlyPlayerStats yps : playerStats) {
            leagueRecords.checkRecord(LeagueRecords.PLAYER_SEASON_POINTS, String.valueOf(yps.playerId), yps.year, yps.playerStats.points);
            leagueRecords.checkRecord(LeagueRecords.PLAYER_SEASON_ASSISTS, String.valueOf(yps.playerId), yps.year, yps.playerStats.assists);
            leagueRecords.checkRecord(LeagueRecords.PLAYER_SEASON_REBOUNDS, String.valueOf(yps.playerId), yps.year, yps.playerStats.defensiveRebounds);
            leagueRecords.checkRecord(LeagueRecords.PLAYER_SEASON_STEALS, String.valueOf(yps.playerId), yps.year, yps.playerStats.steals);
            leagueRecords.checkRecord(LeagueRecords.PLAYER_SEASON_BLOCKS, String.valueOf(yps.playerId), yps.year, yps.playerStats.blocks);
            leagueRecords.checkRecord(LeagueRecords.PLAYER_SEASON_FGM, String.valueOf(yps.playerId), yps.year, yps.playerStats.fieldGoalsMade);
            leagueRecords.checkRecord(LeagueRecords.PLAYER_SEASON_3GM, String.valueOf(yps.playerId), yps.year, yps.playerStats.threePointsMade);
            if (yps.playerStats.fieldGoalsMade > 50)
                leagueRecords.checkRecord(LeagueRecords.PLAYER_SEASON_FGP, String.valueOf(yps.playerId), yps.year, yps.getPG("FG%"));
            if (yps.playerStats.threePointsMade > 50)
                leagueRecords.checkRecord(LeagueRecords.PLAYER_SEASON_3FGP, String.valueOf(yps.playerId), yps.year, yps.getPG("3P%"));

            if (playerTeamMap.get(yps.playerId) == playerTeam) {
                teamRecords.checkRecord(LeagueRecords.PLAYER_SEASON_POINTS, String.valueOf(yps.playerId), yps.year, yps.playerStats.points);
                teamRecords.checkRecord(LeagueRecords.PLAYER_SEASON_ASSISTS, String.valueOf(yps.playerId), yps.year, yps.playerStats.assists);
                teamRecords.checkRecord(LeagueRecords.PLAYER_SEASON_REBOUNDS, String.valueOf(yps.playerId), yps.year, yps.playerStats.defensiveRebounds);
                teamRecords.checkRecord(LeagueRecords.PLAYER_SEASON_STEALS, String.valueOf(yps.playerId), yps.year, yps.playerStats.steals);
                teamRecords.checkRecord(LeagueRecords.PLAYER_SEASON_BLOCKS, String.valueOf(yps.playerId), yps.year, yps.playerStats.blocks);
                teamRecords.checkRecord(LeagueRecords.PLAYER_SEASON_FGM, String.valueOf(yps.playerId), yps.year, yps.playerStats.fieldGoalsMade);
                teamRecords.checkRecord(LeagueRecords.PLAYER_SEASON_3GM, String.valueOf(yps.playerId), yps.year, yps.playerStats.threePointsMade);
                if (yps.playerStats.fieldGoalsMade > 50)
                    teamRecords.checkRecord(LeagueRecords.PLAYER_SEASON_FGP, String.valueOf(yps.playerId), yps.year, yps.getPG("FG%"));
                if (yps.playerStats.threePointsMade > 50)
                    teamRecords.checkRecord(LeagueRecords.PLAYER_SEASON_3FGP, String.valueOf(yps.playerId), yps.year, yps.getPG("3P%"));
            }
        }

        List<LeagueRecords.Record> recordsBroken = new ArrayList<>();
        for (String record : LeagueRecords.ALL_SEASON_ECORDS) {
            LeagueRecords.Record r = leagueRecords.getRecord(record);
            if (r != null && r.getYear() == getYear()) {
                // Broken this year
                recordsBroken.add(r);
            }
        }

        leagueRecords.saveRecords(new File(getFilesDir(), Settings.RECORDS_FILE_NAME));
        teamRecords.saveRecords(new File(getFilesDir(), Settings.TEAM_RECORDS_FILE_NAME));

        return recordsBroken;
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

        final List<LeagueRecords.Record> recordsBroken = checkSeasonRecords();

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
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    // Do nothing
                                }
                            });
                        } else if (position == 1) {
                            listView.setAdapter(new LeagueRecordsListArrayAdapter(MainActivity.this,
                                    recordsBroken, playerTeam.getName()));
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    LeagueRecords.Record r = ((LeagueRecordsListArrayAdapter) listView
                                            .getAdapter()).getItem(position);
                                    showPlayerDialogFromRecord(r);
                                }
                            });
                        } else if (position < 4) {
                            // MVP or DPOY
                            if (position == 2) awardWinners.add(playerMap.get(leagueResults.mvpId));
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
                            ThreeAwardTeams awardTeams = leagueResults.getTeam(position-4);
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

    public void tryToScheduleTournaments(boolean showDialogs) {
        if (LeagueEvents.determineLastUnplayedRegularSeasonWeek(league.getAllTeams()) == Integer.MAX_VALUE) {
            league.scheduleConferenceTournament(MainActivity.this);
        }
        if (league.conferenceTournamentFinished()) {
            league.scheduleMarchMadness(MainActivity.this);
        }
    }

    public void showMadeMissedTourneyDialog(Game.GameType gameType, boolean madeTourney) {
        String message = "";
        if (gameType == Game.GameType.TOURNAMENT_GAME) {
            if (madeTourney) {
                message = "Congratulations! Your team has been invited to participate in the Conference Tournament!";
            } else {
                message = "Sorry coach. Your team was not invited to the Conference Tournament.";
            }
        } else if (gameType == Game.GameType.MARCH_MADNESS) {
            if (madeTourney) {
                message = "Congratulations! Your team is going to March Madness!";
            } else {
                message = "Better luck next year. Your team wasn't invited to participate in March Madness.";
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Actually overwrite
                        dialog.dismiss();
                    }
                });
        AlertDialog dialog2 = builder.create();
        dialog2.show();
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
            tryToScheduleTournaments(true);
            if (spg) {
                showSummaryToast();
                updateUI();
            }
        }
    }

    public void showSettingsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Settings")
                .setPositiveButton("Apply", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Apply filters, done at bottom of method
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                })
                .setView(getLayoutInflater().inflate(R.layout.settings_dialog, null));
        final AlertDialog dialog = builder.create();
        dialog.show();

        String[] difficulties = {"Easy", "Normal", "Hard"};

        TextView difficultyTextView = (TextView) dialog.findViewById(R.id.textViewDifficulty);
        difficultyTextView.setText(difficulties[settings.getDifficulty()]);

        final CheckBox enableToastsCheckBox = (CheckBox) dialog.findViewById(R.id.checkBoxEnableToasts);
        enableToastsCheckBox.setChecked(settings.areToastsEnabled());

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View v) {
                settings.setEnableToasts(enableToastsCheckBox.isChecked());
                settings.saveSettings(new File(getFilesDir(), Settings.SETTINGS_FILE_NAME));
                dialog.dismiss();
            }
        });
    }
}
