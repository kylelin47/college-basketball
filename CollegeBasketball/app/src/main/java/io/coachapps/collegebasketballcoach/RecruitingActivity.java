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
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
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
import io.coachapps.collegebasketballcoach.models.PlayerModel;
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

    private static final int NUM_RECRUITS = 300;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recruiting);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Get all teams from DB
        playerDao = new PlayerDao(this);
        teamDao = new TeamDao(this);
        existingPlayers = new ArrayList<>();
        existingPlayersTeamMap = new HashMap<>();
        playerImprovementMap = new HashMap<>();
        try {
            teamList = teamDao.getAllTeams(getYear());
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
        playerGen = new PlayerGen(getString(R.string.league_player_names),
                getString(R.string.league_last_names), getYear());
        availableRecruits = playerGen.genRecruits(NUM_RECRUITS);
        Collections.sort(availableRecruits, new PlayerOverallComp());
        recruitCostMap = new HashMap<>();
        recruitPersonalityMap = new HashMap<>();
        commitments = new ArrayList<>();
        for (Player p : availableRecruits) {
            recruitCostMap.put(p, (int)(((Math.random()*50 + 75)*(p.getOverall()-60))/5));
            if (recruitCostMap.get(p) < 50) recruitCostMap.put(p, 50);
        }
        for (Player p : availableRecruits) {
            int personality = (int)(10*Math.random());
            if (personality == 0) {
                // Hometown discount
                recruitPersonalityMap.put(p, "Grew up a fan of the " + playerTeamName);
                recruitCostMap.put(p, (recruitCostMap.get(p)*3)/4);
            } else if (personality == 1) {
                // Wants to be a hero
                recruitPersonalityMap.put(p, "Wants to be the star of his own team");
            } else if (personality == 2) {
                // Wants to play for winner
                recruitPersonalityMap.put(p, "Winning is the most important thing, wants to play for proven winner");
            } else {
                recruitPersonalityMap.put(p, getRandomPersonality());
            }
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
                dialogLoading = ProgressDialog.show(RecruitingActivity.this, "",
                        getResources().getString(R.string.recruiting_loading_msg), true);
                dialogLoading.setCancelable(false);
                new FinishRecruitingTask().execute();
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

    public void goToMainActivity() {
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
            filteredList.addAll(availableRecruits);
            Collections.sort(filteredList, new PlayerOverallComp());
            filteredList = filteredList.subList(0, Math.min(50, availableRecruits.size()));
            recruitingListView.setAdapter(new RecruitsListArrayAdapter(this, filteredList,
                    recruitCostMap, recruitPersonalityMap));
        } else {
            for (Player p : availableRecruits) {
                if (p.getPosition() == selection) filteredList.add(p);
            }
            Collections.sort(filteredList, new PlayerOverallComp());
            recruitingListView.setAdapter(new RecruitsListArrayAdapter(this, filteredList,
                    recruitCostMap, recruitPersonalityMap));
        }

        recruitingListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Player p = ((RecruitsListArrayAdapter) recruitingListView
                        .getAdapter()).getItem(position);
                showRecruitDialog(p);
            }
        });
    }

    public void showPlayersLeavingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setView(getLayoutInflater().inflate(R.layout.simple_list, null));
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

        List<Player> playerTeamSeniors = new ArrayList<>();
        for (Player p : existingPlayers) {
            if (existingPlayersTeamMap.get(p) == playerTeam && p.year > 4) {
                playerTeamSeniors.add(p);
            }
        }
        Collections.sort(playerTeamSeniors, new PlayerOverallComp());

        final ListView listView = (ListView) dialog.findViewById(R.id.listView);
        listView.setAdapter(new PlayerStatsRatingsListArrayAdapter(this, playerTeamSeniors, getYear()-1));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Player p = ((PlayerStatsRatingsListArrayAdapter)(listView.getAdapter())).getItem(position);
                showPlayerDialog(p);
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
        /*
        if (p.ratings.steal <= threshold) {
            list.add("A great pickpocket, has the ability to get steals on a nightly basis");
        }
        if (p.ratings.block <= threshold) {
            list.add("Can block shots with the best of them");
        }
        */
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

    private String getRandomPersonality() {
        String[] personalities =
                {
                        "Known to be a joker in the locker room",
                        "He is all business on the court, takes the game very seriously",
                        "Grew up the son of an NBA player and has been around the game his whole life",
                        "Came from a very bad neighborhood, and used basketball to escape",
                        "Was a late bloomer in high school, didn't make the varsity team until his senior year",
                        "Was also a 5-star football prospect, but ultimately chose basketball",
                        "Tore his ACL in his junior year, but bounced back in a big way",
                        "Starred in a hit dunk highlight video (as the one being dunked on)"
                };
        return personalities[(int)(Math.random()*personalities.length)];
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
                int oldOverall = p.getOverall();
                PlayerGen.advanceYearRatings(p.ratings);
                p.updateOverall();
                int newOverall = p.getOverall();
                playerImprovementMap.put(p, newOverall - oldOverall);
                playerDao.updatePlayer(new PlayerModel(p, existingPlayersTeamMap.get(p).getName()));
            }
            db.delete(Schemas.BoxScoreEntry.TABLE_NAME, null, null);
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
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

}
