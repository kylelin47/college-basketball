package io.coachapps.collegebasketballcoach.adapters.game;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import io.coachapps.collegebasketballcoach.fragments.BracketDialogFragment;
import io.coachapps.collegebasketballcoach.R;

public class BracketListArrayAdapter extends ArrayAdapter<String>  {
    private final Context context;
    /**
     *   Array of game toString()
     */
    private String[][] gameSummaries;
    // boundaries to place rounds
    private List<Integer> boundaries;
    private final BracketDialogFragment bracketDialogFragment;

    public BracketListArrayAdapter(Context context, BracketDialogFragment bracketDialogFragment,
                                   List<String> games) {
        super(context, R.layout.game_result_list_item, games);
        this.context = context;
        this.bracketDialogFragment = bracketDialogFragment;
        setGames(games);
        setBoundaries();
    }

    private void setGames(List<String> games) {
        gameSummaries = new String[games.size()][8];
        for (int i = 0; i < games.size(); i++) {
            gameSummaries[i] = games.get(i).split(",");
        }
    }

    private void setBoundaries() {
        boundaries = new ArrayList<>();
        for (int i = 1; i < gameSummaries.length; i++) {
            if (getWeek(gameSummaries[i]) != getWeek(gameSummaries[i-1])) {
                boundaries.add(i - 1);
            }
        }
    }

    @Override
    @NonNull
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.bracket_list_item, parent, false);

        TextView textHomeName = (TextView) rowView.findViewById(R.id.bracketListHomeTeam);
        TextView textAwayName = (TextView) rowView.findViewById(R.id.bracketListAwayTeam);
        Button gameButton = (Button) rowView.findViewById(R.id.bracketListGameButton);
        if (boundaries.contains(position)) {
            LinearLayout rootLayout = (LinearLayout) rowView.findViewById(R.id.bracketListRootLayout);
            TextView round = new TextView(context);
            round.setText("Round " + (boundaries.indexOf(position) + 2));
            round.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            rootLayout.addView(round);
        }
        final String[] gameInfo = gameSummaries[position];
        textHomeName.setText(gameInfo[0] + " (" + gameInfo[8] + ")");
        textAwayName.setText(gameInfo[1] + " (" + gameInfo[9] + ")");
        if (gameInfo[6].equals("true")) {
            gameButton.setText(gameInfo[2] + " - " + gameInfo[3]);
        } else {
            gameButton.setText("---");
        }
        gameButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (gameInfo[6].equals("true")) {
                    bracketDialogFragment.showGameSummaryDialog(getYear(gameInfo),
                           getWeek(gameInfo), gameInfo[0], gameInfo[1],
                            "(" + gameInfo[8] + ") " + gameInfo[0],
                            "(" + gameInfo[9] + ") " + gameInfo[1]);
                }
            }
        });
        textHomeName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bracketDialogFragment.examineTeam(gameInfo[0]);
            }
        });
        textAwayName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bracketDialogFragment.examineTeam(gameInfo[1]);
            }
        });
        return rowView;
    }
    private int getYear(String[] gameSummary) {
        return Integer.valueOf(gameSummary[4]);
    }
    private int getWeek(String[] gameSummary) {
        return Integer.valueOf(gameSummary[5]);
    }
}
