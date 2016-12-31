package io.coachapps.collegebasketballcoach;

import android.app.DialogFragment;
import android.app.FragmentTransaction;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
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

import io.coachapps.collegebasketballcoach.adapters.recruiting.CommitmentsListArrayAdapter;
import io.coachapps.collegebasketballcoach.adapters.recruiting.ImprovementsListArrayAdapter;
import io.coachapps.collegebasketballcoach.adapters.player.PlayerStatsRatingsListArrayAdapter;
import io.coachapps.collegebasketballcoach.adapters.recruiting.RecruitsListArrayAdapter;
import io.coachapps.collegebasketballcoach.adapters.recruiting.StrengthWeaknessListArrayAdapter;
import io.coachapps.collegebasketballcoach.basketballsim.Player;
import io.coachapps.collegebasketballcoach.basketballsim.PlayerGen;
import io.coachapps.collegebasketballcoach.db.DbHelper;
import io.coachapps.collegebasketballcoach.db.PlayerDao;
import io.coachapps.collegebasketballcoach.db.Schemas;
import io.coachapps.collegebasketballcoach.fragments.PlayerDialogFragment;
import io.coachapps.collegebasketballcoach.models.LeagueResults;
import io.coachapps.collegebasketballcoach.models.PlayerModel;
import io.coachapps.collegebasketballcoach.models.ThreeAwardTeams;
import io.coachapps.collegebasketballcoach.util.DataDisplayer;
import io.coachapps.collegebasketballcoach.util.PlayerOverallComp;
import io.coachapps.collegebasketballcoach.basketballsim.Team;
import io.coachapps.collegebasketballcoach.db.LeagueResultsEntryDao;
import io.coachapps.collegebasketballcoach.db.TeamDao;

public class RecruitingActivity extends AppCompatActivity {

    public class TeamPlayerCommitment {
        public Player player;
        public Team team;
        public TeamPlayerCommitment(Player p, Team t) {
            player = p;
            team = t;
        }
    }

    private class Filter {
        int minPotential;
        int minShooting;
        int minDefense;
        int minPassing;
        int minRebouding;
        boolean hideUnaffordable;

        public Filter() {
            minPotential = 0;
            minShooting = 0;
            minDefense = 0;
            minPassing = 0;
            minRebouding = 0;
            hideUnaffordable = false;
        }

        public int getSelection(int minimum) {
            if (minimum < 50) return 0;
            else return (minimum - 50)/10;
        }

        public boolean meetsFilters(Player p) {
            return (p.ratings.potential >= minPotential &&
                    p.getCompositeShooting() >= minShooting &&
                    p.getCompositeDefense() >= minDefense &&
                    p.getCompositePassing() >= minPassing &&
                    p.getCompositeRebounding() >= minRebouding &&
                    ((recruitCostMap.get(p) <= playerTeamMoney && hideUnaffordable) || !hideUnaffordable));
        }
    }

    private static final int NUM_RECRUITS = 300;

    private static final int HOF_SCORE    = 200;
    private static final int MVP_SCORE    = 200;
    private static final int DPOY_SCORE   = 100;
    private static final int ALL_AMER_1st = 80;
    private static final int ALL_AMER_2nd = 60;
    private static final int ALL_AMER_3rd = 40;
    private static final int ALL_CONF_1st = 40;
    private static final int ALL_CONF_2nd = 25;
    private static final int ALL_CONF_3rd = 10;

    PlayerDao playerDao;
    TeamDao teamDao;

    PlayerGen playerGen;
    List<Team> teamList;
    Team playerTeam;
    String playerTeamName;
    int playerTeamMoney = 0;

    TextView recruitingTextView;
    Spinner recruitingSpinner;
    ListView recruitingListView;
    Button doneButton;
    Button viewTeamButton;
    ProgressDialog dialogLoading;

    List<Player> existingPlayers;
    HashMap<Player, Team> existingPlayersTeamMap;
    List<Player> availableRecruits;
    HashMap<Player, Integer> recruitCostMap;
    HashMap<Player, String> recruitPersonalityMap;
    HashMap<Player, Integer> playerImprovementMap;

    List<TeamPlayerCommitment> commitments;

    Filter recruitFilter = new Filter();

    boolean canClickRecruit = true;

    public void onBackPressed() {
        endRecruiting();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recruiting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get all teams from DB
        playerGen = new PlayerGen(getString(R.string.league_player_names),
                getString(R.string.league_last_names), getYear());
        playerDao = new PlayerDao(this);
        teamDao = new TeamDao(this);
        existingPlayers = new ArrayList<>();
        existingPlayersTeamMap = new HashMap<>();
        playerImprovementMap = new HashMap<>();
        try {
            teamList = teamDao.getAllTeams(getYear(), playerGen);
            Collections.sort(teamList, new Comparator<Team>() {
                @Override
                public int compare(Team t1, Team t2) {
                    return t2.prestige - t1.prestige;
                }
            });
            for (Team t : teamList) {
                for (Player p : t.players) {
                    existingPlayersTeamMap.put(p, t);
                }
                existingPlayers.addAll(t.players);
                t.removeSeniorsAndAddYear();
            }
        } catch (IOException | ClassNotFoundException e) {
            Log.e("RecruitingActivity", "Could not retrieve teams", e);
            // PROBABLY JUST CRASH
        }

        // Find user team
        playerTeamName = teamDao.getPlayerTeamName();
        for (Team t : teamList) {
            if (t.getName().equals(playerTeamName)) {
                playerTeam = t;
                playerTeamMoney = playerTeam.getPrestige()*15;
                break;
            }
        }

        // Make recruits
        availableRecruits = playerGen.genRecruits(NUM_RECRUITS);
        Collections.sort(availableRecruits, new PlayerOverallComp());
        recruitCostMap = new HashMap<>();
        recruitPersonalityMap = new HashMap<>();
        commitments = new ArrayList<>();

        for (Player p : availableRecruits) {
            if (p.getOverall() > 65) {
                recruitCostMap.put(p, (int)(50 + Math.random()*50 + Math.pow(2*(p.getOverall() - 65), 1.75)));
            }
            else {
                recruitCostMap.put(p, (int)(50 + Math.random()*50));
            }
            if (recruitCostMap.get(p) < 50) recruitCostMap.put(p, 50);
        }

        for (Player p : availableRecruits) {
            assignPersonality(p);
        }

        getSupportActionBar().setTitle(getYear() + " " + playerTeamName + " Recruiting");

        // Set up UI
        recruitingTextView = (TextView) findViewById(R.id.textViewRecruiting);
        recruitingSpinner = (Spinner) findViewById(R.id.spinnerRecruiting);
        recruitingListView = (ListView) findViewById(R.id.listViewRecruiting);
        doneButton = (Button) findViewById(R.id.doneButton);
        doneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                endRecruiting();
            }
        });
        viewTeamButton = (Button) findViewById(R.id.viewTeamButton);
        viewTeamButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showTeamDialog();
            }
        });

        updateTextView();
        fillSpinner();

        showPlayersLeavingDialog();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_recruiting, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_filter) {
            showFilterDialog();
        }

        return super.onOptionsItemSelected(item);
    }

    public void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Filter Recruits")
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
                .setView(getLayoutInflater().inflate(R.layout.filter_recruits_dialog, null));
        final AlertDialog dialog = builder.create();
        dialog.show();

        String[] filterSelection = {"F","D","C","B","A"};

        // Set up filter potential spinner
        final Spinner filterPotSpinner = (Spinner) dialog.findViewById(R.id.spinnerFilterPot);
        ArrayAdapter<String> filterPotSpinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, filterSelection);
        filterPotSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterPotSpinner.setAdapter(filterPotSpinnerAdapter);
        filterPotSpinner.setSelection(recruitFilter.getSelection(recruitFilter.minPotential));

        // Set up filter Shooting spinner
        final Spinner filterShootingSpinner = (Spinner) dialog.findViewById(R.id.spinnerFilterShooting);
        ArrayAdapter<String> filterShootingSpinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, filterSelection);
        filterShootingSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterShootingSpinner.setAdapter(filterShootingSpinnerAdapter);
        filterShootingSpinner.setSelection(recruitFilter.getSelection(recruitFilter.minShooting));

        // Set up filter Defense spinner
        final Spinner filterDefenseSpinner = (Spinner) dialog.findViewById(R.id.spinnerFilterDefense);
        ArrayAdapter<String> filterDefenseSpinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, filterSelection);
        filterDefenseSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterDefenseSpinner.setAdapter(filterDefenseSpinnerAdapter);
        filterDefenseSpinner.setSelection(recruitFilter.getSelection(recruitFilter.minDefense));

        // Set up filter Passing spinner
        final Spinner filterPassingSpinner = (Spinner) dialog.findViewById(R.id.spinnerFilterPassing);
        ArrayAdapter<String> filterPassingSpinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, filterSelection);
        filterPassingSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterPassingSpinner.setAdapter(filterPassingSpinnerAdapter);
        filterPassingSpinner.setSelection(recruitFilter.getSelection(recruitFilter.minPassing));

        // Set up filter Rebounding spinner
        final Spinner filterReboundingSpinner = (Spinner) dialog.findViewById(R.id.spinnerFilterRebounding);
        ArrayAdapter<String> filterReboundingSpinnerAdapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_spinner_item, filterSelection);
        filterReboundingSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterReboundingSpinner.setAdapter(filterReboundingSpinnerAdapter);
        filterReboundingSpinner.setSelection(recruitFilter.getSelection(recruitFilter.minRebouding));

        // Checkbox for removing unaffordable players
        final CheckBox filterUnaffordableCheckbox = (CheckBox) dialog.findViewById(R.id.checkBoxFilterUnaffordable);
        filterUnaffordableCheckbox.setChecked(recruitFilter.hideUnaffordable);

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View
                .OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("RecruitingActivity", "Filtered recruits");
                recruitFilter.minPotential = 50 + 10 * filterPotSpinner.getSelectedItemPosition();
                recruitFilter.minShooting = 50 + 10 * filterShootingSpinner.getSelectedItemPosition();
                recruitFilter.minDefense = 50 + 10 * filterDefenseSpinner.getSelectedItemPosition();
                recruitFilter.minPassing = 50 + 10 * filterPassingSpinner.getSelectedItemPosition();
                recruitFilter.minRebouding = 50 + 10 * filterReboundingSpinner.getSelectedItemPosition();
                recruitFilter.hideUnaffordable = filterUnaffordableCheckbox.isChecked();
                applyFilters();
                dialog.dismiss();
            }
        });
    }

    public void applyFilters() {
        int selection = recruitingSpinner.getSelectedItemPosition();
        updateListView(selection);
    }

    public void endRecruiting() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(RecruitingActivity.this);
        builder.setMessage("Are you sure you are done with recruiting? Any open positions will be filled by walk-ons.")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Actually overwrite
                        dialogLoading = ProgressDialog.show(RecruitingActivity.this, "",
                                getResources().getString(R.string.recruiting_loading_msg), true);
                        dialogLoading.setCancelable(false);
                        new FinishRecruitingTask().execute();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // Do nothing
                        dialog.dismiss();
                    }
                });
        android.app.AlertDialog dialog = builder.create();
        dialog.show();
    }

    public void goToMainActivity() {
        File recruitingFile = new File(getFilesDir(), "current_state");
        try (Writer writer = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(recruitingFile), "utf-8"))) {
            writer.write("SEASON");
        } catch (Exception e) {
            System.out.println(e.toString());
        }

        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }

    public void recruitPlayer(Team team, Player player) {
        if (player == null) {
            System.out.print(team.getName() + " tried to recruit a null player!");
            return;
        }
        //System.out.println(team.getName() + " recruited " + player.toString());
        TeamPlayerCommitment commit = new TeamPlayerCommitment(player, team);
        commitments.add(commit);
        team.players.add(player);
        availableRecruits.remove(player);
    }

    public void letComputerTeamsRecruit(boolean updateUI) {
        for (Team t : teamList) {
            if (t != playerTeam) {
                Player p = t.recruitPlayerFromList(availableRecruits);
                if (p != null) {
                    recruitPlayer(t, p);
                }
            }
        }

        if (updateUI) {
            // Update UI
            int selection = recruitingSpinner.getSelectedItemPosition();
            updateListView(selection);
            fillSpinner();
            recruitingSpinner.setSelection(selection);
            showCommitmentsDialog();
        }
    }

    private int getYear() {
        LeagueResultsEntryDao leagueResultsEntryDao = new LeagueResultsEntryDao(this);
        int currentYear = leagueResultsEntryDao.getCurrentYear();
        Log.i("RecruitingActivity", "Current Year: " + currentYear);
        return currentYear;
    }

    private void updateTextView() {
        String str = "Prestige: " + playerTeam.getPrestige() +
                ", Budget: $" + playerTeamMoney;
        recruitingTextView.setText(str);
    }

    private void fillSpinner() {
        List<String> spinnerList = new ArrayList<>();
        spinnerList.add("Top 50 Players");
        spinnerList.add("Point Guards (Team: " + playerTeam.getPosTotals(1) + "/2)");
        spinnerList.add("Shooting Guards (Team: " + playerTeam.getPosTotals(2) + "/2)");
        spinnerList.add("Small Forwards (Team: " + playerTeam.getPosTotals(3) + "/2)");
        spinnerList.add("Power Forwards (Team: " + playerTeam.getPosTotals(4) + "/2)");
        spinnerList.add("Centers (Team: " + playerTeam.getPosTotals(5) + "/2)");
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, spinnerList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        recruitingSpinner.setAdapter(dataAdapter);
        recruitingSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        // Update to that position
                        updateListView(position);
                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                        //heh
                    }
                });
    }

    public void updateListView(int selection) {
        List<Player> filteredList = new ArrayList<>();
        if (selection == 0) {
            List<Player> allPlayers = new ArrayList<>();
            allPlayers.addAll(availableRecruits);
            for (Player p : allPlayers) {
                if (recruitFilter.meetsFilters(p)) filteredList.add(p);
            }
            Collections.sort(filteredList, new PlayerOverallComp());
            filteredList = filteredList.subList(0, Math.min(50, filteredList.size()));
            recruitingListView.setAdapter(new RecruitsListArrayAdapter(this, filteredList,
                    recruitCostMap, recruitPersonalityMap));
        } else {
            List<Player> allPlayers = new ArrayList<>();
            allPlayers.addAll(availableRecruits);
            for (Player p : allPlayers) {
                if (recruitFilter.meetsFilters(p) && p.getPosition() == selection) filteredList.add(p);
            }
            Collections.sort(filteredList, new PlayerOverallComp());
            recruitingListView.setAdapter(new RecruitsListArrayAdapter(this, filteredList,
                    recruitCostMap, recruitPersonalityMap));
        }

        recruitingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (canClickRecruit) {
                    canClickRecruit = false;
                    Player p = ((RecruitsListArrayAdapter) recruitingListView
                            .getAdapter()).getItem(position);
                    showRecruitDialog(p);
                }
            }
        });
    }

    public void showPlayersLeavingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(getLayoutInflater().inflate(R.layout.spinner_list, null));
        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.setTitle("Players Leaving");
        final AlertDialog dialog = builder.create();
        dialog.show();

        // Graduating seniors
        final List<Player> playerTeamSeniors = new ArrayList<>();
        for (Player p : existingPlayers) {
            if (existingPlayersTeamMap.get(p) == playerTeam && p.year == 5) {
                playerTeamSeniors.add(p);
            }
        }
        Collections.sort(playerTeamSeniors, new PlayerOverallComp());

        // Players leaving early for the pros
        final List<Player> playersLeavingEarly = new ArrayList<>();
        for (Player p : existingPlayers) {
            if (existingPlayersTeamMap.get(p) == playerTeam && p.year == 6) {
                playersLeavingEarly.add(p);
            }
        }
        Collections.sort(playersLeavingEarly, new PlayerOverallComp());

        // Add money for players that leave early
        int bonusMoney = playersLeavingEarly.size() * 200;
        playerTeamMoney += bonusMoney;
        if (bonusMoney == 0) {
            Toast.makeText(RecruitingActivity.this, "None of your players left early for the pros.",
                    Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(RecruitingActivity.this, playersLeavingEarly.size() +
                    " of your players left early for the pros, granting you $" + bonusMoney + " extra recruiting budget.",
                    Toast.LENGTH_LONG).show();
        }
        updateTextView();

        final ListView listView = (ListView) dialog.findViewById(R.id.listView);
        final Spinner spinner = (Spinner) dialog.findViewById(R.id.spinner);
        final List<String> categoryList = new ArrayList<>();
        categoryList.add("Players Leaving For Pros");
        categoryList.add("Players Graduating");
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, categoryList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(
                            AdapterView<?> parent, View view, int position, long id) {
                        if (position == 0) {
                            listView.setAdapter(new PlayerStatsRatingsListArrayAdapter(
                                    RecruitingActivity.this, playersLeavingEarly, getYear() - 1));
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Player p = ((PlayerStatsRatingsListArrayAdapter)
                                            (listView.getAdapter())).getItem(position);
                                    showPlayerDialog(p);
                                }
                            });
                        } else {
                            listView.setAdapter(new PlayerStatsRatingsListArrayAdapter(
                                    RecruitingActivity.this, playerTeamSeniors, getYear() - 1));
                            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                @Override
                                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                    Player p = ((PlayerStatsRatingsListArrayAdapter)
                                            (listView.getAdapter())).getItem(position);
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

    public void showCommitmentsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(getLayoutInflater().inflate(R.layout.simple_list, null));
        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.setTitle("Commitments");
        final AlertDialog dialog = builder.create();
        dialog.show();

        final ListView listView = (ListView) dialog.findViewById(R.id.listView);
        listView.setAdapter(new CommitmentsListArrayAdapter(this, commitments));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Player p = ((CommitmentsListArrayAdapter)(listView.getAdapter())).getItem(position).player;
                showPlayerDialog(p);
            }
        });
    }

    public void showTeamDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(getLayoutInflater().inflate(R.layout.simple_list, null));
        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        builder.setTitle(playerTeamName);
        final AlertDialog dialog = builder.create();
        dialog.show();

        // Show player stats of team from the past year
        Collections.sort(playerTeam.players, new Comparator<Player>() {
            @Override
            public int compare(Player p1, Player p2) {
                if (p1.getPosition() == p2.getPosition()) {
                    return p2.getOverall() - p1.getOverall();
                }
                else return p1.getPosition() - p2.getPosition();
            }
        });
        final ListView listView = (ListView) dialog.findViewById(R.id.listView);
        listView.setAdapter(new PlayerStatsRatingsListArrayAdapter(this, playerTeam.players, getYear()-1));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Player p = ((PlayerStatsRatingsListArrayAdapter)(listView.getAdapter())).getItem(position);
                showPlayerDialog(p);
            }
        });
    }

    public void showImprovementsDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(getLayoutInflater().inflate(R.layout.simple_list, null));
        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        goToMainActivity();
                    }
                });
        builder.setTitle(playerTeamName + " Improvements");
        builder.setCancelable(false);
        final AlertDialog dialog = builder.create();
        dialog.show();

        List<String> values = new ArrayList<>();
        Collections.sort(playerTeam.players, new PlayerOverallComp());
        for (Player p : playerTeam.players) {
            String plusMinus = playerImprovementMap.get(p) >= 0 ? "+" : "";
            values.add(plusMinus + playerImprovementMap.get(p) + "," + p.toString());
        }

        final ListView listView = (ListView) dialog.findViewById(R.id.listView);
        listView.setAdapter(new ImprovementsListArrayAdapter(this, values));
    }

    public void showPlayerDialog(final Player p) {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        DialogFragment newFragment =
                PlayerDialogFragment.newInstance(p, playerTeamName, getYear()-1);
        newFragment.show(ft, "player dialog");
    }

    public void showRecruitDialog(final Player p) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(getLayoutInflater().inflate(R.layout.recruit_dialog, null));
        builder.setPositiveButton("OK",
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                canClickRecruit = true;
                dialog.dismiss();
            }
        });
        builder.setNeutralButton("Recruit: $" + recruitCostMap.get(p),
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (playerTeamMoney >= recruitCostMap.get(p)) {
                    playerTeamMoney -= recruitCostMap.get(p);
                    updateTextView();
                    recruitPlayer(playerTeam, p);
                    letComputerTeamsRecruit(true);
                    dialog.dismiss();
                    Toast.makeText(RecruitingActivity.this, "Recruited " + p.name,
                            Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(RecruitingActivity.this, "Not enough money!",
                            Toast.LENGTH_LONG).show();
                }
                canClickRecruit = true;
            }
        });
        final AlertDialog dialog = builder.create();
        dialog.show();

        TextView playerName = (TextView) dialog.findViewById(R.id.textViewName);
        TextView playerPosition = (TextView) dialog.findViewById(R.id.textViewPosition);
        TextView playerOvrPot = (TextView) dialog.findViewById(R.id.textViewOvrPot);

        playerName.setText(p.name + " [" + DataDisplayer.getYearAbbreviation(p.year) + "]");
        playerPosition.setText(DataDisplayer.getPositionAbbreviation(p.getPosition()));
        playerOvrPot.setText(String.valueOf(p.getOverall()) + " / " +
                DataDisplayer.getLetterGrade(p.getPotential()));

        TextView playerShooting    = (TextView) dialog.findViewById(R.id.textViewShooting);
        TextView playerDefense     = (TextView) dialog.findViewById(R.id.textViewDefense);
        TextView playerPassing     = (TextView) dialog.findViewById(R.id.textViewPassing);
        TextView playerRebounding  = (TextView) dialog.findViewById(R.id.textViewRebounding);
        TextView playerCost        = (TextView) dialog.findViewById(R.id.textViewCost);
        TextView playerPersonality = (TextView) dialog.findViewById(R.id.textViewPersonality);

        playerShooting.setText(DataDisplayer.getLetterGrade(p.getCompositeShooting()));
        playerDefense.setText(DataDisplayer.getLetterGrade(p.getCompositeDefense()));
        playerPassing.setText(DataDisplayer.getLetterGrade(p.getCompositePassing()));
        playerRebounding.setText(DataDisplayer.getLetterGrade(p.getCompositeRebounding()));
        playerCost.setText("$" + recruitCostMap.get(p));
        playerPersonality.setText(recruitPersonalityMap.get(p));

        DataDisplayer.colorizeRatings(playerShooting);
        DataDisplayer.colorizeRatings(playerDefense);
        DataDisplayer.colorizeRatings(playerPassing);
        DataDisplayer.colorizeRatings(playerRebounding);

        ListView strengthsListView = (ListView) dialog.findViewById(R.id.listViewStrengths);
        ListView weaknessesListView = (ListView) dialog.findViewById(R.id.listViewWeaknesses);

        List<String> strengths = getStrengths(p);

        List<String> weaknesses = getWeaknesses(p);

        strengthsListView.setAdapter(new StrengthWeaknessListArrayAdapter(this, strengths, true));
        weaknessesListView.setAdapter(new StrengthWeaknessListArrayAdapter(this, weaknesses, false));
    }

    public void finishRecruiting() {
        for (int i = 0; i < 3; ++i) {
            letComputerTeamsRecruit(false);
        }

        for (Team t : teamList) {
            List<Player> walkOns = t.recruitWalkOns(playerGen);
            System.out.println(t.getName() + " made " + walkOns.size() + " walk ons");
            for (Player p : walkOns) {
                commitments.add(new TeamPlayerCommitment(p, t));
            }
        }

        LeagueResultsEntryDao leagueResultsEntryDao = new LeagueResultsEntryDao(this);
        List<LeagueResults> leagueResultsList = leagueResultsEntryDao.getLeagueResults(getYear()-4, getYear());

        SQLiteDatabase db = DbHelper.getInstance(this).getReadableDatabase();
        db.beginTransaction();
        try {
            for (TeamPlayerCommitment c : commitments) {
                // Don't save players til after recruiting
                if (c.player == null) {
                    System.out.println("commitment has null player: " + c.team.getName());
                } else {
                    playerImprovementMap.put(c.player, 0);
                    playerDao.save(new PlayerModel(c.player, c.team.getName()));
                }
            }

            for (Player p : existingPlayers) {
                // Hall of Fame check
                if (p.year == 5 || p.year == 6) {
                    if (getHallOfFameScore(leagueResultsList, p) >= HOF_SCORE) {
                        p.year = 7;
                    }
                } else {
                    int oldOverall = p.getOverall();
                    PlayerGen.advanceYearRatings(p.ratings);
                    p.updateOverall();
                    int newOverall = p.getOverall();
                    playerImprovementMap.put(p, newOverall - oldOverall);
                }

                playerDao.updatePlayer(new PlayerModel(p, existingPlayersTeamMap.get(p).getName()));
            }
            db.delete(Schemas.BoxScoreEntry.TABLE_NAME, null, null);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    private int getHallOfFameScore(List<LeagueResults> leagueResultsList, Player player) {
        int score = 0;
        int id = player.getId();
        try {
            for (LeagueResults results : leagueResultsList) {

                if (results.mvpId == id) {
                    // MVP
                    score += MVP_SCORE;
                }
                if (results.dpoyId == id) {
                    // DPOY
                    score += DPOY_SCORE;
                }

                // All Americans
                if (results.allAmericans.firstTeam.contains(id)) {
                    score += ALL_AMER_1st;
                } else if (results.allAmericans.secondTeam.contains(id)) {
                    score += ALL_AMER_2nd;
                } else if (results.allAmericans.thirdTeam.contains(id)) {
                    score += ALL_AMER_3rd;
                }

                // All Conference Teams
                score += checkConfAwardTeams(results.allCowboy, id);
                score += checkConfAwardTeams(results.allLakes, id);
                score += checkConfAwardTeams(results.allMountains, id);
                score += checkConfAwardTeams(results.allNorth, id);
                score += checkConfAwardTeams(results.allPacific, id);
                score += checkConfAwardTeams(results.allSouth, id);
            }
        } catch (Exception e) {
            // uh oh
        }

        return score;
    }

    private int checkConfAwardTeams(ThreeAwardTeams teams, int id) {
        if (teams.firstTeam.contains(id)) {
            return ALL_CONF_1st;
        }
        else if (teams.secondTeam.contains(id)) {
            return ALL_CONF_2nd;
        }
        else if (teams.thirdTeam.contains(id)) {
            return ALL_CONF_3rd;
        }
        return 0;
    }

    /**
     * Class responsible for finishing up recruiting
     * Done via a AsyncTask so the UI thread isn't overwhelmed.
     */
    private class FinishRecruitingTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... throwaways) {
            finishRecruiting();
            return null;
        }
        @Override
        protected void onPostExecute(Void result) {
            dialogLoading.dismiss();
            showImprovementsDialog();
        }
    }

    private List<String> getStrengths(Player p) {
        List<String> list = new ArrayList<>();
        int threshold = 90;
        if (p.ratings.potential >= threshold) {
            list.add("Has great potential, is likely to improve much during his college career");
        }
        if (p.ratings.bballIQ >= threshold) {
            list.add("Is a student of the game, a very smart basketball player");
        }
        if (p.ratings.insideShooting >= threshold) {
            list.add("Is a solid threat to drive to the basket for easy dunks and lay-ups");
        }
        if (p.ratings.midrangeShooting >= threshold) {
            list.add("Has a silky smooth midrange jumper");
        }
        if (p.ratings.outsideShooting >= threshold) {
            list.add("Is a great shooter from long range, will frustrate perimeter defenders");
        }
        if (p.ratings.passing >= threshold) {
            list.add("A fantastic passer, finds the open man with ease");
        }
        if (p.ratings.handling >= threshold) {
            list.add("Handles the ball very well, great dribbling skills");
        }
        if (p.ratings.steal >= threshold) {
            list.add("A great pickpocket, has the ability to get steals on a nightly basis");
        }
        if (p.ratings.block >= threshold) {
            list.add("Can block shots with the best of them");
        }
        if (p.ratings.insideDefense >= threshold) {
            list.add("A true rim protector, plays interior defense at a high level");
        }
        if (p.ratings.perimeterDefense >= threshold) {
            list.add("Great lateral quickness and defensive instincts, can shut down perimeter shooters");
        }
        if (p.ratings.rebounding >= threshold) {
            list.add("Is a force on the boards, boxes out well and grabs rebounds");
        }
        return list;
    }

    private List<String> getWeaknesses(Player p) {
        List<String> list = new ArrayList<>();
        int threshold = 75;
        if (p.ratings.potential <= threshold) {
            list.add("Not much of a grower, isn't expected to improve much");
        }
        if (p.ratings.bballIQ <= threshold) {
            list.add("He ain't come here to play school, doesn't have the best Basketball IQ");
        }
        if (p.ratings.insideShooting <= threshold) {
            list.add("Not a great finisher at the rim");
        }
        if (p.ratings.midrangeShooting <= threshold) {
            list.add("Doesn't have a midrange jumper to speak of");
        }
        if (p.ratings.outsideShooting <= threshold) {
            list.add("Isn't a threat from downtown");
        }
        if (p.ratings.passing <= threshold) {
            list.add("Doesn't have good court vision, isn't known as a passer");
        }
        if (p.ratings.handling <= threshold) {
            list.add("Has bricks for hands, has a tendency to fumble the ball when dribbling");
        }
        if (p.ratings.insideDefense <= threshold) {
            list.add("Is a bit of a turnstile when defending the rim");
        }
        if (p.ratings.perimeterDefense <= threshold) {
            list.add("Not known as a good perimeter defender");
        }
        if (p.ratings.rebounding <= threshold) {
            list.add("Not a good rebounder");
        }
        return list;
    }

    private void assignPersonality(Player p) {
        int personality = (int)(15*Math.random());
        if (personality == 0) {
            // Hometown discount
            recruitPersonalityMap.put(p, "Grew up a fan of the " + playerTeamName + ".");
            recruitCostMap.put(p, (recruitCostMap.get(p)*3)/4);
        } else if (personality == 1) {
            // Hates your team
            recruitPersonalityMap.put(p, "Has despised the " + playerTeamName + " since he was a child.");
            recruitCostMap.put(p, (recruitCostMap.get(p)*4)/3);
        } else if (personality > 7) {
            recruitPersonalityMap.put(p, getAttributePersonality(p.ratings.getBestAttribute()));
        } else {
            recruitPersonalityMap.put(p, getRandomPersonality());
        }
    }

    private String getAttributePersonality(String attribute) {
        if (attribute.equals("Potential")) {
            return "Has tremendous potential, is expected to improve greatly throughout his career.";
        } else if (attribute.equals("BBall IQ")) {
            return "Is an incredibly smart basketball player, the game comes naturally to him.";
        } else if (attribute.equals("Inside Shooting")) {
            return "Is a great threat inside the restricted area, known for acrobatic layups and high flying dunks.";
        } else if (attribute.equals("Midrange Shooting")) {
            return "Is money from midrange, 15 feet is his sweet spot.";
        } else if (attribute.equals("Outside Shooting")) {
            return "Made a name for himself as Dr. Splash, a long range assassin.";
        } else if (attribute.equals("Passing")) {
            return "He's no Magic Johnson, but his flashy passing certainly brings comparisons.";
        } else if (attribute.equals("Stealing")) {
            return "Watch your wallet, this player is known as the best pickpocket in his state.";
        } else if (attribute.equals("Blocking")) {
            return "Averaged 5 blocks and 3 finger wags per game as a senior.";
        } else if (attribute.equals("Inside Defense")) {
            return "Known simply as The Wall. Good luck laying the ball in against him.";
        } else if (attribute.equals("Perimeter Defense")) {
            return "A lock down perimeter defender, held 5 star prospects to season lows in high school.";
        } else {
            return getRandomPersonality();
        }
    }

    private String getRandomPersonality() {
        String[] personalities =
                {
                        "Known to be a joker in the locker room.",
                        "He is all business on the court, takes the game very seriously.",
                        "Grew up the son of an NBA player and has been around the game his whole life.",
                        "Came from a very bad neighborhood, and used basketball to escape.",
                        "Was a late bloomer in high school, didn't make the varsity team until his senior year.",
                        "Was also a 5-star football prospect, but ultimately chose basketball.",
                        "Tore his ACL in his junior year, but bounced back in a big way.",
                        "Starred in a hit dunk highlight video (as the one being dunked on).",
                        "Spends more time browsing reddit than watching film.",
                        "Lost ten pounds in the last year.",
                        "A great athlete and a great student. Had a 4.0 all through high school.",
                        "He was 5'3\" until a growth spurt his junior year.",
                        "Has a sweet tooth, eats candy bars during halftime.",
                        "He wanted to be a track star but a foot injury kept him off that path.",
                        "Is prolific on social media, has 50,000 Twitter followers.",
                        "Wants to go to a school close to a Chik-Fil-A.",
                        "Is known to play College Hoops Coach during class.",
                        "Doesn't like to talk about practice.",
                        "Has a burger named after him in his hometown.",
                        "Shows tremendous flash and flair. Loves to put on a show.",
                        "Likes to celebrate a lot on the court, and is very expressive.",
                        "Started on his varsity team as a freshman.",
                        "Has nerves of steel, good at making comebacks at the end of a game.",
                        "Been dunking since the 8th grade, does windmills for fun.",
                        "The son of Dwight Howard, wants to follow in his father's footsteps.",
                        "Didn't play basketball until he was 15, but has been a natural ever since."
                };
        return personalities[(int)(Math.random()*personalities.length)];
    }

}
