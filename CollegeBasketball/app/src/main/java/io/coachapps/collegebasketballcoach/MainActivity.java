package io.coachapps.collegebasketballcoach;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import io.coachapps.collegebasketballcoach.adapters.PlayerRatingsListArrayAdapter;
import io.coachapps.collegebasketballcoach.adapters.PlayerStatsListArrayAdapter;
import io.coachapps.collegebasketballcoach.basketballsim.Player;
import io.coachapps.collegebasketballcoach.basketballsim.PlayerGen;
import io.coachapps.collegebasketballcoach.basketballsim.Simulator;
import io.coachapps.collegebasketballcoach.basketballsim.Team;

public class MainActivity extends AppCompatActivity {

    Simulator bballSim;
    PlayerGen playerGen;
    ArrayList<Team> teamList;

    Spinner teamSpinner;
    TextView currTeamTextView;

    ListView mainList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Make generator, passing in possible player names
        playerGen = new PlayerGen(getString(R.string.league_player_names),
                                  getString(R.string.league_last_names));

        // Make 10 teams;
        teamList = new ArrayList<>();
        for (int i = 0; i < 10; ++i) {
            teamList.add(new Team("Team " + i, playerGen));
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
                        mainList.setAdapter(new PlayerStatsListArrayAdapter(MainActivity.this, teamList.get(position).getPlayers()));
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

}
